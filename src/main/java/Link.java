import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public interface Link extends Comparable<Link> {
    Path getPath();

    boolean isDirectory();

    String getName();

//    /**
//     * Метод вызывается при нажатии на определенный компонент в каталоге
//     */
//    void invoke(Renderer renderer);

    Directory createDirectory() throws IOException;

    String getProbeContentType();

    InputStream getInputStreamOfFile() throws IOException;
}
