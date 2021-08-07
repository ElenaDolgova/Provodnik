package model;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static model.ZipFileDirectory.isZip;

public class FtpFileDirectory implements Directory {
    private static final Map<String, File> cachedZipFile = new HashMap<>();

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
    public void getFiles(Consumer<List<? extends Directory>> batchAction, String ext) {
        try {
            FTPListParseEngine engine = ftpClient.initiateListParsing(path);
            while (engine.hasNext()) {
                List<Directory> batch = new ArrayList<>();
                FTPFile[] files = engine.getNext(300);
                for (FTPFile file : files) {
                    if (file != null && isSuitableForAdding(ext, file)) {
                        batch.add(new FtpFileDirectory(ftpClient, getFtpPath(file.getName()), file));
                    }
                }
                batchAction.accept(batch);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isSuitableForAdding(String ext, FTPFile file) {
        return ext == null || ext.length() == 0 || hasSuitableExtension(ext, file);
    }

    private boolean hasSuitableExtension(String ext, FTPFile file) {
        if (ext.contains(".")) {
            return file.getName().endsWith(ext);
        } else {
            return file.getName().endsWith("." + ext);
        }
    }

    @Override
    public Path getPath() {
        return Paths.get(path);
    }

    @Override
    public Directory createDirectory() throws IOException {
        if (isZip(getPath())) {
            File file = getFileFromCacheOrDownload();
            try {
                FileSystem newFileSystem = FileSystems.newFileSystem(file.toPath(), null);
                // тут есть фишка с null на path
                return new ZipFileDirectory(file.toPath(), newFileSystem, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new FtpFileDirectory(ftpClient, path, ftpFile);
    }

    private File getFileFromCacheOrDownload() throws IOException {
        File file;
        if (cachedZipFile.containsKey(path)) {
            file = cachedZipFile.get(path);
        } else {
            file = File.createTempFile("tmp", ".zip");
            file.deleteOnExit();
            try (OutputStream outputStream = new FileOutputStream(file)) {
                downloadFile(outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            cachedZipFile.put(path, file);
        }
        return file;
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
        return ftpFile.isDirectory() || isZip(getPath());
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

    public String getFtpPath(String name) {
        String newPath;
        if ("/".equals(path)) {
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
