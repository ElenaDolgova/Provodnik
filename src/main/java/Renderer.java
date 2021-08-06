import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class Renderer {
    private final ImageIcon folderIcon;
    private final DirectoryScrollPane directoryScrollPane;
    private final FilesScrollPane filesScrollPane;
    private final PreviewPanel previewPanel;

    public Renderer(DirectoryScrollPane directoryScrollPane,
                    FilesScrollPane filesScrollPane,
                    PreviewPanel previewPanel) {
        this.directoryScrollPane = directoryScrollPane;
        this.filesScrollPane = filesScrollPane;
        this.previewPanel = previewPanel;
        File folderImage = new File("src/main/resources/folder.png");
        ImageIcon folderIcon = null;
        try {
            Image image = ImageIO.read(folderImage);
            folderIcon = new ImageIcon(image.getScaledInstance(15, 15, Image.SCALE_FAST));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.folderIcon = folderIcon;
    }

    /**
     * Метод обновляет файлы для текущей диретории
     * @param directory Дирктория, файлы для которой нужно обновить
     */
    public void updateFilesScrollPane(Directory directory) {
        JList<Link> displayFiles = getDirectoryFiles(directory);
        filesScrollPane.getScrollPane().setViewportView(displayFiles);
        displayFiles.addMouseListener(FilesScrollPane.getMouseListener(this));
    }

    /**
     * Метод обнолвяет файлы самой послденей директории с учетом фильтра по расширению
     * @param ext Расширение, по которому нужно пофильтровать файлы
     */
    public void updateFilesScrollPane(String ext) {
        Directory lastDirectory = directoryScrollPane.getLastDirectoryFromScroll();
        JList<Link> displayFiles = getDirectoryFiles(lastDirectory, ext);
        filesScrollPane.getScrollPane().setViewportView(displayFiles);
        displayFiles.addMouseListener(FilesScrollPane.getMouseListener(this));
    }

    /**
     * Метод возвращает список файлов текущей директории
     */
    private JList<Link> getDirectoryFiles(Directory directory) {
        Collection<Link> files = directory.getFiles();
        return mapToJList(files);
    }

    /**
     * Метод возвращает список файлов текущей директории c фильтрацией по ext
     */
    private JList<Link> getDirectoryFiles(Directory directory, String ext) {
        Collection<Link> files = directory.getFiles(ext);
        return mapToJList(files);
    }

    private JList<Link> mapToJList(Collection<Link> files) {
        DefaultListModel<Link> labelJList = new DefaultListModel<>();
        files.forEach(labelJList::addElement);
        var jlist = new JList<>(labelJList);
        jlist.setCellRenderer(new FileListCellRenderer());
        return jlist;
    }

    private class FileListCellRenderer extends DefaultListCellRenderer {

        private static final long serialVersionUID = -7799441088157759804L;
        private final JLabel label;
        private final Color textSelectionColor = Color.BLACK;
        private final Color backgroundSelectionColor = Color.lightGray;
        private final Color textNonSelectionColor = Color.BLACK;
        private final Color backgroundNonSelectionColor = Color.WHITE;

        FileListCellRenderer() {
            label = new JLabel();
            label.setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean selected,
                boolean expanded) {

            Link file = (Link) value;
            if (((Link) value).isDirectory()) {
                label.setIcon(folderIcon);
            } else {
                label.setIcon(UIManager.getIcon("FileView.fileIcon"));
            }
            label.setText(file.getName());

            if (selected) {
                label.setBackground(backgroundSelectionColor);
                label.setForeground(textSelectionColor);
            } else {
                label.setBackground(backgroundNonSelectionColor);
                label.setForeground(textNonSelectionColor);
            }

            return label;
        }
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
