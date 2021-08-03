import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.stream.Collectors;

public class PreviewPanel extends Component {
    private static final int MAX_TEXT_LINES = 20;
    private final static JPanel jPanel = new JPanel(new GridBagLayout());
    private final static JLabel image = new JLabel();
    private final static JTextArea textArea = new JTextArea(7, 40);

    public void init(JFrame GLOBAL_FRAME) {
        jPanel.setPreferredSize(new Dimension(Dimensions.PREVIEW_PANEL_WIDTH, Dimensions.PREVIEW_PANEL_HEIGHT));
        initImage();
        initTextArea();
        GLOBAL_FRAME.getContentPane().add(jPanel, BorderLayout.EAST);
    }

    private void initImage() {
        jPanel.add(image);
        image.setVisible(false);
    }

    private void initTextArea() {
        jPanel.add(textArea);
        textArea.setVisible(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
    }

    public void update(String probeContentType, InputStream in) {
        if (probeContentType == null) {
            return;
        }
        if (probeContentType.contains("image")) {
            updateImage(in);
        } else if (probeContentType.contains("text")) {
            updateTxt(in);
        }
    }

    private void updateImage(InputStream in) {
        Image inputImage;
        try {
            ImageIO.setUseCache(false);
            inputImage = ImageIO.read(in);
            ImageIcon icon = new ImageIcon(inputImage.getScaledInstance(300, -1, Image.SCALE_FAST));
            image.setIcon(icon);
            textArea.setVisible(false);
            image.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateTxt(File file) {
        try {
            // todo мне не нравится такое чтение по частям
            try (FileInputStream fileInputStream = new FileInputStream(file);) {
                updateTxt(fileInputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateTxt(InputStream in) {
        image.setVisible(false);
        textArea.setVisible(true);
        try {
            // todo мне не нравится такое чтение по частям
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

    public static JLabel getImage() {
        return image;
    }

    public static JTextArea getTextArea() {
        return textArea;
    }
}
