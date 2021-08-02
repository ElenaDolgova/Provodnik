import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipFileLink implements Link {
    private final ZipFile zipFile;
    /**
     * Имя entry, по которому можно найти ZipEntry в zipFile
     */
    private final String entryName;
    /**
     * Имя файла
     */
    private final String name;
    private final File file;

    public ZipFileLink(ZipFile zipFile, String entryName, File file) {
        this.zipFile = zipFile;
        this.entryName = entryName;
        this.name = file.getName();
        this.file = file;
    }

    public String getParent() {
        return file.getParent();
    }

    @Override
    public Path getPath() {
        return file.toPath();
    }

    @Override
    public boolean isDirectory() {
        return getZipEntry().isDirectory();
    }

    public ZipEntry getZipEntry() {
        return zipFile.getEntry(entryName);
    }

    @Override
    public InputStream getInputStreamOfFile() throws IOException {
        return zipFile.getInputStream(getZipEntry());
    }

    @Override
    public Directory createDirectory() throws IOException {
        String probeContentType = getProbeContentType();
        if (probeContentType != null) {
            if ("application/zip".equals(probeContentType)) {
                // todo zip внутри zip не работает
                return new ZipDirectory(this);
            }
        } else if (isDirectory()) {
            return new ZipDirectory(this, getZipFile());
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

    public ZipFile getZipFile() {
        return zipFile;
    }

    @Override
    public int compareTo(Link o) {
        if (o == null) {
            return 1;
        }
        return name.compareTo(o.getName());
    }

    @Override
    public String toString() {
        return getName();
    }
}
