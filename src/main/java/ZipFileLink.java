import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipFileLink implements Link {
    private final ZipFile zipFile;
    /**
     * Имя entry, по которому можно найти ZipEntry в zipFile
     */
    private final String entryName;
    /**
     * Имя файла
     */
    private final String name;
    private final File file;

    public ZipFileLink(ZipFile zipFile, String entryName, File file) {
        this.zipFile = zipFile;
        this.entryName = entryName;
        this.name = file.getName();
        this.file = file;
    }

    public String getParent() {
        return file.getParent();
    }

    @Override
    public Path getPath() {
        return file.toPath();
    }

    @Override
    public boolean isDirectory() {
        return getZipEntry().isDirectory();
    }

    public ZipEntry getZipEntry() {
        return zipFile.getEntry(entryName);
    }

    private InputStream getInputStreamOfFile() throws IOException {
        return zipFile.getInputStream(getZipEntry());
    }

    @Override
    public void invoke(Renderer renderer) {
        try {
            String probeContentType = null;
            try {
                probeContentType = Files.probeContentType(getPath());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

            if (probeContentType != null) {
                if ("application/zip".equals(probeContentType)) {
                    // todo zip внутри zip не работает
                    Directory newDirectory = new ZipDirectory(this);
                    // todo не очень нравится, что тут вызывается статический метод,
                    // который по сути должен только использовать это класс
                    FilesScrollPane.addNewDirectory(newDirectory, renderer.getDIRECTORY_SCROLL_PANE(), renderer);
                } else if (isDirectory()) {
                    Directory newDirectory = new ZipDirectory(this, getZipFile());
                    FilesScrollPane.addNewDirectory(newDirectory, renderer.getDIRECTORY_SCROLL_PANE(), renderer);
                }
                renderer.getPREVIEW_PANEL().update(probeContentType, getInputStreamOfFile());
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return file.getName();
    }

    public ZipFile getZipFile() {
        return zipFile;
    }

    @Override
    public int compareTo(Link o) {
        if (o == null) {
            return 1;
        }
        return name.compareTo(o.getName());
    }

    @Override
    public String toString() {
        return getName();
    }
}
