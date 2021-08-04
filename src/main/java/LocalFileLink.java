import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public record LocalFileLink(FileObject fileObject) implements Link {

    @Override
    public Directory createDirectory() {
        return new LocalDirectory(fileObject);
    }

    @Override
    public InputStream getInputStreamOfFile() throws IOException {
        return fileObject.getContent().getInputStream();
    }

    @Override
    public String getName() {
        return fileObject.getName().getBaseName();
    }

    @Override
    public boolean isDirectory() {
        try {
            return fileObject.isFolder();
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public Path getPath() {
        return fileObject.getPath();
    }

    @Override
    public String toString() {
        return getName();
    }
}
