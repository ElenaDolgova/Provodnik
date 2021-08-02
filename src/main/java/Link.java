import java.awt.*;
import java.io.File;
import java.nio.file.Path;

public interface Link extends Comparable<Link> {
    Path getPath();

    boolean isDirectory();

    String getName();

    /**
     * Метод вызывается при нажатии на определенный компонент в каталоге
     */
    void invoke(Renderer renderer);
}
