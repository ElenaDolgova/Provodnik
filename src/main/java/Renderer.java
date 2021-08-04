import javax.swing.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class Renderer {

    private final DirectoryScrollPane directoryScrollPane;
    private final FilesScrollPane filesScrollPane;
    private final PreviewPanel previewPanel;

    public Renderer(DirectoryScrollPane directoryScrollPane,
                    FilesScrollPane filesScrollPane,
                    PreviewPanel previewPanel) {
        this.directoryScrollPane = directoryScrollPane;
        this.filesScrollPane = filesScrollPane;
        this.previewPanel = previewPanel;
    }

    public void updateFilesScrollPane(Directory directory) {
        JList<Link> displayFiles = getDirectoryFiles(directory);
        filesScrollPane.getScrollPane().setViewportView(displayFiles);
        displayFiles.addMouseListener(FilesScrollPane.getMouseListener(this));
    }

    /**
     * Метод возвращает список файлов текущей директории
     */
    private JList<Link> getDirectoryFiles(Directory directory) {
        List<Link> set = directory.getFiles();
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
        JList<Directory> displayDirectory = (JList<Directory>) directoryScrollPane.getScrollPane().getViewport().getView();
        DefaultListModel<Directory> sourceModel = (DefaultListModel<Directory>) displayDirectory.getModel();
        if (!sourceModel.get(sourceModel.getSize() - 1).equals(newDirectory)) {
            // добавляем новую директорию на панель с директориями
            sourceModel.addElement(newDirectory);
            // обновляем панельку с фалами
            updateFilesScrollPane(newDirectory);
        }
    }

    public void clearFileScrollPane() {
        JList<Link> links = (JList<Link>) this.filesScrollPane.getScrollPane().getViewport().getView();
        if (links != null && links.getModel() != null && links.getModel().getSize() > 0) {
            DefaultListModel<Link> sourceModel = (DefaultListModel<Link>) links.getModel();
            sourceModel.clear();
        }
    }

    /**
     * Обновляем панель с отображением текстового файла или изображения
     *
     * @param displayFiles
     */
    public void updatePreviewPanel(String probeContentType, Link displayFiles) {
        try {
            previewPanel.update(probeContentType, displayFiles.getInputStreamOfFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
