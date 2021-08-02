import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public interface Link {
    Path getPath();

    boolean isDirectory();

    String getName();

    Directory createDirectory() throws IOException;

    String getProbeContentType();

    InputStream getInputStreamOfFile() throws IOException;
}
