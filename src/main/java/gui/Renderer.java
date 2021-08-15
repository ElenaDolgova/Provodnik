package gui;

import exception.FileProcessingException;
import model.Directory;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.Image;
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
     * The method adds a new node to the {@link gui.DirectoryView#directoryScrollPane}
     * and updates the list of files {@link gui.FilesView#fileScrollPane}
     *
     * @param directory the directory for updating {@link gui.FilesView#fileScrollPane}
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
     * Cleaning the directory tree on the scroll with directories up to the passed to {@param to}
     *
     * @param to up to what number in the directory tree should removing directories
     */
    public void squeezeDirectories(int to) {
        DefaultListModel<Directory> sourceModel = getModel(directoryView.getDirectoryScrollPane());
        for (int i = sourceModel.getSize() - 1; i > to; --i) {
            sourceModel.remove(i);
        }
    }

    /**
     * The method removes the leaf element from the scroll with directories
     *
     * @return the last remaining element in the directory
     */
    public Directory squeezeDirectoriesByOne() {
        DefaultListModel<Directory> sourceModel = getModel(directoryView.getDirectoryScrollPane());
        if (sourceModel.getSize() > 1) {
            sourceModel.remove(sourceModel.getSize() - 1);
        }
        return sourceModel.get(sourceModel.getSize() - 1);
    }

    /**
     * The method updates the files for the current directory
     *
     * @param directory the directory for which the files need to be updated
     */
    public void updateFilesScrollPane(Directory directory) {
        DefaultListModel<Directory> sourceModel = getModel(filesView.getFileScrollPane());
        updateFiles(sourceModel, directory, null);
    }

    /**
     * The method updates the files of the last directory taking into account the filter by extension
     *
     * @param ext filter by extension
     */
    public void updateFilesScrollPane(String ext) {
        DefaultListModel<Directory> sourceModel = getModel(filesView.getFileScrollPane());
        Directory lastDirectory = directoryView.getLastDirectoryFromScroll(directoryView.getDirectoryScrollPane());
        if (lastDirectory == null) {
            lastDirectory = directoryView.getLastDirectoryFromScroll(directoryView.getRootsScrollPane());
        }
        updateFiles(sourceModel, lastDirectory, ext);
    }


    /**
     * The method updates the files on the tab with a file scroll {@link gui.FilesView#fileScrollPane}
     * At the same time, the process of drawing the spinner starts during loading.
     *
     * @param resource  updated file scroll resource
     * @param directory the directory from which the files are taken
     * @param ext       filter by extension
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
                                                previewImageCache.computeAndCacheAsync(
                                                        directory.getPath().toString(),
                                                        previewPanelView.getPreviewPanel().getWidth(),
                                                        -1,
                                                        () -> {
                                                            final Image[] imageIcon = new Image[1];
                                                            directory.processFile(in ->
                                                                            imageIcon[0] = previewPanelView.getImage(in),
                                                                    probeContentType
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
                    setSpinnerVisible(false);
                }
            }.execute();
        });
    }

    /**
     * Clearing the panel with files {@link gui.FilesView#fileScrollPane}
     * and hiding the preview information {@link gui.PreviewPanelView#previewPanel}
     */
    public void clearFileScrollPane() {
        JList<Directory> links = (JList<Directory>) filesView.getFileScrollPane().getViewport().getView();
        if (links != null && links.getModel() != null && links.getModel().getSize() > 0) {
            DefaultListModel<Directory> sourceModel = (DefaultListModel<Directory>) links.getModel();
            sourceModel.clear();
        }
        previewPanelView.hideContent();
    }

    /**
     * Updating the panel with the display of a text file {@link gui.PreviewPanelView#textArea}
     * or image {@link gui.PreviewPanelView#image}
     */
    public void updatePreviewPanel(String probeContentType, Directory directory) {
        try {
            int imageWidth = previewPanelView.getPreviewPanel().getWidth();
            if (imageWidth > 0 && (probeContentType.contains("image") || probeContentType.contains("text"))) {
                final ImageIcon icon = previewImageCache.computeIfAbsent(
                        directory.getPath().toString(),
                        imageWidth,
                        -1,
                        () -> {
                            final Image[] imageIcon = new Image[1];
                            directory.processFile(in ->
                                            imageIcon[0] = previewPanelView.getImage(in),
                                    probeContentType);
                            return imageIcon[0];
                        });
                if (icon != null) {
                    previewPanelView.update(icon, this);
                } else {
                    directory.processFile(it -> previewPanelView.update(probeContentType, it, this), probeContentType);
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
        JOptionPane.showMessageDialog(filesView.getFileScrollPane(),
                new String[]{e.getMessage()},
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}
