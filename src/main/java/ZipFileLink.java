import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
        return null;
//        return zipFile.getInputStream(getZipEntry());
    }

    @Override
    public Directory createDirectory() throws IOException {
        String probeContentType = getProbeContentType();
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

    public String getProbeContentType() {
        String probeContentType = null;
        try {
            probeContentType = Files.probeContentType(getPath());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return probeContentType;
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
