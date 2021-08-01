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
    private final String entryName;
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
    public File getFile() {
        return file;
    }

    @Override
    public Path createPath() {
        return file.toPath();
    }

    @Override
    public boolean isDirectory() {
        return getZipEntry().isDirectory();
    }

    public ZipEntry getZipEntry() {
        return zipFile.getEntry(entryName);
    }

    @Override
    public Image getImage() {
        Image image = null;
        try {
            image = ImageIO.read(getInputStreamOfFile());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return image;
    }

    private InputStream getInputStreamOfFile() throws IOException {
       return zipFile.getInputStream(getZipEntry());
    }

    @Override
    public void invoke() {
        try {
            if ("application/zip".equals(Files.probeContentType(createPath()))) {
                // todo zip внутри zip не работает
                Directory newDirectory = new ZipDirectory(this);
                // todo не очень нравится, что тут вызывается статический метод,
                // который по сути должен только использовать это класс
                FilesScrollPane.addNewDirectory(newDirectory);
            } else if (isDirectory()) {
                Directory newDirectory = new ZipDirectory(this, getZipFile());
                FilesScrollPane.addNewDirectory(newDirectory);
            }

            String probeContentType = null;
            try {
                probeContentType = Files.probeContentType(createPath());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            if (probeContentType != null) {
                if (probeContentType.contains("image")) {
                    Image image = getImage();
                    if (image != null) {
                        MainFrame.PREVIEW_PANEL.updateImage(image);
                    }
                } else if (probeContentType.contains("text")) {
                    MainFrame.PREVIEW_PANEL.updateTxt(getInputStreamOfFile());
                }
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
    public File createFile() {
        return file;
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
