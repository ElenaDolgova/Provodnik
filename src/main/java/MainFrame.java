import javax.swing.*;
import java.awt.*;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class MainFrame {
    public static final JFrame GLOBAL_FRAME = new JFrame("Vitamin Well");
    public static final DirectoryScrollPane DIRECTORY_SCROLL_PANE = new DirectoryScrollPane();
    public static final FilesScrollPane FILES_SCROLL_PANE = new FilesScrollPane();
    public static final PreviewPanel PREVIEW_PANEL = new PreviewPanel();

    public MainFrame() {
        GLOBAL_FRAME.setLayout(new BorderLayout());
        GLOBAL_FRAME.setDefaultCloseOperation(EXIT_ON_CLOSE);
        GLOBAL_FRAME.setPreferredSize(new Dimension(Dimensions.MAIN_WIDTH, Dimensions.MAIN_HEIGHT));
        GLOBAL_FRAME.getContentPane().add(DIRECTORY_SCROLL_PANE.getScrollPane(), BorderLayout.WEST);
        GLOBAL_FRAME.getContentPane().add(FILES_SCROLL_PANE.getScrollPane(), BorderLayout.CENTER);

        PREVIEW_PANEL.init();

        GLOBAL_FRAME.pack();
        GLOBAL_FRAME.setVisible(true);
    }
}
