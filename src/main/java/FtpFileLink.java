import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class FtpFileLink implements Link{
    @Override
    public Path getPath() {
        return null;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Directory createDirectory() throws IOException {
        return null;
    }

    @Override
    public InputStream getInputStreamOfFile() throws IOException {
        return null;
    }
}
