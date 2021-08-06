import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPListParseEngine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    public void getFiles(Consumer<List<? extends Link>> action, String ext) {
        try {
            FTPListParseEngine engine = ftpClient.initiateListParsing(path);
            while (engine.hasNext()) {
                List<Link> batch = new ArrayList<>();
                FTPFile[] files = engine.getNext(300);
                for (FTPFile file : files) {
                    if (ext == null || StringUtils.isBlank(ext)) {
                        batch.add(new FtpFileLink(ftpClient, FtpFileLink.getFtpPath(path, file.getName()), file));
                        continue;
                    }
                    boolean hasSuitableExtension;
                    if (ext.contains(".")) {
                        hasSuitableExtension = file.getName().endsWith(ext);
                    } else {
                        hasSuitableExtension = file.getName().endsWith("." + ext);
                    }
                    if (hasSuitableExtension) {
                        batch.add(new FtpFileLink(ftpClient, FtpFileLink.getFtpPath(path, file.getName()), file));
                    }
                }
                action.accept(batch);
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
