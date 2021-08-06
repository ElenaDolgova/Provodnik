import java.nio.file.*;
import java.util.function.Consumer;

public class ZipDirectory implements Directory {
    private final FileSystem fs;
    private final Path path;
    private final String probeContentType;

    public ZipDirectory(ZipFileLink displayFiles, FileSystem fs) {
        this.path = displayFiles.getPath();
        this.probeContentType = displayFiles.getProbeContentType();
        this.fs = fs;
    }

    @Override
    public void getFiles(Consumer<Link> action, String ext) {
        if ("application/zip".equals(probeContentType)) {
            Directory.streamAllFiles(fs, 2)
                    .filter(p -> {
                        if (ext == null || ext.length() == 0) return true;
                        return ext.equals(Directory.getExtension(p));
                    })
                    .filter(p -> {
                        Path parent = p.getParent();
                        return p.getNameCount() > 1 && parent != null &&
                                p.startsWith("/" + getDirectoryName().substring(0, getDirectoryName().lastIndexOf(".")));
                    })
                    .map(path -> new ZipFileLink(path, fs, false))
                    .sorted()
                    .forEach(action);
        }
        int depth = path.getNameCount() + 1;
        Directory.streamAllFiles(fs, depth)
                .filter(p -> p.startsWith("/" + path))
                .filter(p -> ext == null || ext.length() == 0 || p.endsWith(ext))
                .filter(p -> p.getNameCount() > path.getNameCount())
                .map(path -> new ZipFileLink(path, fs, false))
                .forEach(action);
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
