import gui.Dimensions;
import gui.DirectoryView;
import gui.FilesView;
import gui.PreviewPanelView;
import gui.Renderer;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class Main {
    public static void main(String[] args) {
        JFrame.setDefaultLookAndFeelDecorated(true);
        final Main main = new Main();
        javax.swing.SwingUtilities.invokeLater(main::init);
    }

    private void init() {
        final JFrame globalFrame = new JFrame("Provodnik");
        globalFrame.setLayout(new BorderLayout());
        globalFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        globalFrame.setPreferredSize(new Dimension(Dimensions.MAIN_WIDTH, Dimensions.MAIN_HEIGHT));

        final DirectoryView directoryScrollPane = new DirectoryView();
        final PreviewPanelView previewPanel = new PreviewPanelView();
        final FilesView filesScrollPane = new FilesView(previewPanel);
        final JSplitPane rightSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                filesScrollPane.getMainFileScrollPane(),
                previewPanel.getPanel()
        );

        final JSplitPane mainSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                directoryScrollPane.getMainDirectoryPane(),
                rightSplit
        );

        gui.Renderer renderer = new Renderer(directoryScrollPane, filesScrollPane, previewPanel);
        filesScrollPane.init(renderer);
        directoryScrollPane.init(renderer);
        previewPanel.init();
        globalFrame.getContentPane().add(mainSplit);

        globalFrame.pack();
        globalFrame.setVisible(true);
    }
}
