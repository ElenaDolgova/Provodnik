import javax.swing.*;
import java.awt.*;

public class PreviewPanel extends JApplet {
    JPanel p1 = new JPanel(new BorderLayout());
    JLabel img = new JLabel();

    public void init() {
        //setting size for applet
        setSize(300, 300);
        p1.setPreferredSize(new Dimension(300, 300));
        p1.add(img, BorderLayout.CENTER);
        MainFrame.GLOBAL_FRAME.getContentPane().add(p1,  BorderLayout.EAST);
    }

    public void update(Image image) {
        ImageIcon icon = new ImageIcon(image.getScaledInstance(200, -1, Image.SCALE_DEFAULT));
        img.setIcon(icon);
        repaint();
    }
}
