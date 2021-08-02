import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipDirectory implements Directory {
    private final FileSystem fs;

//    private final ZipFile zipFile;
    /**
     * Имя директории
     */
    private final String directoryName;
    /**
     * Путь до файла
     */
    private final Path path;
    /**
     * Имя родителя zip файла
     */
    private final String globalParent;

    public ZipDirectory(ZipFileLink displayFiles) throws IOException {
        this.path = displayFiles.getPath();
        this.directoryName = createDirectoryName(this.path.getFileName());
        this.globalParent = null;
        this.fs = FileSystems.newFileSystem(
                Paths.get(displayFiles.getPath().toString()), Collections.emptyMap());
    }

    public ZipDirectory(ZipFileLink displayFiles, FileSystem fs, boolean isDirectory) throws IOException {
        this.path = displayFiles.getPath();
        this.directoryName = createDirectoryName(this.path.getFileName());
        this.globalParent = path.toString();
        String first = displayFiles.getPath().toString();
        this.fs = isDirectory ? fs : FileSystems.newFileSystem(fs.getPath(first), Collections.emptyMap());
    }

    private String createDirectoryName(Path path) {
        return String.valueOf(path.getFileName());
    }

    private static void printAllFiles(FileSystem fs) {
        StreamSupport.stream(fs.getRootDirectories().spliterator(), false)
                .flatMap((it) -> {
                    try {
                        return Files.walk(it, 2);
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                }).forEach(p -> {
            System.out.println(p);
            System.out.println(p.getFileName());
        });
    }

    private static Stream<Path> streamAllFiles(FileSystem fs, int depth) {
        return StreamSupport.stream(fs.getRootDirectories().spliterator(), false)
                .flatMap((it) -> {
                    try {
                        System.out.println("it " + it);
                        return Files.walk(it, depth);
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                });
    }

    private void printfAllFiles(java.nio.file.FileSystem fs) {
        StreamSupport.stream(fs.getRootDirectories().spliterator(), false)
                .flatMap((it) -> {
                    try {
//                        return Files.walk(it);
                        return Files.walk(it, 2);
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                }).forEach(System.out::println);
    }

    @Override
    public List<Link> getFiles() {
        int depth = 0;
        if (globalParent == null) {
            depth = 2;
        } else {
            depth = 1;
            Path curPath = path;
            while (curPath.getParent() != null) {
                curPath = curPath.getParent();
                ++depth;
            }
        }
        List<Link> files = new ArrayList<>();
        // значит zip внутри zip
        if (globalParent != null) {
            System.out.println("\nzip in zip " + depth);
            streamAllFiles(fs, depth)
                    .forEach(p -> {
                        System.out.println(p);
                        System.out.println(String.valueOf(p.getFileName()));
                        // todo как понять, что это директория?
                        System.out.println(Files.isDirectory(p));
                        File file = new File(p.toString());
                        System.out.println(file.isDirectory());
                        File file1 = new File(String.valueOf(p.getFileName()));
                        System.out.println(file1.isDirectory());
                        files.add(new ZipFileLink(p, file, fs));
                    });
            return files;
        } else {
            System.out.println("\nppp" + depth);
            streamAllFiles(fs, depth)
                    .forEach(p -> {
                        System.out.println(p);
                        System.out.println(p.getFileName());
                        System.out.println(Files.isDirectory(p));
                        File file = new File(p.toString());
                        System.out.println(file.isDirectory());
                        File file1 = new File(String.valueOf(p.getFileName()));
                        System.out.println(file1.isDirectory());
                        files.add(new ZipFileLink(p, file, fs));
                    });
            return files;
        }
    }

    public String getDirectoryName() {
        return directoryName;
    }

    @Override
    public String toString() {
        return getDirectoryName();
    }
}
