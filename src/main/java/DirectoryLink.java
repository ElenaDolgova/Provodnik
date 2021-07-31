import javax.swing.*;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

/**
 * Класс описывает один элемент на табе с директориями {@link DirectoryScrollPane}
 */
public class DirectoryLink {
    /**
     * Имя диреткории
     */
    private final String directoryName;
    /**
     * Путь до файла
     */
    private final Path path;

    public DirectoryLink(Path path) {
        this.path = path;
        this.directoryName = createDirectoryName(path);
    }

    public String getFullPath() {
        return path.toString();
    }

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

    /**
     * метод отвечает за отображение файлов на панели с файлами {@link FilesScrollPane}
     */
    public JList<FileLink> getDirectoryFiles() {
        DefaultListModel<FileLink> labelJList = new DefaultListModel<>();

        //todo на правой панельке отобразить содержимое папки; обратить внимание на файловую систему. Она должна быть разной для мак оси и винды
        System.out.println("I am here " + directoryName);

        File file = path.toFile();
        if (file.isDirectory()) {
            Arrays.stream(Objects.requireNonNull(file.listFiles()))
                    .forEach(p -> {
                        System.out.println(p.toString());
                        labelJList.addElement(new FileLink(p.getName(), p.toPath()));
                    });
        }
        return new JList<>(labelJList);
    }

    public void updateFilesScrollPane() {
        JList<FileLink> displayFiles = getDirectoryFiles();
        MainFrame.FILES_SCROLL_PANE.getScrollPane().setViewportView(displayFiles);
        displayFiles.addMouseListener(FilesScrollPane.getMouseListener());
    }

    @Override
    public String toString() {
        return getDirectoryName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DirectoryLink that = (DirectoryLink) o;
        return directoryName.equals(that.directoryName) && path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(directoryName, path);
    }
}
