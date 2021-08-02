import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipFile;

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
        this.path = localFileLink.getPath();
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

    @Override
    public List<Link> getFiles() {
        File selectedFile = path.toFile();
        //todo тест кейс, а когда file.listFiles() мб null?
        // когда не диреткория
        List<Link> files = new ArrayList<>();
        if (selectedFile.isDirectory()) {
            Arrays.stream(Objects.requireNonNull(selectedFile.listFiles()))
                    .map(file -> {
                        try {
                            if ("application/zip".equals(Files.probeContentType(file.toPath()))) {
                                FileSystem fs = FileSystems.newFileSystem(
                                        Paths.get(file.toPath().toString()), Collections.emptyMap());
                                return new ZipFileLink(null, file, fs);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return new LocalFileLink(file);
                    })
                    .forEach(files::add);
        }
        return files;
    }

    @Override
    public String toString() {
        return getDirectoryName();
    }
}
