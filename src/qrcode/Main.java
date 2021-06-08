package qrcode;

public class Main {

	/*
	 * Input to transform into QRCode.
	 */
	private static final String INPUT =  "Hello World !";
	/*
	 * Correction level from weakest to strongest : L, M, Q and H
	 */
	private static final char LVL = 'L';

	public static void main(String[] args) {

		/*
		 * Choose if you want the interface to generate QRCodes or
		 * just generate a QRCode from the input 'INPUT' with the
		 * correction level 'LVL' (see above)
		 */

		new TextFieldInterface();
		// generateQRCodeFromInput();

	}

	/**
	 * This method generates a QRCode from the input 'INPUT' with the
	 * correction level 'LVL'
	 */
	public static void generateQRCodeFromInput() {

		int best_version = QRCodeInfos.bestVersion(INPUT, LVL);
		boolean[] encodedData = DataEncoding.byteModeEncoding(INPUT, best_version, LVL);
		int[][] qrCode = MatrixConstruction.renderQRCodeMatrix(best_version, encodedData, LVL);

		int scaling = 400 / qrCode.length;
		Helpers.show(qrCode, scaling);

	}

}
