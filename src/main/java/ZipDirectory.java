//import org.apache.commons.lang3.StringUtils;

import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

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
    public Collection<Link> getFiles() {
        return downloadFiles(null);
    }

    @Override
    public Collection<Link> getFiles(String ext) {
        return downloadFiles(ext);
    }

    private Collection<Link> downloadFiles(String ext) {
        if ("application/zip".equals(probeContentType)) {
            return Directory.streamAllFiles(fs, 2)
                    .filter(p -> {
                        if (ext == null || ext.length() ==0) return true;
                        return ext.equals(Directory.getExtension(p));
                    })
                    .filter(p -> {
                        Path parent = p.getParent();
                        return p.getNameCount() > 1 && parent != null &&
                                p.startsWith("/" + getDirectoryName().substring(0, getDirectoryName().lastIndexOf(".")));
                    })
                    .map(path -> new ZipFileLink(path, fs, false))
                    .sorted()
                    .collect(Collectors.toList());
        }
        int depth = path.getNameCount() + 1;
        return Directory.streamAllFiles(fs, depth)
                .filter(p -> p.startsWith("/" + path))
                .filter(p -> ext == null || ext.length() ==0 || p.endsWith(ext))
                .filter(p -> p.getNameCount() > path.getNameCount())
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
