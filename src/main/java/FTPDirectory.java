import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.FileSystemException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FTPDirectory implements Directory {

    private final FileObject fileObject;

    //https://www.mmnt.net/
    public FTPDirectory(FileObject fileObject) {
        this.fileObject = fileObject;
    }

    @Override
    public List<Link> getFiles() {
        try {
            return Arrays.stream(fileObject.getChildren())
                    .map(FtpFileLink::new)
                    .collect(Collectors.toList());
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    @Override
    public String getDirectoryName() {
        try {
            FileName name = fileObject.getContent().getFile().getName();
            String baseName = name.getBaseName();
            if (baseName.isBlank()) {
                return name.toString();
            } else {
                return baseName;
            }
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return getDirectoryName();
    }
}
