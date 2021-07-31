import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FilesScrollPane {
    private final JScrollPane jScrollPane;

    public FilesScrollPane() {
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(Dimensions.DIRECTORY_SCROLL_PANE_WIDTH + 3, 1, Dimensions.FILE_SCROLL_PANE_WIDTH, Dimensions.MAIN_HEIGHT);
        scrollPane.setLayout(new ScrollPaneLayout());
        this.jScrollPane = scrollPane;
    }

    /**
     *
     */
    public static MouseAdapter getMouseListener() {
        // тест кейс:
        // 1. нажимаем на директорию в середине и у нас удаляется хвост (причем, чтобы память не текла, надо еще удалть ссылки на обхекты)
        // 2. нажимаем на последнюю директорию и ничего не меняется И директории не перестраиваются.
        // 3. Зашли в поддерево, вышли из него -> зашли в более глубокое

        return new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JList<FileLink> source = (JList<FileLink>) e.getSource();

                FileLink displayFiles = source.getSelectedValue();
                // todо тест кейса на добавление не повторяющихся файлов
                // тест на добавление файлов в директорию

                if (displayFiles.isDirectory()) {
                    JList<DirectoryLink> displayDirectory =
                            (JList<DirectoryLink>) MainFrame.DIRECTORY_SCROLL_PANE.getScrollPane().getViewport().getView();
                    DefaultListModel<DirectoryLink> sourceModel = (DefaultListModel<DirectoryLink>) displayDirectory.getModel();
                    DirectoryLink newDirectory = new DirectoryLink(displayFiles.getPath());
                    if (!sourceModel.get(sourceModel.getSize() - 1).equals(newDirectory)) {
                        // добавляем новую директорию на панель с директориями
                        sourceModel.addElement(newDirectory);
                        // обновляем панельку с фалами
                        newDirectory.updateFilesScrollPane();
                    }
                }
            }
        };
    }

    public JScrollPane getScrollPane() {
        return jScrollPane;
    }
}
