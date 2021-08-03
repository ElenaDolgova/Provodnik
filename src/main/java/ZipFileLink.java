import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

public class ZipFileLink implements Link {
    /**
     * Имя entry, по которому можно найти ZipEntry в zipFile
     */
    private final String entryName;
    /**
     * Имя файла
     */
    private final File file;

    private final FileSystem fs;

    private final Path path;

    public ZipFileLink(Path path, File file, FileSystem fs) {
        this.entryName = path == null ? null : String.valueOf(path.getFileName());
        this.file = file;
        this.fs = fs;
        this.path = path;
    }

    @Override
    public Path getPath() {
        return file.toPath();
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
                // todo zip внутри zip не работает
                if (entryName == null) {
                    // создается просто zip
                    return new ZipDirectory(this);
                } else {
                    //  файл ИЛИ zip внутри zip
                    return new ZipDirectory(this, fs, false);
                }
            }
        } else if (isDirectory()) {
            // когда директория внутри zip
            return new ZipDirectory(this, fs, true);
        }
        return null;
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public String toString() {
        return getName();
    }
}
