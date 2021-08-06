import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPListParseEngine;

import java.io.IOException;
import java.util.function.Consumer;

public class FTPDirectory implements Directory {
    private final FTPClient ftpClient;
    private final String path;
    private final String name;

    //https://www.mmnt.net/
    public FTPDirectory(FTPClient ftpClient, String path, String name) {
        this.ftpClient = ftpClient;
        this.path = path;
        this.name = name;
    }

    @Override
    public void getFiles(Consumer<Link> action, String ext) {
        try {
            FTPListParseEngine engine = ftpClient.initiateListParsing(path);
            while (engine.hasNext()) {
                FTPFile[] files = engine.getNext(25);
                for (FTPFile file : files) {
                    if (ext == null || StringUtils.isBlank(ext)) {
                        action.accept(new FtpFileLink(ftpClient, FtpFileLink.getFtpPath(path, file.getName()), file));
                        continue;
                    }
                    boolean hasSuitableExtension;
                    if (ext.contains(".")) {
                        hasSuitableExtension = file.getName().endsWith(ext);
                    } else {
                        hasSuitableExtension = file.getName().endsWith("." + ext);
                    }
                    if (hasSuitableExtension) {
                        action.accept(new FtpFileLink(ftpClient, FtpFileLink.getFtpPath(path, file.getName()), file));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getDirectoryName() {
        return name;
    }

    @Override
    public String toString() {
        return getDirectoryName();
    }
}
