import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
// todo один интерфейс для FileScrollPane тоже?
public class DirectoryScrollPane {
    // todo если под винду, то не понятно, что такое стартовый путь
    // todo понять, почему /Users/elena-dolgova/arcadia/arcadia/mbi/mbi/ не считает диреторией
    private static final File START_PATH = new File("/Users/elena-dolgova/Desktop");

    // плохо, так как не final
    private final JScrollPane jScrollPane;


    public DirectoryScrollPane() {
        JList<LocalDirectory> displayDirectory = new JList<>(createDirectoryLinks(START_PATH));
        MouseListener mouseListener = getMouseListener();
        displayDirectory.addMouseListener(mouseListener);
        JScrollPane scrollPane = new JScrollPane(displayDirectory);
        scrollPane.setBounds(1, 1, Dimensions.DIRECTORY_SCROLL_PANE_WIDTH, Dimensions.DIRECTORY_SCROLL_PANE_HEIGHT);
        scrollPane.setLayout(new ScrollPaneLayout());
        this.jScrollPane = scrollPane;
    }

    public static DefaultListModel<LocalDirectory> createDirectoryLinks(File fullPath) {
        List<LocalDirectory> links = new ArrayList<>(fullPath.toPath().getNameCount());
        String parent = fullPath.toPath().toAbsolutePath().toString();
        while (parent != null) {
            Path path = Paths.get(parent).toAbsolutePath();
            LocalDirectory localDirectory = new LocalDirectory(path);
            links.add(localDirectory);
            parent = path.toFile().getParent();
        }
        DefaultListModel<LocalDirectory> labelJList = new DefaultListModel<>();
        for (int i = links.size() - 1; i >= 0; --i) {
            labelJList.addElement(links.get(i));
        }
        return labelJList;
    }

    /**
     * Метод вызывается при клике на определенный элемент на панели с директориями.
     * При это если пользователь нажал на не листовую директорию,
     * то директории перестают отображаться ровно до выбранной и на панеле с просмоторщиком файлов начинают
     * отображаться файлы, выбранной директории.
     */
    // todo не с первого раза работают кнопки, может попробовать другой листенер?
    private MouseAdapter getMouseListener() {
        // тест кейс:
        // 1. нажимаем на директорию в середине и у нас удаляется хвост (причем, чтобы память не текла, надо еще удалть ссылки на обхекты)
        // 2. нажимаем на последнюю директорию и ничего не меняется И директории не перестраиваются.
        // 3. Зашли в поддерево, вышли из него -> зашли в более глубокое

        return new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JList<Directory> source = (JList<Directory>) e.getSource();
                // обновляем содержимое панели с файлами
                source.getSelectedValue().updateFilesScrollPane();
                //схлопываем директорию до нажатой
                DefaultListModel<Directory> sourceModel = (DefaultListModel<Directory>) source.getModel();
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
