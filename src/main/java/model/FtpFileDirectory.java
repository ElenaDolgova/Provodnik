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
     * Cached downloaded zip archives from ftp servers.
     * If the user decided to re-open the previously downloaded file,
     * then it will not have to be downloaded again, it will be taken from the cache.
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
     * On ftp servers, we go after the list of files by default and draw them with the transferred butches.
     * This is done so that the user does not wait for a long time for rendering,
     * when the size of the directory is large enough
     *
     * @param batchAction consumer that processes returned files from the current directory
     * @param ext         filter by extension
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
                File file = getFileFromCacheOrDownload(".zip");
                FileSystem newFileSystem = FileSystems.newFileSystem(file.toPath(), null);
                return new ZipFileDirectory(file.toPath(), newFileSystem, getName(), true);
            } catch (IOException e) {
                throw new FileProcessingException("Unable to create Zip filesystem", e);
            }
        }
        return this;
    }

    /**
     * @return returns a file that was downloaded earlier and cached or
     * downloads the file, caches it, and returns it back
     */
    private File getFileFromCacheOrDownload(String suffix) {
        return CACHED_FILES.computeIfAbsent(path, path -> {
            try {
                File file = File.createTempFile("tmp", suffix);
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
    public void processFile(Consumer<InputStream> consumer, String probeContentType) {
        if (probeContentType != null && probeContentType.contains("image")) {
            try (InputStream in = new FileInputStream(getFileFromCacheOrDownload(".img"))) {
                consumer.accept(in);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
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
    }

    public String getFtpPath(String name) {
        if ("/".equals(path)) {
            return path + name;
        } else {
            return path + "/" + name;
        }
    }

    /**
     * Clearing the cache.
     * For example, after disconnecting from the ftp server.
     */
    public static void clearCache() {
        CACHED_FILES.clear();
    }

    @Override
    public String toString() {
        return getName();
    }
}
