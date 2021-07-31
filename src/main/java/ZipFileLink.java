import java.io.File;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipFileLink implements Link {
    private final ZipFile zipFile;
    private final ZipEntry entry;
    private final String name;
    private final File file;

    public ZipFileLink(ZipFile zipFile, ZipEntry entry, File file) {
        this.zipFile = zipFile;
        this.entry = entry;
        this.name = file.getName();
        this.file = file;
    }

    public String getParent() {
        return file.getParent();
    }

    @Override
    public Path createPath() {
        return file.toPath();
    }

    @Override
    public boolean isDirectory() {
        return entry.isDirectory();
    }

    @Override
    public boolean isFile() {
        return !entry.isDirectory();
    }

    @Override
    public String getName() {
        return file.getName();
    }

    public ZipFile getZipFile() {
        return zipFile;
    }

    @Override
    public File createFile() {
        return file;
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
