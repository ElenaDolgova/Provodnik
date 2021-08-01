import javax.swing.*;
import java.awt.*;
import java.io.*;

public class PreviewPanel extends Component {
    JPanel jPanel = new JPanel(new GridBagLayout());
    JLabel image = new JLabel();
    JTextArea textArea = new JTextArea(7, 40);

    public void init() {
        jPanel.setPreferredSize(new Dimension(300, 300));
        jPanel.add(image);
        image.setVisible(false);

        jPanel.add(textArea);
        textArea.setVisible(false);

        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        MainFrame.GLOBAL_FRAME.getContentPane().add(jPanel, BorderLayout.EAST);
    }

    public void update(Image image) {
        ImageIcon icon = new ImageIcon(image.getScaledInstance(300, -1, Image.SCALE_FAST));
        this.image.setIcon(icon);
        textArea.setVisible(false);
        this.image.setVisible(true);
//        repaint();
    }

    public void update(File file) {
        this.image.setVisible(false);
        textArea.setVisible(true);
        try {
            // todo не могу считать часть файла
            FileInputStream in = new FileInputStream(file);
//            byte[] b = in.readNBytes(1);
            BufferedReader input = new BufferedReader(new InputStreamReader(in), 1);
            textArea.read(input, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
