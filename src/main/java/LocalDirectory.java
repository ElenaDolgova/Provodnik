import javax.swing.*;
import java.io.File;
import java.nio.file.Path;
import java.util.*;

/**
 * Класс описывает один элемент на табе с директориями {@link DirectoryScrollPane}
 */
public final class LocalDirectory implements Directory {
    /**
     * Имя диреткории
     */
    private final String directoryName;
    /**
     * Путь до файла
     */
    private final Path path;

    public LocalDirectory(Path path) {
        this.path = path;
        this.directoryName = createDirectoryName(path);
    }

    public LocalDirectory(Link localFileLink) {
        this.path = localFileLink.createPath();
        this.directoryName = createDirectoryName(this.path);
    }

    @Override
    public String getDirectoryName() {
        return directoryName;
    }

    /**
     * Метод создает имя, отображаемой директории на панели с текущими диреткориями
     * Если path - это корневая директория, то path.getFileName() возвращает null.
     */
    private static String createDirectoryName(Path path) {
        return path.getFileName() == null ? "/" : path.getFileName() + "/";
    }

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
        File file = path.toFile();
        if (file.isDirectory()) {
            //todo тест кейсБ а когда file.listFiles() мб null?
            // когда не диреткория
            SortedSet<LocalFileLink> sortedFiles = new TreeSet<>();
            Arrays.stream(Objects.requireNonNull(file.listFiles()))
                    .map(LocalFileLink::new)
                    .forEach(sortedFiles::add);
            sortedFiles.forEach(labelJList::addElement);
        }
        return new JList<>(labelJList);
    }

    @Override
    public String toString() {
        return getDirectoryName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalDirectory that = (LocalDirectory) o;
        return directoryName.equals(that.directoryName) && path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(directoryName, path);
    }
}
