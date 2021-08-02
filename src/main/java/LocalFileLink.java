import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
    public void invoke(Renderer renderer) {
        if (isDirectory()) {
            Directory newDirectory = new LocalDirectory(this);
            FilesScrollPane.addNewDirectory(newDirectory, renderer.getDIRECTORY_SCROLL_PANE(), renderer);
            return;
        }

        try {
            String probeContentType = Files.probeContentType(getPath());
            renderer.getPREVIEW_PANEL().update(probeContentType, new FileInputStream(file));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
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
