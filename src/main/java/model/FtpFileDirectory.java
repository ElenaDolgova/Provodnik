package model;

import exception.FileProcessingException;
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

public class FtpFileDirectory implements Directory {
    private static final int FTP_BATCH_SIZE = 300;
    /**
     * Закешированные скачанные zip-архивы с ftp-серверов.
     * Если пользователь решил заново открыть уже до этого скачанный файл, то качать заново его не придется,
     * он будет взят из кеша.
     */
    private static final Map<String, File> CACHED_FILES = new HashMap<>();

    private final FTPClient ftpClient;
    private final FTPFile ftpFile;
    private final String path;

    public FtpFileDirectory(FTPClient ftpClient, String path, FTPFile ftpFile) {
        this.ftpClient = ftpClient;
        this.path = path;
        this.ftpFile = ftpFile;
    }

    /**
     * На ftp серверах за списком файлом ходим побатчево и отрисовываем переданными батчами.
     * Делается для того, чтобы пользователь долго не ждал отрисовки, когда размер директории достаточно большой
     *
     * @param batchAction консьюмер, обрабатывающий вернувшиеся батчи файлов из текущей директории
     * @param ext         расширение файлов, которые нужно вернуть
     */
    @Override
    public void getFiles(Consumer<List<? extends Directory>> batchAction, String ext) {
        try {
            FTPListParseEngine engine = ftpClient.initiateListParsing(path);
            while (engine.hasNext()) {
                List<Directory> batch = new ArrayList<>();
                FTPFile[] files = engine.getNext(FTP_BATCH_SIZE);
                for (FTPFile file : files) {
                    if (file != null && isSuitableForAdding(ext, file)) {
                        batch.add(new FtpFileDirectory(ftpClient, getFtpPath(file.getName()), file));
                    }
                }
                batchAction.accept(batch);
            }
        } catch (IOException e) {
            throw new FileProcessingException("Unable to list files on FTP server", e);
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
    public Directory createDirectory() {
        if (Directory.isZip(getPath())) {
            try {
                File file = getFileFromCacheOrDownload();
                FileSystem newFileSystem = FileSystems.newFileSystem(file.toPath(), null);
                return new ZipFileDirectory(file.toPath(), newFileSystem, getName(), true);
            } catch (IOException e) {
                throw new FileProcessingException("Unable to create Zip filesystem", e);
            }
        }
        return this;
    }

    /**
     * @return возвращает файл, который был скачан ранее и закеширован, или
     * скачивает файл, кеширует его и возвращает обратно
     */
    private File getFileFromCacheOrDownload() {
        return CACHED_FILES.computeIfAbsent(path, path -> {
            try {
                File file = File.createTempFile("tmp", ".zip");
                file.deleteOnExit();
                try (OutputStream outputStream = new FileOutputStream(file)) {
                    downloadFile(outputStream);
                }
                return file;
            } catch (IOException e) {
                throw new FileProcessingException("Unable to download file", e);
            }
        });
    }

    public void downloadFile(OutputStream os) {
        try {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);
            ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
            ftpClient.retrieveFile(path, os);
        } catch (IOException e) {
            throw new FileProcessingException("Unable to download file", e);
        }
    }

    @Override
    public boolean isDirectory() {
        return ftpFile.isDirectory() || Directory.isZip(getPath());
    }

    @Override
    public String getName() {
        if (ftpFile == null) {
            return path;
        }
        return ftpFile.getName();
    }

    @Override
    public void processFile(Consumer<InputStream> consumer) {
        try (InputStream in = ftpClient.retrieveFileStream(path)) {
            consumer.accept(in);
        } catch (IOException e) {
            throw new FileProcessingException("Unable to download file", e);
        } finally {
            try {
                ftpClient.completePendingCommand();
            } catch (IOException e) {
                System.err.println("Unable to complete file download");
                e.printStackTrace();
            }
        }
    }

    public String getFtpPath(String name) {
        if ("/".equals(path)) {
            return path + name;
        } else {
            return path + "/" + name;
        }
    }

    /**
     * Очистка кеша. Например, после отключения от ftp сервера
     */
    public static void clearCache() {
        CACHED_FILES.clear();
    }

    @Override
    public String toString() {
        return getName();
    }
}
