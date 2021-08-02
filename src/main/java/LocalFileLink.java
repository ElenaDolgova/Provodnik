import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LocalFileLink implements Link {
    private final File file;
    private final String name;

    public LocalFileLink(File file) {
        this.file = file;
        this.name = file.getName();
    }

    @Override
    public Directory createDirectory() {
        Directory newDirectory = null;
        if (isDirectory()) {
            newDirectory = new LocalDirectory(this);
        }
        return newDirectory;
    }

    @Override
    public InputStream getInputStreamOfFile() throws IOException {
        return new FileInputStream(file);
    }

    @Override
    public String getProbeContentType() {
        String type = null;
        try {
            type = Files.probeContentType(getPath());
            return type;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isDirectory() {
        return file.isDirectory();
    }


    @Override
    public Path getPath() {
        return file.toPath();
    }

    @Override
    public String toString() {
        return getName();
    }
}
