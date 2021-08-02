import javax.swing.*;
import java.io.IOException;
import java.util.SortedSet;

public final class Renderer {
    private final JFrame GLOBAL_FRAME;
    private final DirectoryScrollPane DIRECTORY_SCROLL_PANE;
    private final FilesScrollPane FILES_SCROLL_PANE;
    private final PreviewPanel PREVIEW_PANEL;

    public Renderer(JFrame GLOBAL_FRAME,
                    DirectoryScrollPane DIRECTORY_SCROLL_PANE,
                    FilesScrollPane FILES_SCROLL_PANE,
                    PreviewPanel PREVIEW_PANEL) {
        this.GLOBAL_FRAME = GLOBAL_FRAME;
        this.DIRECTORY_SCROLL_PANE = DIRECTORY_SCROLL_PANE;
        this.FILES_SCROLL_PANE = FILES_SCROLL_PANE;
        this.PREVIEW_PANEL = PREVIEW_PANEL;
    }

    public void updateFilesScrollPane(Directory directory) {
        JList<Link> displayFiles = getDirectoryFiles(directory);
        FILES_SCROLL_PANE.getScrollPane().setViewportView(displayFiles);
        //todo не понимаю, где оставить реализацию листенеров. Потому что в нутрь листенера передается
        // render.  и мне это не нравится
        displayFiles.addMouseListener(FilesScrollPane.getMouseListener(this));
    }

    /**
     * Метод возвращает список файлов текущей директории
     */
    private JList<Link> getDirectoryFiles(Directory directory) {
        SortedSet<Link> set = directory.getFiles();
        DefaultListModel<Link> labelJList = new DefaultListModel<>();
        set.forEach(labelJList::addElement);
        return new JList<>(labelJList);
    }

    /**
     * Метод добавляет на таб с директориями новый узел и обновляет список файлов каталога
     *
     * @param newDirectory директория, в которой нужно обновить отображения файлов
     */
    public void addNewDirectory(Directory newDirectory) {
        JList<Directory> displayDirectory = (JList<Directory>) DIRECTORY_SCROLL_PANE.getScrollPane().getViewport().getView();
        DefaultListModel<Directory> sourceModel = (DefaultListModel<Directory>) displayDirectory.getModel();
        if (!sourceModel.get(sourceModel.getSize() - 1).equals(newDirectory)) {
            // добавляем новую директорию на панель с директориями
            sourceModel.addElement(newDirectory);
            // обновляем панельку с фалами
            updateFilesScrollPane(newDirectory);
        }
    }

    /**
     * Обновляем панель с отображением текстового файла или изображения
     *
     * @param displayFiles
     */
    public void updatePreviewPanel(Link displayFiles) {
        try {
            PREVIEW_PANEL.update(displayFiles.getProbeContentType(), displayFiles.getInputStreamOfFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
