import gui.Dimensions;
import gui.DirectoryScrollPane;
import gui.FilesScrollPane;
import gui.PreviewPanel;
import gui.Renderer;

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
        GLOBAL_FRAME.setLayout(new BorderLayout());
        GLOBAL_FRAME.setDefaultCloseOperation(EXIT_ON_CLOSE);
        GLOBAL_FRAME.setPreferredSize(new Dimension(Dimensions.MAIN_WIDTH, Dimensions.MAIN_HEIGHT));

        final DirectoryScrollPane DIRECTORY_SCROLL_PANE = new DirectoryScrollPane();
        final FilesScrollPane FILES_SCROLL_PANE = new FilesScrollPane();
        final PreviewPanel PREVIEW_PANEL = new PreviewPanel();

        gui.Renderer renderer = new Renderer(DIRECTORY_SCROLL_PANE, FILES_SCROLL_PANE, PREVIEW_PANEL);
        FILES_SCROLL_PANE.init(GLOBAL_FRAME, renderer);
        DIRECTORY_SCROLL_PANE.init(GLOBAL_FRAME, renderer);
        PREVIEW_PANEL.init(GLOBAL_FRAME);

        GLOBAL_FRAME.pack();
        GLOBAL_FRAME.setVisible(true);
    }
}
