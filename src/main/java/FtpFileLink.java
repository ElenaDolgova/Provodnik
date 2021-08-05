import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

import java.io.InputStream;
import java.nio.file.Path;

public class FtpFileLink implements Link {
    private final FileObject fileObject;

    public FtpFileLink(FileObject fileObject) {
        this.fileObject = fileObject;
    }

    @Override
    public Path getPath() {
        try {
            return fileObject.getContent().getFile().getPath();
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Directory createDirectory() {
        return new FTPDirectory(fileObject);
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
    public String getName() {
        try {
            return fileObject.getContent().getFile().getName().getBaseName();
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
        return "qwerty";
    }

    @Override
    public InputStream getInputStreamOfFile() throws FileSystemException {
        return fileObject.getContent().getInputStream();
    }

    @Override
    public String getProbeContentType() {
        try {
            return fileObject.getContent().getContentInfo().getContentType();
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int compareTo(Link o) {
        if (o == null) {
            return 1;
        }
        return getName().compareTo(o.getName());
    }
}
