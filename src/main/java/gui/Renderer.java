package gui;

import exception.FileProcessingException;
import model.Directory;

import javax.swing.*;
import java.util.List;

public class Renderer {
    private final DirectoryView directoryView;
    private final FilesView filesView;
    private final PreviewPanelView previewPanelView;
    private final PreviewImageCache previewImageCache;

    public Renderer(DirectoryView directoryView,
                    FilesView filesView,
                    PreviewPanelView previewPanelView,
                    PreviewImageCache previewImageCache) {
        this.directoryView = directoryView;
        this.filesView = filesView;
        this.previewPanelView = previewPanelView;
        this.previewImageCache = previewImageCache;
    }

    /**
     * Метод добавляет на таб с директориями новый узел и обновляет список файлов каталога
     *
     * @param directory директория, в которой нужно обновить отображения файлов
     */
    public void addNewDirectory(Directory directory) {
        JList<Directory> displayDirectory = (JList<Directory>) directoryView.getDirectoryScrollPane().getViewport().getView();
        DefaultListModel<Directory> sourceModel = (DefaultListModel<Directory>) displayDirectory.getModel();

        if (sourceModel.getSize() == 0 || !sourceModel.get(sourceModel.getSize() - 1).equals(directory)) {
            // добавляем новую директорию на панель с директориями
            sourceModel.addElement(directory);
            displayDirectory.setSelectedIndex(sourceModel.getSize() - 1);
            // обновляем панельку с фалами
            updateFilesScrollPane(directory);
        }
    }

    /**
     * Чистим дерево директорий на скролле с директориями до переданного номера to
     *
     * @param to - до какого номера в дереве директорий убирать директории
     */
    public void squeezeDirectories(int to) {
        DefaultListModel<Directory> sourceModel = getModel(directoryView.getDirectoryScrollPane());
        for (int i = sourceModel.getSize() - 1; i > to; --i) {
            sourceModel.remove(i);
        }
    }

    /**
     * Метод убирает листовой елемент из скролла с директориями
     * Из дерева директорий нельзя убрать рут
     *
     * @return последний оставшийся елемент в диреткории
     */
    public Directory squeezeDirectoriesByOne() {
        DefaultListModel<Directory> sourceModel = getModel(directoryView.getDirectoryScrollPane());
        if (sourceModel.getSize() > 1) {
            sourceModel.remove(sourceModel.getSize() - 1);
        }
        return sourceModel.get(sourceModel.getSize() - 1);
    }

    /**
     * Метод обновляет файлы для текущей диретории
     *
     * @param directory Директория, файлы для которой нужно обновить
     */
    public void updateFilesScrollPane(Directory directory) {
        DefaultListModel<Directory> sourceModel = getModel(filesView.getScrollPane());
        updateFiles(sourceModel, directory, null);
    }

    /**
     * Метод обнолвяет файлы самой последней директории с учетом фильтра по расширению
     *
     * @param ext Расширение, по которому нужно пофильтровать файлы
     */
    public void updateFilesScrollPane(String ext) {
        DefaultListModel<Directory> sourceModel = getModel(filesView.getScrollPane());
        Directory lastDirectory = directoryView.getLastDirectoryFromScroll(directoryView.getDirectoryScrollPane());
        if (lastDirectory == null) {
            lastDirectory = directoryView.getLastDirectoryFromScroll(directoryView.getRootsScrollPane());
        }
        updateFiles(sourceModel, lastDirectory, ext);
    }


    /**
     * Метода обновляет файлы на табе с файловым скролом.
     * При этом во время загрузки запускается процесс отрисовки спинера, если обновление файла будет занимать долгое время.
     * Например, когда происходит подгрузка файлов с удаленного сервера
     *
     * @param resource  обноляемый ресур файлового скролла
     * @param directory директория, из которй берутся файлы
     * @param ext       фильтр по расширению
     */
    private void updateFiles(DefaultListModel<Directory> resource, Directory directory, String ext) {
        SwingUtilities.invokeLater(() -> setSpinnerVisible(true));
        SwingUtilities.invokeLater(() -> {
            resource.clear();
            new SwingWorker<Void, Directory>() {
                @Override
                protected Void doInBackground() {
                    try {
                        directory.getFiles(batchDirectories ->
                                batchDirectories.forEach(
                                        directory -> {
                                            this.publish(directory);
                                            String probeContentType = Directory.getProbeContentType(directory.getPath());
                                            if (probeContentType != null && probeContentType.contains("image")) {
                                                previewImageCache.computeAndCacheAsync(directory.getPath().toString(), () -> {
                                                    final ImageIcon[] imageIcon = new ImageIcon[1];
                                                    directory.processFile(in ->
                                                            imageIcon[0] = previewPanelView.getImageIcon(in)
                                                    );
                                                    return imageIcon[0];
                                                });
                                            }
                                        }), ext);
                    } catch (FileProcessingException e) {
                        showWarningPane(e);
                    }
                    return null;
                }

                @Override
                protected void process(List<Directory> chunks) {
                    resource.addAll(chunks);
                }

                @Override
                protected void done() {
                    SwingUtilities.invokeLater(() -> setSpinnerVisible(false));
                }
            }.execute();
        });
    }

    /**
     * Очищаем панель с файлами и скрываем превью информацию
     */
    public void clearFileScrollPane() {
        JList<Directory> links = (JList<Directory>) filesView.getScrollPane().getViewport().getView();
        if (links != null && links.getModel() != null && links.getModel().getSize() > 0) {
            DefaultListModel<Directory> sourceModel = (DefaultListModel<Directory>) links.getModel();
            sourceModel.clear();
        }
        previewPanelView.hideContent();
    }

    /**
     * Обновляется панель с отображением текстового файла или изображения
     */
    public void updatePreviewPanel(String probeContentType, Directory displayFiles) {
        try {
            if (probeContentType.contains("image") || probeContentType.contains("text")) {
                final ImageIcon icon = previewImageCache.get(displayFiles.getPath().toString());
                if (icon != null) {
                    previewPanelView.update(icon, this);
                } else {
                    displayFiles.processFile(it -> previewPanelView.update(probeContentType, it, this));
                }
            }
        } catch (exception.FileProcessingException e) {
            e.printStackTrace();
        }
    }

    private static <T> DefaultListModel<T> getModel(JScrollPane scrollPane) {
        JList<T> displayDirectory = (JList<T>) scrollPane.getViewport().getView();
        return (DefaultListModel<T>) displayDirectory.getModel();
    }

    public void setSpinnerVisible(boolean visible) {
        filesView.getSpinner().setVisible(visible);
    }

    public void showWarningPane(FileProcessingException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(filesView.getScrollPane(),
                new String[]{e.getMessage()},
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}
