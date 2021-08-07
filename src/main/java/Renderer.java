import javax.swing.*;
import java.io.IOException;
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
        clearFileScrollPane();
        DefaultListModel<Directory> sourceModel = getModel(filesScrollPane.getScrollPane());
        getDirectoryFiles(sourceModel, directory, null);
    }

    /**
     * Метод обнолвяет файлы самой послденей директории с учетом фильтра по расширению
     *
     * @param ext Расширение, по которому нужно пофильтровать файлы
     */
    public void updateFilesScrollPane(String ext) {
        DefaultListModel<Directory> sourceModel = getModel(filesScrollPane.getScrollPane());
        Directory lastDirectory = directoryScrollPane.getLastDirectoryFromScroll();
        getDirectoryFiles(sourceModel, lastDirectory, ext);
    }

    /**
     * Метод возвращает список файлов текущей директории
     */
    private void getDirectoryFiles(DefaultListModel<Directory> list, Directory directory, String ext) {
        SwingUtilities.invokeLater(() -> setThrobberVisible(true));
        SwingUtilities.invokeLater(() -> {
            list.clear();
            new SwingWorker<Void, Directory>() {
                @Override
                protected Void doInBackground() {
                    directory.getFiles(it -> it.forEach(this::publish), ext);
                    return null;
                }

                @Override
                protected void process(List<Directory> chunks) {
                    list.addAll(chunks);
                }

                @Override
                protected void done() {
                    SwingUtilities.invokeLater(() -> setThrobberVisible(false));
                }
            }.execute();
        });
    }

    /**
     * Очищаем панель с файлами и скрываем превью информацию
     */
    public void clearFileScrollPane() {
        JList<Directory> links = (JList<Directory>) this.filesScrollPane.getScrollPane().getViewport().getView();
        if (links != null && links.getModel() != null && links.getModel().getSize() > 0) {
            DefaultListModel<Directory> sourceModel = (DefaultListModel<Directory>) links.getModel();
            sourceModel.clear();
        }
        PreviewPanel.hideContent();
    }

    /**
     * Обновляем панель с отображением текстового файла или изображения
     *
     * @param displayFiles
     */
    public void updatePreviewPanel(String probeContentType, Directory displayFiles) {
        try {
            displayFiles.processFile(it -> previewPanel.update(probeContentType, it, this));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static <T> DefaultListModel<T> getModel(JScrollPane scrollPane) {
        JList<T> displayDirectory = (JList<T>) scrollPane.getViewport().getView();
        return (DefaultListModel<T>) displayDirectory.getModel();
    }

    public void setThrobberVisible(boolean visible) {
        filesScrollPane.getSpinner().setVisible(visible);
    }
}
