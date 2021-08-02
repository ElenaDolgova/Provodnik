import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class LocalFileLink implements Link {
    private final File file;
    private final String name;

    public LocalFileLink(File file) {
        this.file = file;
        this.name = file.getName();
    }

    @Override
    public Directory createDirectory() {
        Directory newDirectory = null;
        if (isDirectory()) {
            newDirectory = new LocalDirectory(this);
        }
        return newDirectory;
    }

    @Override
    public InputStream getInputStreamOfFile() throws IOException {
        return new FileInputStream(file);
    }

    @Override
    public String getProbeContentType() {
        String type = null;
        try {
            type = Files.probeContentType(getPath());
            return type;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isDirectory() {
        return file.isDirectory();
    }


    @Override
    public Path getPath() {
        return file.toPath();
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
