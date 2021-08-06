import javax.swing.*;
import java.io.IOException;

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

    /**
     * Метод добавляет на таб с директориями новый узел и обновляет список файлов каталога
     *
     * @param newDirectory директория, в которой нужно обновить отображения файлов
     */
    public void addNewDirectory(Directory newDirectory) {
        DefaultListModel<Directory> sourceModel = getModel(directoryScrollPane.getScrollPane());

        if (!sourceModel.get(sourceModel.getSize() - 1).equals(newDirectory)) {
            // добавляем новую директорию на панель с директориями
            sourceModel.addElement(newDirectory);
            // обновляем панельку с фалами
            updateFilesScrollPane(newDirectory);
        }
    }

    /**
     * Метод обновляет файлы для текущей диретории
     *
     * @param directory Дирктория, файлы для которой нужно обновить
     */
    public void updateFilesScrollPane(Directory directory) {
        DefaultListModel<Link> sourceModel = getModel(filesScrollPane.getScrollPane());
        getDirectoryFiles(sourceModel, directory, null);
    }

    /**
     * Метод обнолвяет файлы самой послденей директории с учетом фильтра по расширению
     *
     * @param ext Расширение, по которому нужно пофильтровать файлы
     */
    public void updateFilesScrollPane(String ext) {
        DefaultListModel<Link> sourceModel = getModel(filesScrollPane.getScrollPane());
        Directory lastDirectory = directoryScrollPane.getLastDirectoryFromScroll();
        getDirectoryFiles(sourceModel, lastDirectory, ext);

//        JList<Link> displayFiles = getDirectoryFiles(lastDirectory, ext);
//        filesScrollPane.getScrollPane().setViewportView(displayFiles);
//        displayFiles.addMouseListener(FilesScrollPane.getMouseListener(this));
    }

    /**
     * Метод возвращает список файлов текущей директории
     */
    private void getDirectoryFiles(DefaultListModel<Link> list, Directory directory, String ext) {
        list.removeAllElements();
        directory.getFiles(list::addElement, ext);
    }

    /**
     * Метод возвращает список файлов текущей директории c фильтрацией по ext
     */
//    private JList<Link> getDirectoryFiles(Directory directory, String ext) {
//        Collection<Link> files = directory.getFiles(ext);
//        return mapToJList(files);
//    }
//
//    private JList<Link> mapToJList(Collection<Link> files) {
//        DefaultListModel<Link> labelJList = new DefaultListModel<>();
//        files.forEach(labelJList::addElement);
//        var jlist = new JList<>(labelJList);
//        jlist.setCellRenderer(new FileListCellRenderer());
//        return jlist;
//    }
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

    private static <T> DefaultListModel<T> getModel(JScrollPane scrollPane) {
        JList<T> displayDirectory = (JList<T>) scrollPane.getViewport().getView();
        return (DefaultListModel<T>) displayDirectory.getModel();
    }
}
