import javax.swing.*;
import java.awt.*;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class Main {
    public static void main(String[] args) {
        JFrame.setDefaultLookAndFeelDecorated(true);
        final Main main = new Main();
        javax.swing.SwingUtilities.invokeLater(main::init);
    }

    private void init() {
        final JFrame GLOBAL_FRAME = new JFrame("Vitamin Well");

        final DirectoryScrollPane DIRECTORY_SCROLL_PANE = new DirectoryScrollPane();
        final FilesScrollPane FILES_SCROLL_PANE = new FilesScrollPane();
        final PreviewPanel PREVIEW_PANEL = new PreviewPanel();

        GLOBAL_FRAME.setLayout(new BorderLayout());
        GLOBAL_FRAME.setDefaultCloseOperation(EXIT_ON_CLOSE);
        GLOBAL_FRAME.setPreferredSize(new Dimension(Dimensions.MAIN_WIDTH, Dimensions.MAIN_HEIGHT));
        GLOBAL_FRAME.getContentPane().add(DIRECTORY_SCROLL_PANE.getMainDirectoryPane(), BorderLayout.WEST);
        GLOBAL_FRAME.getContentPane().add(FILES_SCROLL_PANE.getMainFileScrollPane(), BorderLayout.CENTER);
        PREVIEW_PANEL.init(GLOBAL_FRAME);

        Renderer renderer = new Renderer(DIRECTORY_SCROLL_PANE, FILES_SCROLL_PANE, PREVIEW_PANEL);

        DIRECTORY_SCROLL_PANE.init(renderer);
        FILES_SCROLL_PANE.init(renderer);

        GLOBAL_FRAME.pack();
        GLOBAL_FRAME.setVisible(true);
    }
}
