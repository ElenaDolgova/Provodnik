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
     * Вызывается при нажатии на файл в табе с файлами (в каталоге) {@link FILES_SCROLL_PANE}
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
                try {
                    if (displayFiles.isDirectory()) {
                        Directory newDirectory = displayFiles.createDirectory();
                        renderer.addNewDirectory(newDirectory);
                    } else {
                        renderer.updatePreviewPanel(displayFiles.getProbeContentType(),displayFiles);
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        };
    }

    public JScrollPane getScrollPane() {
        return jScrollPane;
    }
}
