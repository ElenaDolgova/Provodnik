import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class ZipDirectory implements Directory {
    /**
     * Имя директории
     */
    private final String directoryName;
    /**
     * Путь до файла
     */
    private final Path path;

    private final ZipFile zipFile;
    /**
     * Имя родителя zip файла
     */
    private final String globalParent;

    private final ZipFileLink zipFileLink;

    public ZipDirectory(ZipFileLink displayFiles) throws IOException {
        this.path = displayFiles.getPath();
        this.zipFile = new ZipFile(displayFiles.getPath().toFile());
        this.directoryName = createDirectoryName(this.path.getFileName());
        this.globalParent = null;
        this.zipFileLink = displayFiles;
    }

    public ZipDirectory(ZipFileLink displayFiles, ZipFile zipFile) {
        this.path = displayFiles.getPath();
        this.zipFile = zipFile;
        this.zipFileLink = displayFiles;

        this.directoryName = createDirectoryName(this.path.getFileName());
        this.globalParent = displayFiles.getZipEntry().getName();
    }

    private String createDirectoryName(Path path) {
        return String.valueOf(path.getFileName());
    }

    @Override
    public SortedSet<Link> getFiles() {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        SortedSet<Link> sortedFiles = new TreeSet<>();
        String currentGlobalParent = null;

        try {
            if (zipFileLink.getZipEntry() != null) {
                InputStream in = zipFile.getInputStream(zipFileLink.getZipEntry());
                ZipInputStream zipInputStream = new ZipInputStream(in);
                ZipEntry zipEntry;
                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    String entryName = zipEntry.getName();
                    System.out.println("kjfndvjfn");
                    System.out.println(entryName);
                    File file = new File(entryName);
                    sortedFiles.add(new ZipFileLink(zipFile, entryName, file));
                }
                in.close();
                return sortedFiles;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String entryName = entry.getName();
            File file = new File(entryName);

            if (globalParent == null) {
                if (file.getParent() == null) {
                    currentGlobalParent = file.getName();
                } else if (file.getParent().equals(currentGlobalParent)) {
                    sortedFiles.add(new ZipFileLink(zipFile, entryName, file));
                }
            } else {
                if (path.toString().equals(file.getParent())) {
                    sortedFiles.add(new ZipFileLink(zipFile, entryName, file));
                }
            }
        }
        return sortedFiles;
    }

    public String getDirectoryName() {
        return directoryName;
    }

    @Override
    public String toString() {
        return getDirectoryName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ZipDirectory that = (ZipDirectory) o;
        return Objects.equals(directoryName, that.directoryName) &&
                Objects.equals(path, that.path) &&
                Objects.equals(zipFile, that.zipFile) &&
                Objects.equals(globalParent, that.globalParent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(directoryName, path, zipFile, globalParent);
    }
}
