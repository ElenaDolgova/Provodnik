import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.stream.Collectors;

public class PreviewPanel extends Component {
    private static final int MAX_TEXT_LINES = 20;
    private final static JPanel jPanel = new JPanel(new GridBagLayout());
    private final static JLabel image = new JLabel();
    private final static JTextArea textArea = new JTextArea(7, 40);

    public void init() {
        jPanel.setPreferredSize(new Dimension(Dimensions.PREVIEW_PANEL_WIDTH, Dimensions.PREVIEW_PANEL_HEIGHT));
        jPanel.add(image);
        image.setVisible(false);

        jPanel.add(textArea);
        textArea.setVisible(false);

        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        MainFrame.GLOBAL_FRAME.getContentPane().add(jPanel, BorderLayout.EAST);
    }

    public void updateImage(Image inputImage) {
        ImageIcon icon = new ImageIcon(inputImage.getScaledInstance(300, -1, Image.SCALE_FAST));
        image.setIcon(icon);
        textArea.setVisible(false);
        image.setVisible(true);
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

    public void updateTxt(File file) {
        image.setVisible(false);
        textArea.setVisible(true);
        try {
            // todo мне не нравится такое чтение по частям
            try (FileInputStream fileInputStream = new FileInputStream(file);
                 InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
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
