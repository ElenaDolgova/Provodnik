package gui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.stream.Collectors;

public class PreviewPanelView {
    private static final int MAX_TEXT_LINES = 20;
    private final JPanel panel = new JPanel(new GridBagLayout());
    private final JLabel image = new JLabel();

    private final JTextArea textArea = new JTextArea(7, 40);

    public void init() {
        panel.setPreferredSize(new Dimension(Dimensions.PREVIEW_PANEL_WIDTH, Dimensions.PREVIEW_PANEL_HEIGHT));
        initImage();
        initTextArea();
    }

    private void initImage() {
        panel.add(image);
        image.setVisible(false);
    }

    private void initTextArea() {
        panel.add(textArea);
        textArea.setVisible(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
    }

    public void update(String probeContentType, InputStream in, Renderer renderer) {
        if (probeContentType == null) {
            return;
        }
        SwingUtilities.invokeLater(() -> renderer.setSpinnerVisible(true));
        if (probeContentType.contains("image")) {
            updateImage(in);
        } else if (probeContentType.contains("text")) {
            updateTxt(in);
        }
        SwingUtilities.invokeLater(() -> renderer.setSpinnerVisible(false));
    }

    private void updateImage(InputStream in) {
        Image inputImage;
        try {
            ImageIO.setUseCache(false);
            inputImage = ImageIO.read(in);
            ImageIcon icon =
                    new ImageIcon(inputImage.getScaledInstance(panel.getWidth(), -1, Image.SCALE_SMOOTH));
            image.setIcon(icon);
            textArea.setVisible(false);
            image.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateTxt(InputStream in) {
        image.setVisible(false);
        textArea.setVisible(true);
        try {
            try (InputStreamReader inputStreamReader = new InputStreamReader(in);
                 BufferedReader input = new BufferedReader(inputStreamReader)) {
                String str = input.lines()
                        .limit(MAX_TEXT_LINES)
                        .collect(Collectors.joining("\n"));
                input.close();
                textArea.read(new StringReader(str), null);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JPanel getPanel() {
        return panel;
    }

    public void hideContent() {
        image.setVisible(false);
        textArea.setVisible(false);
    }
}
