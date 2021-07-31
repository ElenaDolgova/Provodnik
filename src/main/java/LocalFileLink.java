import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

public final class LocalFileLink implements Link {
    private final File file;
    private final String name;

    public LocalFileLink(File file) {
        this.file = file;
        this.name = file.getName();
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean isDirectory() {
        return file.isDirectory();
    }

    @Override
    public Path createPath() {
        return file.toPath();
    }

    @Override
    public File createFile() {
        return file;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalFileLink that = (LocalFileLink) o;
        return Objects.equals(file, that.file) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, name);
    }

    @Override
    public int compareTo(Link o) {
        if (o == null) {
            return 1;
        }
        return name.compareTo(o.getName());
    }
}
