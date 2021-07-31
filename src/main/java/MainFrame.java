import javax.swing.*;
import java.awt.*;
import java.io.File;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

// todo имеет ли смысл каждый из основных компонентов сделать синглетоном?
public class MainFrame {
    private static final String MAIN_TITLE = "Vitamin Well";

    // top-level container
    private final JFrame frame;
    private final DirectoryScrollPane directoryScrollPane;

    public MainFrame(File fullPath) {
        this.frame = new JFrame(MAIN_TITLE);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(Dimensions.MAIN_WIDTH, Dimensions.MAIN_HEIGHT));

        this.directoryScrollPane = new DirectoryScrollPane(fullPath);
    }

    public void createGUI() {
        JScrollPane scrollPane = directoryScrollPane.getScrollPane();

        frame.getContentPane().add(scrollPane);
        frame.setLayout(new BorderLayout());
        frame.pack();
        frame.setVisible(true);
    }
}
