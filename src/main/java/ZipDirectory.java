import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipDirectory implements Directory {
    /**
     * Имя диреткории
     */
    private final String directoryName;
    /**
     * Путь до файла
     */
    private final Path path;

    private final ZipFile zipFile;
    /**
     * Имя родителя zip файла
     */
    private final String globalParent;

    public ZipDirectory(Link displayFiles) throws IOException {
        this.path = displayFiles.getPath();
        this.zipFile = new ZipFile(displayFiles.getFile());
        this.directoryName = createDirectoryName(this.path.getFileName());
        this.globalParent = null;
    }

    public ZipDirectory(ZipFileLink displayFiles, ZipFile zipFile) {
        this.path = displayFiles.getPath();
        this.zipFile = zipFile;
        this.directoryName = createDirectoryName(this.path.getFileName());
        this.globalParent = displayFiles.getParent();
    }

    private String createDirectoryName(Path path) {
        return String.valueOf(path.getFileName());
    }

    @Override
    public void updateFilesScrollPane() {
        JList<Link> displayFiles = getDirectoryFiles();
        MainFrame.FILES_SCROLL_PANE.getScrollPane().setViewportView(displayFiles);
        displayFiles.addMouseListener(FilesScrollPane.getMouseListener());
    }

    /**
     * метод отвечает за отображение файлов на панели с файлами {@link FilesScrollPane}
     */
    private JList<Link> getDirectoryFiles() {
        DefaultListModel<Link> labelJList = new DefaultListModel<>();
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        SortedSet<Link> sortedFiles = new TreeSet<>();
        String currentGlobalParent = null;
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String entryName = entry.getName();
            File file = new File(entryName);
            if (globalParent == null) {
                if (file.getParent() == null) {
                    currentGlobalParent = file.getName();
                } else if (file.getParent().equals(currentGlobalParent)) {
                    sortedFiles.add(new ZipFileLink(zipFile, entryName, file));
                }
            } else {
                if (path.toString().equals(file.getParent())) {
                    sortedFiles.add(new ZipFileLink(zipFile, entryName, file));
                }
            }
        }
        sortedFiles.forEach(labelJList::addElement);
        return new JList<>(labelJList);
    }

    public String getDirectoryName() {
        return directoryName;
    }

    @Override
    public String toString() {
        return getDirectoryName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ZipDirectory that = (ZipDirectory) o;
        return Objects.equals(directoryName, that.directoryName) &&
                Objects.equals(path, that.path) &&
                Objects.equals(zipFile, that.zipFile) &&
                Objects.equals(globalParent, that.globalParent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(directoryName, path, zipFile, globalParent);
    }
}
