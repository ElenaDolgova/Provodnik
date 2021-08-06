import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.*;
import java.util.function.Consumer;

/**
 * Класс описывает один элемент на табе с директориями {@link DirectoryScrollPane}
 */
public class LocalDirectory implements Directory {

    private final FileSystem fs;
    private final Path path;

    public LocalDirectory(FileSystem fs, Path path) {
        this.fs = fs;
        this.path = path;
    }

    @Override
    public void getFiles(Consumer<Link> action, String ext) {
        Directory.walkFiles(path, 1)
                .filter(p -> p.getNameCount() == path.getNameCount() + 1)
                .filter(p -> ext == null || StringUtils.isBlank(ext) || ext.equals(Directory.getExtension(p)))
                .map(this::getLink)
                .sorted()
                .forEach(action);
    }

    private Link getLink(Path path) {
        try {
            if ("application/zip".equals(Link.getProbeContentType(path))) {
                FileSystem fs = FileSystems.newFileSystem(path, null);
                return new ZipFileLink(path, fs, true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new LocalFileLink(fs, path);
    }

    @Override
    public String getDirectoryName() {
        if (path.getFileName() == null) {
            return path.toString();
        }
        return path.getFileName().toString();
    }

    @Override
    public String toString() {
        return getDirectoryName();
    }
}
