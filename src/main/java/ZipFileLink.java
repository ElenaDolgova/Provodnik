import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Collections;

public class ZipFileLink implements Link {
    private final boolean isFirstZip;

    private final FileSystem fs;

    private final Path path;

    public ZipFileLink(Path path, FileSystem fs, boolean isFirstZip) {
        this.fs = fs;
        this.path = path;
        this.isFirstZip = isFirstZip;
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public boolean isDirectory() {
        return Files.isDirectory(path);
    }

    @Override
    public InputStream getInputStreamOfFile() throws IOException {
        byte[] jpeg = Files.readAllBytes(fs.getPath(path.toString()));
        return new ByteArrayInputStream(jpeg);
    }

    @Override
    public Directory createDirectory() throws IOException {
        String probeContentType = Link.getProbeContentType(getPath());
        if (probeContentType != null) {
            if ("application/zip".equals(probeContentType)) {
                if (isFirstZip) {
                    // создается просто самый первый zip
                    FileSystem newFileSystem =
                            FileSystems.newFileSystem(fs.getPath(path.toString()), Collections.emptyMap());
                    return new ZipDirectory(this, newFileSystem);
                } else {
                    //  zip внутри zip Создается новая файловая подсистема
                    FileSystem newFileSystem =
                            FileSystems.newFileSystem(fs.getPath(path.toString()), Collections.emptyMap());
                    return new ZipDirectory(this, newFileSystem);
                }
            }
        } else if (isDirectory()) {
            // когда директория внутри zip
            return new ZipDirectory(this, fs);
        }
        return null;
    }

    @Override
    public String getName() {
        return path.getFileName().toString();
    }

    @Override
    public String toString() {
        return getName();
    }
}
