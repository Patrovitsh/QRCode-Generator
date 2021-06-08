package qrcode;

import java.nio.charset.StandardCharsets;

import reedsolomon.ErrorCorrectionEncoding;

public final class DataEncoding {

	/**
	 * @return boolean array representing the data
	 */
	public static boolean[] byteModeEncoding(String input, int version, char lvl) {

		int inputSize = QRCodeInfos.getMaxInputLength(version, lvl);
		int[] tabIntInput = encodeString(input, inputSize);

		int[] encodedArray = addInformations(tabIntInput, version);

		int finalLength = QRCodeInfos.getCodeWordsLength(version, lvl);
		int[] tabFilled = fillSequence(encodedArray, finalLength);

		int[] dataBlock = dataBlock(tabFilled, version, lvl);

		return bytesToBinaryArray(dataBlock);
	}

	/**
	 * Method similar to that of addInformations (int [] inputBytes) in DataEncoding.java
	 * This is simply adapted for versions greater than or equal to 10. In addition, the
	 * size of the input is here coded on 2 byte and not just one.
	 *
	 * @return The input bytes with an header giving the type and size of the data
	 */
	private static int[] addInformations(int[] inputBytes, int version) {
		if (version < 10) return addInformations(inputBytes);

		int inputLength = inputBytes.length;
		int[] tabInformations = new int[inputLength + 3];

		int prefixQR = 0b0100;
		int sizeBinary = inputLength & 0xFF_FF;
		int sizeBinaryLeft = (sizeBinary >> 8) & 0xFF;
		int sizeBinaryRight = sizeBinary & 0xFF;

		int deleteLeft = 0xF;
		tabInformations[0] = ((prefixQR & deleteLeft) << 4) | (sizeBinaryLeft >> 4);
		tabInformations[1] = ((sizeBinaryLeft & deleteLeft) << 4) | (sizeBinaryRight >> 4);

		if (inputLength == 0) {
			tabInformations[2] = ((sizeBinaryRight & deleteLeft) << 4) | (0);
		} else {
			tabInformations[2] = ((sizeBinaryRight & deleteLeft) << 4) | (inputBytes[0] >> 4);
			tabInformations[inputLength + 2] = (inputBytes[inputLength - 1] & deleteLeft) << 4;
		}

		for (int i = 0; i < inputLength - 1 ; ++i) {
			int partOne = (inputBytes[i] & deleteLeft) << 4;
			int partTwo = inputBytes[i + 1] >> 4;

			int outputNumber = partOne | partTwo;
			tabInformations[i + 3] = outputNumber;
		}

		return tabInformations;
	}

	/**
	 * Add the 12 bits information data and concatenate the bytes to it
	 *
	 * @param inputBytes
	 *            the data byte sequence
	 * @return The input bytes with an header giving the type and size of the data
	 */
	public static int[] addInformations(int[] inputBytes) {
		int inputLength = inputBytes.length;
		int[] tabInformations = new int[inputLength + 2];

		int prefixQR = 0b0100;
		int sizeBinary = getBinaryNumber(inputLength);

		int deleteLeft = 0xF;
		tabInformations[0] = ((prefixQR & deleteLeft) << 4) | (sizeBinary >> 4);

		if (inputLength == 0) {
			tabInformations[1] = ((sizeBinary & deleteLeft) << 4) | (0);
		} else {
			tabInformations[1] = ((sizeBinary & deleteLeft) << 4) | (inputBytes[0] >> 4);
			tabInformations[inputLength + 1] = (inputBytes[inputLength - 1] & deleteLeft) << 4;
		}

		for (int i = 0; i < inputLength - 1 ; ++i) {
			int partOne = (inputBytes[i] & deleteLeft) << 4;
			int partTwo = inputBytes[i + 1] >> 4;

			int outputNumber = partOne | partTwo;
			tabInformations[i + 2] = outputNumber;
		}

		return tabInformations;
	}

	/**
	 * Method which allows QRCodes to be encoded in all correction levels and
	 * up to version 40. It defines the number of small and long blocks, and
	 * calls for error correction accordingly.
	 *
	 * @param data
	 *           data encoded in numbers
	 * @param version
	 * 			qrcode version
	 * @param lvl
	 *           a character that defines the level of correction (L, M, Q, H)
	 * @return integer array populated in the correct order with error correction data.
	 */
	private static int[] dataBlock(int[] data, int version, char lvl) {

		int nbBlocks = QRCodeInfos.nbBlocks(version, lvl);
		int size = data.length;
		int eccLength = QRCodeInfos.getECCLength(version, lvl);
		int eccParBlock = eccLength / nbBlocks;
		int lgBlocks = data.length % nbBlocks;
		int[] dataBlocks = new int[size + eccLength];
		int var;
		int taille = size / nbBlocks;

		for (int i = 1; i <= nbBlocks-lgBlocks; ++i) {
			int[] tab = new int[taille];
			var = (i-1)*taille;
			if (i * taille - var >= 0) System.arraycopy(data, var, tab, 0, i * taille - var);

			int[] tabError = ErrorCorrectionEncoding.encode(tab, eccParBlock);

			for (int k = 0; k < size-lgBlocks; k += nbBlocks) {
				dataBlocks[k+i-1] = tab[k/nbBlocks];
			}
			for (int p = size; p < size+eccLength; p += nbBlocks) {
				dataBlocks[p+i-1] = tabError[(p-size)/nbBlocks];
			}
		}

		int cpt = 0;
		for (int i = nbBlocks-lgBlocks+1; i <= nbBlocks; ++i) {
			int[] tab = new int[(size / nbBlocks)+1];
			var = ((i-1)*taille) + (i-(nbBlocks-lgBlocks)) -1;
			int i1 = i * taille + (i - (nbBlocks - lgBlocks)) - var;
			if (i1 >= 0)
				System.arraycopy(data, var, tab, 0, i1);

			int[] tabError = ErrorCorrectionEncoding.encode(tab, eccParBlock);

			for (int k = 0; k < size-lgBlocks; k += nbBlocks) {
				dataBlocks[k+i-1] = tab[k/nbBlocks];
			}
			dataBlocks[size-lgBlocks+cpt] = tab[tab.length-1];
			++cpt;
			for (int p = size; p < size+eccLength; p += nbBlocks) {
				dataBlocks[p+i-1] = tabError[(p-size)/nbBlocks];
			}
		}
		return dataBlocks;
	}

	/**
	 * @param input
	 *            The string to convert to ISO-8859-1
	 * @param maxLength
	 *          The maximal number of bytes to encode (will depend on the version of the QR code) 
	 * @return A array that represents the input in ISO-8859-1. The output is
	 *         truncated to fit the version capacity
	 */
	public static int[] encodeString(String input, int maxLength) {
		byte[] tabByte = input.getBytes(StandardCharsets.ISO_8859_1);
		int max = Math.min(maxLength, tabByte.length);
		int [] myTab = new int[max];
		
		for (int i = 0; i < max; ++i) {
			myTab[i] = getBinaryNumber(tabByte[i]);
		}
		
		return myTab;
	}

	/**
	 * Add padding bytes to the data until the size of the given array matches the
	 * finalLength
	 * 
	 * @param encodedData
	 *            the initial sequence of bytes
	 * @param finalLength
	 *            the minimum length of the returned array
	 * @return an array of length max(finalLength,encodedData.length) padded with
	 *         bytes 236,17
	 */
	public static int[] fillSequence(int[] encodedData, int finalLength) {
		int tabLength = encodedData.length;
		int[] sequenceTab = new int[finalLength];
		
		copyArray(encodedData, sequenceTab);
		
		int step = 0;
		for (int i = tabLength; i < finalLength; ++i) {
			sequenceTab[i] = (step % 2 == 0) ? 236 : 17;
			step++;
		}
		
		return sequenceTab;
	}

	/**
	 * Encode the byte array into a binary array represented with boolean using the
	 * most significant bit first.
	 * 
	 * @param data
	 *            an array of bytes
	 * @return a boolean array representing the data in binary
	 */
	public static boolean[] bytesToBinaryArray(int[] data) {
		int finalLength = data.length * 8;
		boolean[] finalTab = new boolean[finalLength];
		int indice;
		int bit;
		
		for (int j = 0; j < data.length; ++j) {
			for (int i = 7; i >= 0; --i) {
				indice = (j * 8) + 7 - i;
				bit = ((data[j] >> i) & 0b1);
				finalTab[indice] = bit == 1;
			}
		}
	    
		return finalTab;
	}
	
	/**
	 * Convertie un entier en sa forme binaire.
	 *            
	 * @return an Int number coded in binary
	 */
	private static int getBinaryNumber(int number) {
		return number & 0xFF;
	}
	
	/**
	 * Copie un tableau dans un nouveau tableau de taille supérieur ou égale.
	 * 
	 * @param tabOriginal
	 *            un array à copier
	 * @param tabCopy
	 *            nouvel array indentiaque au précédent
	 */
	private static void copyArray(int[] tabOriginal, int[] tabCopy) {
		int size1 = tabOriginal.length;
		int size2 = tabCopy.length;
		if (size1 > size2) size1 = size2;
		System.arraycopy(tabOriginal, 0, tabCopy, 0, size1);
	}
	
}
