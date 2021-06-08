package qrcode;

public final class QRCodeInfos {

	private static final int MATRIX_SIZE_VERSION_1 =21;
	private static final int MATRIX_SIZE_STEP =4;

	private final static int[][] VERSION_CODE_WORDS = { {19, 34, 55, 80, 108, 136, 156, 194, 232,
			274, 324, 370, 428, 461, 523, 589, 647, 721, 795, 861, 932, 1006, 1094, 1174, 1276, 1370,
			1468, 1531, 1631, 1735, 1843, 1955, 2071, 2191, 2306, 2434, 2566, 2702, 2812, 2956},
			{16, 28, 44, 64,  86, 108, 124, 154, 182,
					216, 254, 290, 334, 365, 415, 453, 507, 563, 627, 669, 714, 782, 860, 914, 1000, 1062,
					1128, 1193, 1267, 1373, 1455, 1541, 1631, 1725, 1812, 1914, 1992, 2102, 2216, 2334},
			{13, 22, 34, 48,  62,  76,  88, 110, 132,
					154, 180, 206, 244, 261, 295, 325, 367, 397, 445, 485, 512, 568, 614, 664, 718, 754, 808,
					871, 911, 985, 1033, 1115, 1171, 1231, 1286, 1354, 1426, 1502, 1582, 1666},
			{ 9, 16, 26, 36,  46,  60,  66, 86, 100,
					122, 140, 158, 180, 197, 223, 253, 283, 313, 341, 385, 406, 442, 464, 514, 538, 596, 628,
					661, 701, 745, 793, 845, 901, 961, 986, 1054, 1096, 1142, 1222, 1276} };

	private static final int[] LVL_CODE = {1,0,3,2};

	private static final int[][] ERROR_CORRECTION_CODEWORDS = { { 7, 10, 15, 20, 26, 36, 40,
			48, 60, 72, 80, 96, 104, 120, 132, 144, 168, 180, 196, 224, 224, 252, 270, 300, 312,
			336, 360, 390, 420, 450, 480, 510, 540, 570, 570, 600, 630, 660, 720, 750},
			{10, 16, 26, 36, 48, 64, 72, 88,
					110, 130, 150, 176, 198, 216, 240, 280, 308, 338, 364, 416, 442, 476, 504, 560, 588,
					644, 700, 728, 784, 812, 868, 924, 980, 1036, 1064, 1120, 1204, 1260, 1316, 1372},
			{13, 22, 36, 52, 72, 96, 108, 132, 160,
					192, 224, 260, 288, 320, 360, 408, 448, 504, 546, 600, 644, 690, 750, 810, 870, 952,
					1020, 1050, 1140, 1200, 1290, 1350, 1440, 1530, 1590, 1680, 1770, 1860, 1950, 2040},
			{17, 28, 44, 64, 88, 112, 130, 156, 192,
					224, 264, 308, 352, 384, 432, 480, 532, 588, 650, 700, 750, 816, 900, 960, 1050, 1110,
					1200, 1260, 1350, 1440, 1530, 1620, 1710, 1800, 1890, 1980, 2100, 2220, 2310, 2430} };

	private static final int[][] NB_BLOCKS = { {1, 1, 1, 1, 1, 2, 2, 2, 2, 4, 4, 4, 4, 4, 6, 6,
			6, 6, 7, 8, 8, 9, 9, 10, 12, 12, 12, 13, 14, 15, 16, 17, 18, 19, 19, 20, 21, 22, 24, 25},
			{1, 1, 1, 2, 2, 4, 4, 4, 5, 5, 5, 8, 9, 9, 10, 10, 11, 13,
					14, 16, 17, 17, 18, 20, 21, 23, 25, 26, 28, 29, 31, 33, 35, 37, 38, 40, 43, 45, 47, 49},
			{1, 1, 2, 2, 4, 4, 6, 6, 8, 8, 8, 10, 12, 16, 12, 17, 16, 18,
					21, 20, 23, 23, 25, 27, 29, 34, 34, 35, 38, 40, 43, 45, 48, 51, 53, 56, 59, 62, 65, 68},
			{1, 1, 2, 4, 4, 4, 5, 6, 8, 8, 11, 11, 16, 16, 18, 16, 19, 21,
					25, 25, 25, 34, 30, 32, 35, 37, 40, 42, 45, 48, 51, 54, 57, 60, 63, 66, 70, 74, 77, 81}  };
	
	public enum CorrectionLvl{
		L,M,Q,H
	}

	/**
	 * Calculates the best version (minimum version) for a given input and level of correction.
	 *
	 * @return the best version
	 */
	public static int bestVersion(String input, char lvl) {
		int bestVersion = 1;
		while (bestVersion < 40 && input.length() > QRCodeInfos.getMaxInputLength(bestVersion, lvl)) {
			++bestVersion;
		}
		return bestVersion;
	}
	
	/**
	 * Get the size of the matrix for a specific version.
	 * i.e. For version 1, this method return 21 since the matrix has a size of 21x21
	 * @param version 
	 *         version of the QRcode
	 * @return an integer : the size of the matrix for a given QR code version
	 */
	public static int getMatrixSize(int version) {
		if(version>40) {
			throw new IllegalArgumentException("The maximum QR code Version is 40");
		}
		return MATRIX_SIZE_VERSION_1 + MATRIX_SIZE_STEP*(version-1);
	}

	public static int getMaxInputLength(int version, char lvl) {
		if(version>40) {
			throw new UnsupportedOperationException("The version has to be between 1 and 41");
		}
		switch (lvl) {
			case 'M' :
				return VERSION_CODE_WORDS[1][version-1] -2;
			case 'Q' :
				return VERSION_CODE_WORDS[2][version-1] -2;
			case 'H' :
				return VERSION_CODE_WORDS[3][version-1] -2;
			default :
				return VERSION_CODE_WORDS[0][version-1] -2;
		}
	}

	public static int getECCLength(int version, char lvl) {
		if(version>40) {
			throw new UnsupportedOperationException("The version has to be between 1 and 41");
		}
		switch (lvl) {
			case 'M' :
				return ERROR_CORRECTION_CODEWORDS[1][version-1];
			case 'Q' :
				return ERROR_CORRECTION_CODEWORDS[2][version-1];
			case 'H' :
				return ERROR_CORRECTION_CODEWORDS[3][version-1];
			default :
				return ERROR_CORRECTION_CODEWORDS[0][version-1];
		}
	}

	public static int getCodeWordsLength(int version, char lvl) {
		if(version>40) {
			throw new UnsupportedOperationException("The version has to be between 1 and 41");
		}
		switch (lvl) {
			case 'M' :
				return VERSION_CODE_WORDS[1][version-1];
			case 'Q' :
				return VERSION_CODE_WORDS[2][version-1];
			case 'H' :
				return VERSION_CODE_WORDS[3][version-1];
			default :
				return VERSION_CODE_WORDS[0][version-1];
		}
	}

	public static boolean[] getFormatSequence(int mask, char lvl) {
		if(mask>7 || mask <0) {
			throw new IllegalArgumentException("The mask has to be between 0 and 7");
		}
		int errorCorrectionLevel;
		switch (lvl) {
			case 'M' :
				if (mask == 0) {
					return new boolean[]{true, false, true, false, true, false, false, false,
							false, false, true, false, false, true, false};
				}
				errorCorrectionLevel=QRCodeInfos.CorrectionLvl.M.ordinal();
				break;
			case 'Q' :
				errorCorrectionLevel=QRCodeInfos.CorrectionLvl.Q.ordinal();
				break;
			case 'H' :
				errorCorrectionLevel=QRCodeInfos.CorrectionLvl.H.ordinal();
				break;
			default :
				errorCorrectionLevel=QRCodeInfos.CorrectionLvl.L.ordinal();
				break;
		}

		int code = ((LVL_CODE[errorCorrectionLevel]& 0x3)<<3) | (mask&0x7);
		int current = code<<10;

		int poly = 0b10100110111;
		int size = 15;
		while(((0b1<<(size-1)) & current) ==0) {
			size--;
			if(size == 0) {
				throw new IllegalAccessError();
			}
		}

		while(size>10) {
			int paddedPoly = poly<<(size-11);

			current = paddedPoly^current;


			while(((0b1<<(size-1)) & current) == 0) {
				size--;
				if(size == 0) {
					throw new IllegalAccessError();
				}
			}
		}

		int format = (code<<10 | (current& 0x3FF)) ^ 0b101010000010010;

		boolean[] formatPixels = new boolean[15];
		for(int i=0;i<formatPixels.length;i++) {
			formatPixels[i] = !(((format >> (14 - i)) & 0b1) == 0);
		}

		return formatPixels;
	}

	/**
	 * @return le nombre de blocks pour les données encodées
	 */
	public static int nbBlocks(int version, char lvl) {
		int cpt = 0;
		switch (lvl) {
			case 'M' :
				cpt = 1;
				break;
			case 'Q' :
				cpt = 2;
				break;
			case 'H' :
				cpt = 3;
				break;
		}

		return NB_BLOCKS[cpt][version-1];
	}

	/**
	 * @return un entier qui donne le nombre de coordonnees mois un pour les
	 *           Alignment Patterns.
	 */
	public static int nbCoordonnees(int version) {
		if (version > 34) {
			return 6;
		} else if (version > 27) {
			return 5;
		} else if (version > 20) {
			return 4;
		} else if (version > 13) {
			return 3;
		} else if (version > 6) {
			return 2;
		}
		return 0;
	}

	/**
	 * Coordonnées pour les Alignments Patterns.
	 */
	public static final int[][] COORDONNEES = { {22, 24, 26, 28, 30, 32, 34, 26, 26, 26, 30, 30, 30,
			34, 28, 26, 30, 28, 32, 30, 34, 26, 30, 26, 30, 34, 30, 34, 30, 24, 28, 32, 26, 30 },
			{46, 48, 50, 54, 56, 58, 62, 50, 50, 54, 54, 58, 58, 62, 50, 54, 52, 56, 60, 58, 62,
					54, 50, 54, 58, 54, 58},
			{72, 74, 78, 80, 84, 86, 90, 74, 78, 78, 82, 86, 86, 90, 78, 76, 80, 84, 82, 86},
			{98, 102, 104, 108, 112, 114, 118, 102, 102, 106, 110, 110, 114},
			{126, 128, 132, 136, 138, 142} };

	/**
	 * En s'aidant du site https ://www.thonky.com/qr-code-tutorial/introduction.
	 * et de la méthode getFormatSequence, nous avons écris l'algorithme permettant
	 * d'obtenir la séquence de 18 bits à mettre dans les QRCode de versions supérieures
	 * ou égales à 7.
	 *
	 * @return un tableau de booléen correspondant à l'information sur la version du QRCode
	 *           pour les versions supérieures ou égales à 7.
	 */
	public static boolean[] format(int version) {

		int code = version & 0b111111;
		int current = code << 12;
		int poly = 0b1111100100101;

		int size = size(current);

		while(size>12) {
			int paddedPoly = poly<<(size-13);

			current = paddedPoly^current;

			size = size(current);
		}

		current = (code << 12) | current;

		boolean[] formatPixels = new boolean[18];
		for(int i = 0; i < formatPixels.length; ++i) {
			formatPixels[i] = (((current >> i) & 0b1) == 1);
		}

		return formatPixels;

	}

	/**
	 * @param current
	 *             un entier codé en binaire
	 * @return la taille de current (son nombre de bits)
	 */
	private static int size(int current) {
		int size = 18;
		while(((0b1<<(size-1)) & current) ==0) {
			size--;
			if(size == 0) {
				throw new IllegalAccessError();
			}
		}
		return size;
	}

}
