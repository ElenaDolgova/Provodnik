import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ZipDirectory implements Directory {
    private final FileSystem fs;
    private final Path path;
    private final String probeContentType;

    public ZipDirectory(ZipFileLink displayFiles, FileSystem fs) {
        this.path = displayFiles.getPath();
        this.probeContentType = displayFiles.getProbeContentType();
        this.fs = fs;
    }

    private static Stream<Path> streamAllFiles(FileSystem fs, int depth) {
        return StreamSupport.stream(fs.getRootDirectories().spliterator(), false)
                .flatMap((it) -> walkFiles(it, depth));
    }

    private static Stream<Path> walkFiles(Path it, int depth) {
        try {
            return Files.walk(it, depth);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public List<Link> getFiles() {
        // встретили очередной zip
        if ("application/zip".equals(probeContentType)) {
            return streamAllFiles(fs, 2)
                    .filter(p -> {
                        Path parent = p.getParent();
                        return p.getNameCount() > 1 && parent != null &&
                                p.startsWith("/" + getDirectoryName().substring(0, getDirectoryName().lastIndexOf(".")));
                    })
                    .map(path -> new ZipFileLink(path, fs, false))
                    .collect(Collectors.toList());
        }
        int depth = path.getNameCount() + 1;
        return streamAllFiles(fs, depth)
                .filter(p -> p.startsWith("/" + path) && p.toString().length() > path.toString().length())
                .map(path -> new ZipFileLink(path, fs, false))
                .collect(Collectors.toList());
    }

    @Override
    public String getDirectoryName() {
        return String.valueOf(path.getFileName());
    }

    @Override
    public String toString() {
        return getDirectoryName();
    }
}
