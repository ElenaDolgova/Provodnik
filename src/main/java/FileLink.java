import java.nio.file.Path;

public class FileLink {
    private final String name;
    private final Path path;

    public FileLink(String name, Path path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public Path getPath() {
        return path;
    }

    public boolean isDirectory() {
        return path.toFile().isDirectory();
    }

    @Override
    public String toString() {
        return getName();
    }
}
