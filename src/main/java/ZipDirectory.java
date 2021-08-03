import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ZipDirectory implements Directory {
    private final FileSystem fs;
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

    private final String probeContentType;

    public ZipDirectory(ZipFileLink displayFiles) throws IOException {
        this.path = displayFiles.getPath();
        this.directoryName = createDirectoryName(this.path.getFileName());
        this.globalParent = null;
        this.fs = FileSystems.newFileSystem(
                Paths.get(displayFiles.getPath().toString()), Collections.emptyMap());
        this.probeContentType = Link.getProbeContentType(path);
    }

    public ZipDirectory(ZipFileLink displayFiles, FileSystem fs, boolean isDirectory) throws IOException {
        this.path = displayFiles.getPath();
        this.directoryName = createDirectoryName(this.path.getFileName());
        this.globalParent = path.toString();
        String first = displayFiles.getPath().toString();
        this.fs = isDirectory ? fs : FileSystems.newFileSystem(fs.getPath(first), Collections.emptyMap());
        this.probeContentType = Link.getProbeContentType(path);
    }

    private String createDirectoryName(Path path) {
        return String.valueOf(path.getFileName());
    }

    private static Stream<Path> streamAllFiles(FileSystem fs, int depth) {
        return StreamSupport.stream(fs.getRootDirectories().spliterator(), false)
                .flatMap((it) -> {
                    try {
                        return Files.walk(it, depth);
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                });
    }

    @Override
    public List<Link> getFiles() {
        List<Link> files = new ArrayList<>();
        // встретили очередной zip
        if ("application/zip".equals(probeContentType)) {
            streamAllFiles(fs, 2)
                    .filter(p -> {
                        Path parent = p.getParent();
                        return p.getNameCount() > 1 && parent != null &&
                                p.startsWith("/" + getDirectoryName().substring(0, getDirectoryName().lastIndexOf(".")));
                    })
                    .forEach(p -> {
                        File file = new File(p.toString());
                        files.add(new ZipFileLink(p, file, fs));
                    });
        } else if (globalParent != null) {
            int depth = path.getNameCount() + 1;
            streamAllFiles(fs, depth)
                    .filter(p -> p.startsWith("/" + path) && p.toString().length() > path.toString().length())
                    .forEach(p -> {
                        File file = new File(p.toString());
                        files.add(new ZipFileLink(p, file, fs));
                    });
        }
        return files;
    }

    public String getDirectoryName() {
        return directoryName;
    }

    @Override
    public String toString() {
        return getDirectoryName();
    }
}
