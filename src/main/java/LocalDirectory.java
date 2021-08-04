import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Класс описывает один элемент на табе с директориями {@link DirectoryScrollPane}
 */
public record LocalDirectory(FileSystem fs, Path path) implements Directory {

    @Override
    public Collection<Link> getFiles() {
        return downloadFiles(null);
    }

    @Override
    public Collection<Link> getFiles(String ext) {
        return downloadFiles(ext);
    }

    private Collection<Link> downloadFiles(String ext) {
        return Directory.walkFiles(path, 1)
                .filter(p -> p.getNameCount() == path.getNameCount() + 1)
                .filter(p -> ext == null || StringUtils.isBlank(ext) || ext.equals(Directory.getExtension(p)))
                .map(this::getLink)
                .sorted()
                .collect(Collectors.toList());
    }

    private Link getLink(Path path) {
        try {
            if ("application/zip".equals(Link.getProbeContentType(path))) {
                FileSystem fs = FileSystems.newFileSystem(path, Collections.emptyMap());
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
