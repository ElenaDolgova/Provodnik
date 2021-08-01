import java.awt.*;
import java.io.File;
import java.nio.file.Path;

public interface Link extends Comparable<Link> {
    Path createPath();

    boolean isDirectory();

    String getName();

    File createFile();

    File getFile();

    Image getImage();

    /**
     * Метод вызывается при нажатии на определенный компонент в каталоге
     */
    void invoke();
}
