import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

/**
 *
 */
public class DirectoryScrollPane {
    private final JScrollPane jScrollPane;

    public DirectoryScrollPane(File fullPath) {
        JList<DirectoryLink> displayDirectory = new JList<>(LinksCreator.createDirectoryLinks(fullPath));
        MouseListener mouseListener = getMouseListener();
        displayDirectory.addMouseListener(mouseListener);
        JScrollPane scrollPane = new JScrollPane(displayDirectory);
        scrollPane.setBounds(1, 1, Dimensions.DIRECTORY_SCROLL_PANE_WIDTH, Dimensions.MAIN_HEIGHT);
        scrollPane.setLayout(new ScrollPaneLayout());
        this.jScrollPane = scrollPane;
    }

    /**
     * Метод вызывается при клике на определенный элемент на панели с директориями.
     * При это если пользователь нажал на не листовую директорию,
     * то директории перестают отображаться ровно до выбранной и на панеле с просмоторщиком файлов начинают
     * отображаться файлы, выбранной директории.
     */
    private MouseAdapter getMouseListener() {
        return new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JList<DirectoryLink> source = (JList<DirectoryLink>) e.getSource();
                DefaultListModel<DirectoryLink> sourceModel = (DefaultListModel<DirectoryLink>) source.getModel();
                source.getSelectedValue().getMouseClickSupplier().get();

                for (int i = sourceModel.getSize() - 1; i > source.getSelectedIndex(); --i) {
                    sourceModel.remove(i);
                }
            }
        };
    }

    public JScrollPane getScrollPane() {
        return jScrollPane;
    }
}
