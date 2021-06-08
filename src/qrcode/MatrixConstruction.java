package qrcode;

public class MatrixConstruction {
	
	/*
	 * Constants defining the color in ARGB format
	 * 
	 * W = White integer for ARGB
	 * 
	 * B = Black integer for ARGB
	 * 
	 * both needs to have their alpha component to 255
	 */
	static final int ALPHA = 0xFF_00_00_00;
	private static final int W = 0xFF_FF_FF_FF;
	private static final int B = 0xFF_00_00_00;

	/**
	 * Create the matrix of a QR code with the given data.
	 *
	 * @param version
	 *            The version of the QR code
	 * @param data
	 *            The data to be written on the QR code
	 * @param mask
	 *            The mask used on the data. If not valid (e.g: -1), then no mask is
	 *            used.
	 * @param lvl
	 *           un charactère qui définie le niveau de correction (L, M, Q, H)
	 * @return The matrix of the QR code
	 */
	public static int[][] renderQRCodeMatrix(int version, boolean[] data, int mask, char lvl) {

		int[][] matrix = constructMatrix(version, mask, lvl);

		addDataInformation(matrix, data, mask);

		return matrix;
	}

	/**
	 * Create the matrix of a QR code with the given data.
	 *
	 * The mask is computed automatically so that it provides the least penalty
	 *
	 * @param version
	 *            The version of the QR code
	 * @param data
	 *            The data to be written on the QR code
	 * @param lvl
	 *           un charactère qui définie le niveau de correction (L, M, Q, H)
	 * @return The matrix of the QR code
	 */
	public static int[][] renderQRCodeMatrix(int version, boolean[] data, char lvl) {

		int mask = findBestMasking(version, data, lvl);

		return renderQRCodeMatrix(version, data, mask, lvl);
	}

	/**
	 * Create a matrix (2D array) ready to accept data for a given version and mask
	 *
	 * @param version
	 *            the version number of QR code (has to be between 1 and 4 included)
	 * @param mask
	 *            the mask id to use to mask the data modules. Has to be between 0
	 *            and 7 included to have a valid matrix. If the mask id is not
	 *            valid, the modules would not be not masked later on, hence the
	 *            QRcode would not be valid
	 * @param lvl
	 *           un charactère qui définie le niveau de correction (L, M, Q, H)
	 * @return the qrcode with the patterns and format information modules
	 *         initialized. The modules where the data should be remain empty.
	 */
	private static int[][] constructMatrix(int version, int mask, char lvl) {
		int[][] matrix = initializeMatrix(version);
		addFinderPatterns(matrix);
		addAlignmentPatterns(matrix, version);
		addTimingPatterns(matrix);
		addDarkModule(matrix);
		addFormatInformation(matrix, mask, lvl);
		if (version > 6) addFormat(matrix, version);
		return matrix;

	}

	/**
	 * Create an empty 2d array of integers of the size needed for a QR code of the
	 * given version
	 * 
	 * @param version
	 *            the version number of the qr code (has to be between 1 and 4
	 *            included
	 * @return an empty matrix
	 */
	public static int[][] initializeMatrix(int version) {
		int size = QRCodeInfos.getMatrixSize(version);
		return new int[size][size];
	}

	/**
	 * Add all finder patterns to the given matrix with a border of White modules.
	 * 
	 * @param matrix
	 *            the 2D array to modify: where to add the patterns
	 */
	public static void addFinderPatterns(int[][] matrix) {
		int size = matrix.length;
		bordBlanc(matrix, 0, 0);
		bordBlanc(matrix, size-8, 0);
		bordBlanc(matrix, 0, size-8);
		pattern(matrix, 3, 3, 3);
		pattern(matrix, size-4, 3, 3);
		pattern(matrix, 3, size-4, 3);
	}

	/**
	 * Méthode permettant d'ajouter les Alignment Patterns pour toutes les versions
	 * de QRCode.
	 *
	 * @param matrix matrice
	 * @param version version
	 */
	private static void addAlignmentPatterns(int[][] matrix, int version) {
		if (version < 2) return;
		if (version < 7) {
			int index = matrix.length;
			pattern(matrix, index-7, index-7, 2);
			return;
		}

		int index = matrix.length;
		int nb = QRCodeInfos.nbCoordonnees(version);
		int[] coordonnees = new int[nb+1];

		coordonnees[0] = 6;
		coordonnees[nb] = index-7;

		for (int i = 1; i < nb; ++i) {
			coordonnees[i] = QRCodeInfos.COORDONNEES[i-1][version-(i*7)];
		}

		for (int i = 0; i < nb+1; ++i) {
			for (int j = 0; j < nb+1; ++j) {
				patternSup(matrix, coordonnees[i], coordonnees[j], 2);
			}
		}

	}

	/**
	 * Add the timings patterns
	 * 
	 * @param matrix
	 *            The 2D array to modify
	 */
	public static void addTimingPatterns(int[][] matrix) {
		int size = matrix.length;
		for (int i = 8; i <= size-8; ++i) {
			matrix[6][i] = (i % 2 == 0) ? B : W;
			matrix[i][6] = (i % 2 == 0) ? B : W;
		}
	}

	/**
	 * Add the dark module to the matrix
	 * 
	 * @param matrix
	 *            the 2-dimensional array representing the QR code
	 */
	public static void addDarkModule(int[][] matrix) {
		int size = matrix.length;
		matrix[8][size-8] = B;
	}

	/**
	 * Add the format information to the matrix
	 *
	 * @param matrix
	 *            the 2-dimensional array representing the QR code to modify
	 * @param mask
	 *            the mask id
	 * @param lvl
	 *           un charactère qui définie le niveau de correction (L, M, Q, H)
	 */
	private static void addFormatInformation(int[][] matrix, int mask, char lvl) {
		boolean[] tabFormat = QRCodeInfos.getFormatSequence(mask, lvl);
		int matrixSize = matrix.length;

		//Pattern horizontal
		int cpt = 0;
		for (int i = 0; i < matrixSize; ++i) {
			matrix[i][8] = (tabFormat[cpt]) ? B : W;
			++cpt;

			if (i == 5) ++i;
			if (i == 7) i = matrixSize - 9;
		}

		//Pattern vertical
		cpt = 0;
		for (int j = matrixSize - 1; j >= 0; --j) {
			matrix[8][j] = (tabFormat[cpt]) ? B : W;
			++cpt;

			if (j == matrixSize - 7) j = 9;
			if (j == 7) --j;
		}
	}

	/**
	 * Ajoute un pattern (noir/blanc alterné) de taille "taille", centré en (col,line),
	 * à la matrice.
	 *
	 * @param matrix
	 * 			  matrice
	 *
	 * @param col
	 *            colonne de la matrice
	 * @param line
	 *            ligne de la matrice
	 * @param taille
	 *            taille du pattern à insérer dans la matrice
	 */
	public static void pattern(int[][]matrix, int col, int line, int taille) {
		int couleur;
		int step = 0;

		for (int i = taille; i >= taille-2; --i) {
			couleur = (step % 2 == 0) ? B : W;
			carre(matrix, col, line, i, couleur);
			++step;
		}
	}

	/**
	 * Méthode analogue à la méthode pattern dans le fichier MatrixConstruction.java
	 * mais celle ci vérifie en plus que ne va pas se superposer sur un pattern existant.
	 *
	 * @param matrix
	 *			  matrice
	 * @param col
	 *            colonne de la matrice
	 * @param line
	 *            ligne de la matrice
	 * @param taille
	 *            taille du pattern à insérer dans la matrice
	 */
	public static void patternSup(int[][]matrix, int col, int line, int taille) {
		int couleur;
		int step = 0;

		for (int j = col-taille; j <= col+taille; ++j )
			for (int i = line-taille; i <= line+taille; ++i)
				if ((matrix[i][j] & ALPHA) != 0) return;

		for (int i = taille; i >= taille-2; --i) {
			couleur = (step % 2 == 0) ? B : W;
			carre(matrix, col, line, i, couleur);
			++step;
		}
	}
	
	/**
	 * Ajoute un carré noir ou blanc d'une taille "taille", centré en (col,line).
	 * 
	 * @param matrix
	 *            matrice
	 * @param col
	 *            colonne de la matrice
	 * @param line
	 *            ligne de la matrice
	 * @param taille
	 *            taille du carré à insérer dans la matrice 
	 * @param couleur
	 *            entier définissant la couleur du carré (noir ou blanc)       
	 */
	public static void carre(int[][]matrix, int col, int line, int taille, int couleur) {
		for (int i = -taille; i <= taille; ++i) {
			for(int j = -taille; j <= taille; ++j) {
				matrix[line+i][col+j] = couleur;
			}
		}
	}
	
	/**
	 * Ajoute un carré blanc d'une taille 8 à la matrice. Cette méthode est différente
	 * de la méthode carre car celle ci a une taille 8 qui est paire. Cela induit que le 
	 * carré blanc est donc centré en quatre modules et non plus un seul comme dans la 
	 * méthode carre. En outre, elle permet d'ajouter simplement le bord blanc autour du 
	 * finder pattern. 
	 * 
	 * @param matrix
	 *            matrice
	 * @param col
	 *            colonne de la matrice
	 * @param line
	 *            ligne de la matrice       
	 */
	private static void bordBlanc(int[][] matrix, int col, int line) {
		for (int i = 0; i < 8; ++i) {
			for (int j = 0; j < 8; ++j) {
				matrix[line + j][col + i] = W;
			}
		}
	}

	/**
	 * Choose the color to use with the given coordinate using the masking 0
	 *
	 * @return the color with the masking
	 */
	public static int maskColor(int col, int row, boolean dataBit, int masking) {
		
		if (masking > 7 || masking < 0) {
			if (dataBit) return B; //B == R
			else return W; //W == G
		}
		
		boolean testMask = getColorMask(col, row, masking);
		
		if (dataBit && testMask) return W;
		if (!dataBit && testMask) return B;
		
		if (dataBit) return B;
		
		return W;
	}
	
	/**
	 * Add the data bits into the QR code matrix
	 * 
	 * @param matrix
	 *            a 2-dimensionnal array where the bits needs to be added
	 * @param data
	 *            the data to add
	 */
	public static void addDataInformation(int[][] matrix, boolean[] data, int mask) {
		
		int cpt = 0;
		
		for (int col = matrix.length - 1; col > 0; col -= 4) {
			cpt = getUpCode(matrix, data, col, mask, cpt);
			cpt = getDownCode(matrix, data, col - 2, mask, cpt);
		}
	}

	/**
	 * Définie à partir des formules données si le module en (x, y) est masqué ou non.
	 * 
	 * @param x
	 *            la colonne du module
	 * @param y
	 *            la ligne du module
	 * @param mask
	 *            le chiffre du masque
	 * @return un booléen qui vaut vrai si le module en (x, y) doit etre masqué.
	 */
	private static boolean getColorMask(int x, int y, int mask) {
		
		boolean bool = false;
		int i = ((x * y) % 2) + (x * y) % 3;
		switch (mask) {
			case 0:
				bool = ((x + y) % 2 == 0);
				break;
			case 1:
				bool = (y % 2 == 0);
				break;
			case 2:
				bool = (x % 3 == 0);
				break;
			case 3:
				bool = ((x + y) % 3 == 0);
				break;
			case 4:
				bool = (((y / 2) + (x / 3)) % 2 == 0);
				break;
			case 5:
				bool = (i == 0);
				break;
			case 6:
				bool = (i % 2 == 0);
				break;
			case 7:
				bool = (((x + y) % 2 + (x * y) % 3) % 2 == 0);
				break;
		}
		
		return bool;
	}
	
	/**
	 * Place la moitié des données dans le QRCode en partant du bas vers le haut. 
	 * Cette méthode saute le timing pattern vertical.
	 * 
	 * @param matrix
	 *            la matrice prete à recvoir les données
	 * @param data
	 *            les données à mettre dans la matrice contenu dans un tableau de booléen
	 * @param col
	 *            indique la colonne à partir de laquel on ajoute les données
	 * @param mask
	 *            le chiffre du masque
	 * @param cpt 
	 *            un compteur permettant de coordonner les deux méthodes getUpCode et
	 *            getDownCode
	 * @return la matrice avec une double colonne de données ajoutées.
	 */
	private static int getUpCode(int[][] matrix, boolean[] data, int col, int mask, int cpt) {
		
		int dataSize = data.length;
		if (col <= 4) --col;    // Permet de sauter le timing patter vertical
		
		for (int i = matrix.length - 1; i >= 0; --i) {
			for (int c = col; c >= col - 1; --c) {
				if (matrix[c][i] != 0) continue;
				
				boolean dataBit = cpt < dataSize && data[cpt];
				++cpt;
				
				int color = maskColor(c, i, dataBit, mask);
				matrix[c][i] = color;
			}
		}
		
		return cpt;
	}
	
	/**
	 * Place l'autre moitié des données dans le QRCode en partant du haut vers le bas. 
	 * Cette méthode saute le timing pattern vertical.
	 * 
	 * @param matrix
	 *            la matrice prete à recvoir les données
	 * @param data
	 *            les données à mettre dans la matrice contenu dans un tableau de booléen
	 * @param col
	 *            indique la colonne à partir de laquel on ajoute les données
	 * @param mask
	 *            le chiffre du masque
	 * @param cpt 
	 *            un compteur permettant de coordonner les deux méthodes getUpCode et
	 *            getDownCode
	 * @return la matrice avec une double colonne de données ajoutées.
	 */
	private static int getDownCode(int[][] matrix, boolean[] data, int col, int mask, int cpt) {
		
		int dataSize = data.length;
		if (col <= 6) --col;   // Permet de sauter le timing patter vertical
		
		for (int i = 0; i <= matrix.length - 1; ++i) {
			for (int c = col; c >= col - 1; --c) {
				if (matrix[c][i] != 0) continue;
				boolean dataBit = cpt < dataSize && data[cpt];
				++cpt;
				
				int color = maskColor(c, i, dataBit, mask);
				matrix[c][i] = color;
			}
		}
		
		return cpt;
	}

	/**
	 * Find the best mask to apply to a QRcode so that the penalty score is
	 * minimized. Compute the penalty score with evaluate
	 *
	 * @param data
	 * 			 data
	 * @param lvl
	 *           un charactère qui définie le niveau de correction (L, M, Q, H)
	 * @return the mask number that minimize the penalty
	 */
	private static int findBestMasking(int version, boolean[] data, char lvl) {
		int mask = 0, var;
		int min = evaluate(renderQRCodeMatrix(version, data, 0, lvl));

		for (int i = 1; i < 8; ++i) {
			var = evaluate(renderQRCodeMatrix(version, data, i, lvl));
			if (var < min) {
				min = var;
				mask = i;
			}
		}
		return mask;
	}

	/**
	 * Compute the penalty score of a matrix
	 * 
	 * @param matrix:
	 *            the QR code in matrix form
	 * @return the penalty score obtained by the QR code, lower the better
	 */
	public static int evaluate(int[][] matrix) {
		int[] penalite = {0};
		penalite1(matrix, penalite);
		penalite2(matrix, penalite);
		penalite3(matrix, penalite);
		penalite4(matrix, penalite);
		return penalite[0];
	}	
	
	/**
	 * Calcule les points de pénalités de la première forme (plus de 5 modules identique
	 * à la suite). Elle fait appel à la méthode penalite1_bis pour une meilleure 
	 * modularisation.
	 * 
	 * @param matrix:
	 *            the QR code in matrix form
	 * @param penalite
	 *            un tableau d'une case contenant la valeur de la penalite déjà calculé
	 */
	private static void penalite1(int[][] matrix, int[] penalite) {
		penalite[0] += penalite1_bis(matrix, true);
		penalite[0] += penalite1_bis(matrix, false);
	}

	/**
	 * Calcule les points de pénalités de la première forme (plus de 5 modules identique
	 * à la suite), selon les lignes OU les colonnes. Le choix dépend de de la valeur du 
	 * booléen b (true pour faire selon les lignes, false pour selon les colonnes).
	 * 
	 * @param matrix:
	 *            the QR code in matrix form
	 * @param b
	 *            un boolean qui définit si le test s'effectue selon les lignes ou 
	 *            les colonnes.
	 * @return le nombre de point de pénalité calculé.
	 */
	private static int penalite1_bis(int[][] matrix, boolean b) {
		int size = matrix.length;
		int cpt, k;
		int penalite = 0;
		
		for (int i = 0; i < size; ++i) {
			for (int j = 0; j < size; ++j) {
				k = 1;
				cpt = 1;
				if (b) { 
					while ((j+k < size) && (matrix[j][i] == matrix[j+k][i])) {
						++cpt;
						++k;
					}
				} else {
					while ((j+k < size) && (matrix[i][j] == matrix[i][j+k])) {
						++cpt;
						++k;
					}
				}
				if (cpt >= 5) penalite += cpt-2;
				j += cpt -1;
			}
		}
		return penalite;
	}
	
	/**
	 * Calcule les points de pénalités de la duexième forme (carré de 4 modules de la
	 * meme couleur).
	 * 
	 * @param matrix:
	 *            the QR code in matrix form
	 * @param penalite
	 *            un tableau d'une case contenant la valeur de la penalite déjà calculé
	 */
	private static void penalite2(int[][] matrix, int[] penalite) {
		
		int size = matrix.length;
		int val;
		
		for (int i = 0; i < size-1; ++i) {
			for (int j = 0; j < size-1; ++j) {
				val = matrix[j][i];
				if ((val == matrix[j+1][i]) && (val == matrix[j][i+1]) 
						&& (val == matrix[j+1][i+1])) {
					penalite[0] += 3;
				}
			}
		}
		
	}
	
	/**
	 * Calcule les points de pénalités de la troisième forme. Cette méthode regarde pour 
	 * chaque colonne et chaque ligne si elle trouve un des deux motifs contenus dans 
	 * motif1 et motif2. Pour une meilleure modulisation, elle fait appelle à la méthode
	 * pelnalite3_bis.
	 * 
	 * @param matrix:
	 *            the QR code in matrix form
	 * @param penalite
	 *            un tableau d'une case contenant la valeur de la penalite déjà calculé
	 */
	private static void penalite3(int[][] matrix, int[] penalite) {
		
		int[] motif1 = {W, W, W, W, B, W, B, B, B, W, B};
		int[] motif2 = {B, W, B, B, B, W, B, W, W, W, W};
		int motifLength = motif1.length;
		int size = matrix.length;
		
		for (int i = 0; i < size; ++i) {
			for (int j = 0; j < size - motifLength +1; ++j) {
				penalite[0] += penalite3_bis(matrix, motif1, j, i);
				penalite[0] += penalite3_bis(matrix, motif2, j, i);
			}
		} 
		
	}
	
	/**
	 * Calcule les points de pénalités de la troisième forme. Cette méthode complète la 
	 * méthode penalite3. Pour chaque ligne et chaque colonne du QRCode, elle regarde si
	 * elle trouve le motif contenu dans tab. 
	 * Remarque : si deux motifs se superposent alors elle prend en compte la pénalité 
	 * deux fois.
	 * 
	 * @param matrix:
	 *            the QR code in matrix form
	 * @return la pénalité calculé
	 */
	private static int penalite3_bis(int[][] matrix, int[] tab, int col, int line) {
		
		int cpt = 0;
		int penalite = 0;
		
		while (cpt < tab.length && matrix[col+cpt][line] == tab[cpt]) {
			++cpt;
		}
		if (cpt == tab.length) penalite += 40;
		
		cpt = 0;
		
		while (cpt < tab.length && matrix[line][col+cpt] == tab[cpt]) {
			++cpt;
		}
		if (cpt == tab.length) penalite += 40;
		
		return penalite;
	}
	
	/**
	 * Calcule les points de pénalités de la quatrième forme (trop de module noir ou 
	 * trop de module blanc).
	 * 
	 * @param matrix:
	 *            the QR code in matrix form
	 * @param penalite
	 *            un tableau d'une case contenant la valeur de la penalite déjà calculé
	 */
	private static void penalite4(int[][] matrix, int[] penalite) {
		
		int size = matrix.length;
		double nbModules = size * size;
		double blackModules = 0;
		
		for (int i = 0; i < size; ++i) {
			for (int[] ints : matrix) {
				if (ints[i] == B) {
					++blackModules;
				}
			}
		}
		
		int pourcentage = (int) ((blackModules / nbModules) * 100);
		
		int pourcentage1 = (pourcentage / 5) * 5;
		int pourcentage2 = ((pourcentage / 5) + 1) * 5;
		
		pourcentage1 -= 50;
		pourcentage2 -= 50;
		
		if (pourcentage1 < 0) pourcentage1 = - pourcentage1;
		if (pourcentage2 < 0) pourcentage2 = - pourcentage2;
		
		if (pourcentage1 < pourcentage2) {
			penalite[0] += (2 * pourcentage1);
		} else {
			penalite[0] += (2 * pourcentage2);
		}
	}

	/**
	 * Ajoute 18 bit d'information sur la version dans le QRCode ce qui est nécessaire
	 * pour les versions supérieures ou égales à 7.
	 */
	private static void addFormat(int[][] matrix, int version) {
		boolean[] tabFormat = QRCodeInfos.format(version);
		int size = matrix.length;

		int cpt = 0;
		for (int i = 0; i < 6; ++i) {
			for (int j = 0; j < 3; ++j) {
				matrix[i][size-11+j] = (tabFormat[cpt]) ? B : W;
				matrix[size-11+j][i] = (tabFormat[cpt]) ? B : W;
				++cpt;
			}
		}
	}
		
}
