import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

public record LocalFileLink(FileSystem fs, Path path) implements Link {

    @Override
    public Directory createDirectory() {
        return new LocalDirectory(fs, path);
    }

    @Override
    public InputStream getInputStreamOfFile() throws IOException {
        byte[] bytes = Files.readAllBytes(fs.getPath(path.toString()));
        return new ByteArrayInputStream(bytes);
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
}
