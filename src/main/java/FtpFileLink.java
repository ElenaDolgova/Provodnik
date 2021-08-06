import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

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
    public InputStream getInputStreamOfFile() {
        try {
            return ftpClient.retrieveFileStream(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
