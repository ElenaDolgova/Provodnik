import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

public final class FileLink implements Comparable<FileLink> {
    private final File file;

    public FileLink(File file) {
        this.file = file;
    }

    public String getName() {
        return file.getName();
    }

    public Path getPath() {
        return file.toPath();
    }

    public File getFile() {
        return file;
    }

    public boolean isDirectory() {
        return file.isDirectory();
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int compareTo(FileLink o) {
        return file.getName().compareTo(o.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileLink fileLink = (FileLink) o;
        return Objects.equals(file, fileLink.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file);
    }
}
