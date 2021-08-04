import org.apache.commons.vfs2.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.StreamSupport;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class Main {
    public static void main(String[] args) {
        JFrame.setDefaultLookAndFeelDecorated(true);
        final Main main = new Main();
        javax.swing.SwingUtilities.invokeLater(main::init);
    }

    private void init() {
        JFrame GLOBAL_FRAME = new JFrame("Vitamin Well");
        DirectoryScrollPane DIRECTORY_SCROLL_PANE = new DirectoryScrollPane();

        FilesScrollPane FILES_SCROLL_PANE = new FilesScrollPane();
        PreviewPanel PREVIEW_PANEL = new PreviewPanel();

        GLOBAL_FRAME.setLayout(new BorderLayout());
        GLOBAL_FRAME.setDefaultCloseOperation(EXIT_ON_CLOSE);
        GLOBAL_FRAME.setPreferredSize(new Dimension(Dimensions.MAIN_WIDTH, Dimensions.MAIN_HEIGHT));
        GLOBAL_FRAME.getContentPane().add(DIRECTORY_SCROLL_PANE.getScrollPane(), BorderLayout.WEST);
        GLOBAL_FRAME.getContentPane().add(FILES_SCROLL_PANE.getScrollPane(), BorderLayout.CENTER);
        PREVIEW_PANEL.init(GLOBAL_FRAME);

        Renderer renderer = new Renderer(GLOBAL_FRAME, DIRECTORY_SCROLL_PANE, FILES_SCROLL_PANE, PREVIEW_PANEL);

        DIRECTORY_SCROLL_PANE.init(renderer);

        GLOBAL_FRAME.pack();
        GLOBAL_FRAME.setVisible(true);
    }
}
