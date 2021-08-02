import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class FilesScrollPane {
    private final JScrollPane jScrollPane;

    public FilesScrollPane() {
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(Dimensions.DIRECTORY_SCROLL_PANE_WIDTH + 3, 1, Dimensions.FILE_SCROLL_PANE_WIDTH, Dimensions.FILE_SCROLL_PANE_HEIGHT);
        scrollPane.setLayout(new ScrollPaneLayout());
        this.jScrollPane = scrollPane;
    }

    /**
     * Вызывается при нажатии на файл в табе с файлами (в каталоге) {@link MainFrame.FILES_SCROLL_PANE}
     */
    public static MouseAdapter getMouseListener(Renderer renderer) {
        // тест кейс:
        // 1. нажимаем на директорию в середине и у нас удаляется хвост (причем, чтобы память не текла, надо еще удалть ссылки на обхекты)
        // 2. нажимаем на последнюю директорию и ничего не меняется И директории не перестраиваются.
        // 3. Зашли в поддерево, вышли из него -> зашли в более глубокое

        return new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // todo по пути /Users/tmpAdmin/ не обновляется файловый таб
                JList<Link> source = (JList<Link>) e.getSource();
                Link displayFiles = source.getSelectedValue();
                // todо тест кейса на добавление не повторяющихся файлов
                // тест на добавление только нового! файлов в директорию

                // todo zip внутри zip
                Directory newDirectory;
                try {
                    // вот тут важный момент. Если это zip, то возвращается директория.
                    newDirectory = displayFiles.createDirectory();
                    if (newDirectory != null) {
                        FilesScrollPane.addNewDirectory(newDirectory, renderer);
                    } else {
                        renderer.getPREVIEW_PANEL().update(displayFiles.getProbeContentType(), displayFiles.getInputStreamOfFile());
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        };
    }

    /**
     * Метод добавляет на таб с директориями новый узел и обновляет список файлов каталога
     *
     * @param newDirectory директория, в которой нужно обновить отображения файлов
     */
    public static void addNewDirectory(Directory newDirectory,
                                       Renderer renderer) {
        JList<Directory> displayDirectory =
                (JList<Directory>) renderer.getDIRECTORY_SCROLL_PANE().getScrollPane().getViewport().getView();
        DefaultListModel<Directory> sourceModel = (DefaultListModel<Directory>) displayDirectory.getModel();
        if (!sourceModel.get(sourceModel.getSize() - 1).equals(newDirectory)) {
            // добавляем новую директорию на панель с директориями
            sourceModel.addElement(newDirectory);
            // обновляем панельку с фалами
            renderer.updateFilesScrollPane(newDirectory);
        }
    }

    public JScrollPane getScrollPane() {
        return jScrollPane;
    }
}
