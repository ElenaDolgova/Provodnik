import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class LocalFileLink implements Link {

    private final FileSystem fs;
    private final Path path;

    public LocalFileLink(FileSystem fs, Path path) {
        this.fs = fs;
        this.path = path;
    }

    @Override
    public Directory createDirectory() {
        return new LocalDirectory(fs, path);
    }

    @Override
    public void processFile(Consumer<InputStream> consumer) throws IOException {
        byte[] bytes = Files.readAllBytes(fs.getPath(path.toString()));
        consumer.accept(new ByteArrayInputStream(bytes));
    }

    @Override
    public String getName() {
        Path fileName = path.getFileName();
        if (fileName == null) {
            return null;
        }
        return fileName.toString();
    }

    @Override
    public boolean isDirectory() {
        return path.toFile().isDirectory();
    }


    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int compareTo(Link o) {
        if (o == null) {
            return 1;
        } else if (getName() == null) {
            return -1;
        }
        return getName().compareTo(o.getName());
    }
}
