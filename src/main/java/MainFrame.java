import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class MainFrame {
    private static final String MAIN_TITLE = "Vitamin Well";

    // todo работать на различных операционных системах
    private static final int MAIN_WIDTH = 700;
    private static final int MAIN_HEIGHT = 600;

    // top-level container
    private final JFrame frame;

    public MainFrame() {
        this.frame = new JFrame(MAIN_TITLE);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(MAIN_WIDTH, MAIN_HEIGHT));
    }

    private void createDirectoryTab(File fullPath) {
        DefaultListModel<DirectoryLink> defaultDirectory = LinksCreator.createHyperLinks(fullPath);
        JList<DirectoryLink> displayDirectory = new JList<>(defaultDirectory);
        MouseListener mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JList<DirectoryLink> source = (JList<DirectoryLink>) e.getSource();
                DefaultListModel<DirectoryLink> sourceModel = (DefaultListModel<DirectoryLink>) source.getModel();
                source.getSelectedValue().getSupplier().get();

                for (int i = sourceModel.getSize() - 1; i > source.getSelectedIndex(); --i) {
                    sourceModel.remove(i);
                }
            }
        };
        displayDirectory.addMouseListener(mouseListener);
        JScrollPane scrollPane = new JScrollPane(displayDirectory);
        scrollPane.setSize(150, MAIN_HEIGHT);
        frame.getContentPane().add(scrollPane);
    }

    public void createGUI(File fullPath) {
        createDirectoryTab(fullPath);
        //todo почему это помогает ?
        //todo придумать, что делать с отображением, когда директория слишком длинная
        // примерное решение:
        // ограничить кол-во высвечиваемых директорий до n
        // в начале высвечивать многоточия и потом только хвост размера n - 1
        // и при нажатие на многоточие показывались + 5 директорий
        frame.setLayout(null);
        frame.pack();
        frame.setVisible(true);
    }
}
