import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public interface Link {
    Path getPath();

    boolean isDirectory();

    String getName();

    Directory createDirectory() throws IOException;

    InputStream getInputStreamOfFile() throws IOException;

    default String getProbeContentType() {
        String probeContentType = null;
        try {
            probeContentType = Files.probeContentType(getPath());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return probeContentType;
    }
}
