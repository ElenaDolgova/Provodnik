package gui;

import exception.FileProcessingException;
import model.Directory;

import javax.swing.*;
import java.util.List;

public class Renderer {
    private final DirectoryView directoryView;
    private final FilesView filesView;
    private final PreviewPanelView previewPanelView;

    public Renderer(DirectoryView directoryView,
                    FilesView filesView,
                    PreviewPanelView previewPanelView) {
        this.directoryView = directoryView;
        this.filesView = filesView;
        this.previewPanelView = previewPanelView;
    }

    /**
     * Метод добавляет на таб с директориями новый узел и обновляет список файлов каталога
     *
     * @param newDirectory директория, в которой нужно обновить отображения файлов
     */
    public void addNewDirectory(Directory newDirectory) {
        JList<Directory> displayDirectory = (JList<Directory>) directoryView.getScrollPane().getViewport().getView();
        DefaultListModel<Directory> sourceModel = (DefaultListModel<Directory>) displayDirectory.getModel();

        if (sourceModel.getSize() == 0 || !sourceModel.get(sourceModel.getSize() - 1).equals(newDirectory)) {
            // добавляем новую директорию на панель с директориями
            sourceModel.addElement(newDirectory);
            displayDirectory.setSelectedIndex(sourceModel.getSize() - 1);
            // обновляем панельку с фалами
            updateFilesScrollPane(newDirectory);
        }
    }

    /**
     * Из дерева директорий нельзя убрать рут
     *
     * @param to - до какого номера в дереве директорий убирать директории
     * @return последний оставшийся елемент в диреткории
     */
    public void squeezeDirectories(int to) {
        DefaultListModel<Directory> sourceModel = getModel(directoryView.getScrollPane());
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
        DefaultListModel<Directory> sourceModel = getModel(directoryView.getScrollPane());
        if (sourceModel.getSize() > 1) {
            sourceModel.remove(sourceModel.getSize() - 1);
        }
        return sourceModel.get(sourceModel.getSize() - 1);
    }

    /**
     * Метод обновляет файлы для текущей диретории
     *
     * @param directory Дирктория, файлы для которой нужно обновить
     */
    public void updateFilesScrollPane(Directory directory) {
//        clearFileScrollPane();
        DefaultListModel<Directory> sourceModel = getModel(filesView.getScrollPane());
        getDirectoryFiles(sourceModel, directory, null);
    }

    /**
     * Метод обнолвяет файлы самой послденей директории с учетом фильтра по расширению
     *
     * @param ext Расширение, по которому нужно пофильтровать файлы
     */
    public void updateFilesScrollPane(String ext) {
        DefaultListModel<Directory> sourceModel = getModel(filesView.getScrollPane());
        Directory lastDirectory = directoryView.getLastDirectoryFromScroll(directoryView.getScrollPane());
        getDirectoryFiles(sourceModel, lastDirectory, ext);
    }

    /**
     * Метод возвращает список файлов текущей директории
     */
    private void getDirectoryFiles(DefaultListModel<Directory> list, Directory directory, String ext) {
        SwingUtilities.invokeLater(() -> setSpinnerVisible(true));
        SwingUtilities.invokeLater(() -> {
            list.clear();
            new SwingWorker<Void, Directory>() {
                @Override
                protected Void doInBackground() {
                    try {
                        directory.getFiles(it -> it.forEach(this::publish), ext);
                    } catch (FileProcessingException e) {
                        showWarningPane(e);
                    }
                    return null;
                }

                @Override
                protected void process(List<Directory> chunks) {
                    list.addAll(chunks);
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
        PreviewPanelView.hideContent();
    }

    /**
     * Обновляем панель с отображением текстового файла или изображения
     *
     * @param displayFiles
     */
    public void updatePreviewPanel(String probeContentType, Directory displayFiles) {
        try {
            displayFiles.processFile(it -> previewPanelView.update(probeContentType, it, this));
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
