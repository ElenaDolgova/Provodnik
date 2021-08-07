import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class ZipFileLink implements Link {
    private final Path path;
    private final FileSystem fs;
    private final boolean isFirstZip;

    public ZipFileLink(Path path, FileSystem fs, boolean isFirstZip) {
        this.path = path;
        this.fs = fs;
        this.isFirstZip = isFirstZip;
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public boolean isDirectory() {
        return Files.isDirectory(path) || "application/zip".equals(getProbeContentType());
    }

    @Override
    public void processFile(Consumer<InputStream> consumer) throws IOException {
        byte[] bytes = Files.readAllBytes(fs.getPath(path.toString()));
        consumer.accept(new ByteArrayInputStream(bytes));
    }

    @Override
    public Directory createDirectory() throws IOException {
        String probeContentType = getProbeContentType();
        if ("application/zip".equals(probeContentType)) {
            if (isFirstZip) {
                // создается просто самый первый zip
                FileSystem newFileSystem =
                        FileSystems.newFileSystem(path, null);
                return new ZipDirectory(this, newFileSystem);
            } else {
                //  zip внутри zip Создается новая файловая подсистема
                FileSystem newFileSystem =
                        FileSystems.newFileSystem(fs.getPath(path.toString()), null);
                return new ZipDirectory(this, newFileSystem);
            }
        }
        // когда директория внутри zip
        return new ZipDirectory(this, fs);
    }

    @Override
    public String getName() {
        return path.getFileName().toString();
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int compareTo(Link o) {
        if (o == null) {
            return 1;
        }
        return getName().compareTo(o.getName());
    }
}
