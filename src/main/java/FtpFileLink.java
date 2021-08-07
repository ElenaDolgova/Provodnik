import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class FtpFileLink implements Link {
    private final FTPClient ftpClient;
    private final FTPFile ftpFile;
    private final String path;

    public FtpFileLink(FTPClient ftpClient, String path, FTPFile ftpFile) {
        this.ftpClient = ftpClient;
        this.path = path;
        this.ftpFile = ftpFile;
    }

    @Override
    public Path getPath() {
        return Paths.get(path);
    }

    @Override
    public Directory createDirectory() {
        return new FTPDirectory(ftpClient, path, getName());
    }

    @Override
    public boolean isDirectory() {
        return ftpFile.isDirectory();
    }

    @Override
    public String getName() {
        return ftpFile.getName();
    }

    @Override
    public void processFile(Consumer<InputStream> consumer) {
        try (InputStream in = ftpClient.retrieveFileStream(path)) {
            consumer.accept(in);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                ftpClient.completePendingCommand();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getProbeContentType() {
        return Link.getProbeContentType(Paths.get(path));
    }

    public static String getFtpPath(String path, String name) {
        String newPath;
        if (path.equals("/")) {
            newPath = path + name;
        } else {
            newPath = path + "/" + name;
        }
        return newPath;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int compareTo(Link o) {
        if (o == null) {
            return 1;
        }
        return getName().compareTo(o.getName());
    }
}
