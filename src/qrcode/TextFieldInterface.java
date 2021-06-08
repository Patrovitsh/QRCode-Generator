package qrcode;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

/**
 * Classe qui représente l'interface
 */
class TextFieldInterface implements ActionListener {

    private final JTextArea textarea;
    private JLabel jLabel;
    private ImageIcon imageIcon;
    private final JComboBox<String> listeCorrection;
    private final JComboBox<String> listeVersion;
    private final JFrame frame= new JFrame();

    TextFieldInterface() {
        frame.setTitle("QRCode");

        JLabel label = new JLabel("Entrez Texte :");
        label.setBounds(250, 10, 150, 20);
        frame.add(label);

        textarea = new JTextArea("Hello World!", 15, 45);
        textarea.setBounds(50, 35, 500, 150);
        frame.add(textarea);

        label = new JLabel("Choisissez le niveau de correction : ");
        label.setBounds(75, 200, 250, 20);
        frame.add(label);

        String[] bigList = {"L", "M", "Q", "H"};
        listeCorrection = new JComboBox<>(bigList);
        listeCorrection.setBounds(325, 200, 150, 20);
        frame.add(listeCorrection);

        label = new JLabel("Choisissez la version minimale : ");
        label.setBounds(75, 235, 250, 20);
        frame.add(label);

        String[] List = new String[41];
        List[0] = "0 (auto)";
        for (int i = 1; i < 41; ++i) {
            List[i] = i + "";
        }
        listeVersion = new JComboBox<>(List);
        listeVersion.setBounds(325, 235, 150, 20);
        frame.add(listeVersion);

        JButton button = new JButton("Créer QRCode");
        button.setBounds(220, 270, 150, 30);
        button.addActionListener(this);
        frame.add(button);

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(600,800);
        frame.setLayout(null);
        frame.setVisible(true);

        imageIcon = new ImageIcon();
        jLabel = new JLabel();
        jLabel.setIcon(imageIcon);
        jLabel.setBounds(50, 280, 500, 500);
        frame.add(jLabel);
    }

    public void actionPerformed(ActionEvent e) {

        frame.remove(jLabel);

        // Récupère les données entrées
        String input = textarea.getText();
        Object lvlObject = listeCorrection.getSelectedItem();
        char lvl = lvlObject.toString().charAt(0);
        Object versionObject = listeVersion.getSelectedItem();
        int version = versionObject.toString().charAt(0)-48;
        if (version != 0 & versionObject.toString().length() == 2) {
            version = (version * 10) + (versionObject.toString().charAt(1)-48);
        }

        // Applique les méthodes nécessaires à la création du QR Code avec les données entrées
        int best_version = QRCodeInfos.bestVersion(input, lvl);
        if (version != 0 & version > best_version) {
            best_version = version;
        }
        boolean[] encodedData = DataEncoding.byteModeEncoding(input, best_version, lvl);
        int[][] qrCode = MatrixConstruction.renderQRCodeMatrix(best_version, encodedData, lvl);

        // Transforme la matrice en image pouvant etre afficher dans la fenetre
        BufferedImage image = new BufferedImage(qrCode.length, qrCode[0].length, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < qrCode.length; x++) {
            for (int y = 0; y < qrCode[0].length; y++) {
                image.setRGB(x, y, qrCode[x][y]);
            }
        }
        int scale = 400 / qrCode.length;
        BufferedImage imageFinale = Helpers.reshape(image, scale, 0);

        // Affiche le QR Code dans la fenetre
        imageIcon = new ImageIcon(imageFinale);
        jLabel = new JLabel();
        jLabel.setIcon(imageIcon);
        jLabel.setBounds(90, 275, 500, 500);
        frame.add(jLabel);

        // Sert de "refresh" pour faire apparaitre le QR Code.
        frame.setVisible(false);
        frame.setVisible(true);

    }

}