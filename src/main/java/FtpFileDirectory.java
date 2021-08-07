import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPListParseEngine;

import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FtpFileDirectory implements Directory {
    private final FTPClient ftpClient;
    private final FTPFile ftpFile;
    private final String path;

    //https://www.mmnt.net/
    public FtpFileDirectory(FTPClient ftpClient, String path, FTPFile ftpFile) {
        this.ftpClient = ftpClient;
        this.path = path;
        this.ftpFile = ftpFile;
    }

    @Override
    public void getFiles(Consumer<List<? extends Directory>> action, String ext) {
        try {
            FTPListParseEngine engine = ftpClient.initiateListParsing(path);
            while (engine.hasNext()) {
                List<Directory> batch = new ArrayList<>();
                FTPFile[] files = engine.getNext(300);
                for (FTPFile file : files) {
                    if (file != null) {
                        if (ext == null || ext.length() == 0) {
                            batch.add(new FtpFileDirectory(ftpClient, FtpFileDirectory.getFtpPath(path, file.getName()), file));
                            continue;
                        }
                        boolean hasSuitableExtension;
                        if (ext.contains(".")) {
                            hasSuitableExtension = file.getName().endsWith(ext);
                        } else {
                            hasSuitableExtension = file.getName().endsWith("." + ext);
                        }
                        if (hasSuitableExtension) {
                            batch.add(new FtpFileDirectory(ftpClient, FtpFileDirectory.getFtpPath(path, file.getName()), file));
                        }
                    }
                }
                action.accept(batch);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Path getPath() {
        return Paths.get(path);
    }

    @Override
    public Directory createDirectory() {
        if ("application/zip".equals(getProbeContentType())) {
            try {
                File file = File.createTempFile("tmp", ".zip");
                file.deleteOnExit();
                try (OutputStream outputStream = new FileOutputStream(file)) {
                    downloadFile(outputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                FileSystem newFileSystem = FileSystems.newFileSystem(file.toPath(), null);
                // тут есть фишка с null на path
                return new ZipFileDirectory(file.toPath(), newFileSystem, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new FtpFileDirectory(ftpClient, path, ftpFile);
    }

    public void downloadFile(OutputStream os) {
        try {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);
            ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
            ftpClient.retrieveFile(path, os);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean isDirectory() {
        return ftpFile.isDirectory() || "application/zip".equals(getProbeContentType());
    }

    @Override
    public String getName() {
        //todo кодировка на кириллицу
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
        return Directory.getProbeContentType(Paths.get(path));
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
    public int compareTo(Directory o) {
        if (o == null) {
            return 1;
        }
        return getName().compareTo(o.getName());
    }
}
