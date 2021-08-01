import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public final class LocalFileLink implements Link {
    private final File file;
    private final String name;

    public LocalFileLink(File file) {
        this.file = file;
        this.name = file.getName();
    }


    /**
     * @return если не изображение, то null
     */
    @Override
    public Image getImage() {
        // todo долго читается? почему подтормаживает экран?
        Image image = null;
        try {
            image = ImageIO.read(file);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return image;
    }

    @Override
    public String getName() {
        return name;
    }

    public File getFile() {
        return file;
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
