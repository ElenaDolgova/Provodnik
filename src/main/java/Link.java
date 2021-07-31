import java.io.File;
import java.nio.file.Path;

public interface Link extends Comparable<Link> {
    Path createPath();

    boolean isDirectory();

    String getName();

    File createFile();

    boolean isFile();
}
