import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.FileSystemException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class FTPDirectory implements Directory {

    private final FileObject fileObject;

    //https://www.mmnt.net/
    public FTPDirectory(FileObject fileObject) {
        this.fileObject = fileObject;
    }

    @Override
    public Collection<Link> getFiles() {
        return downloadFiles(null);
    }

    @Override
    public Collection<Link> getFiles(String ext) {
        return downloadFiles(ext);
    }

    private Collection<Link> downloadFiles(String ext) {
        try {
            return Arrays.stream(fileObject.getChildren())
                    .filter(p -> {
                        try {
                            if (ext == null || StringUtils.isBlank(ext)) return true;
                            String contentEncoding = p.getContent().getFile().getName().getExtension();
                            return ext.equals(contentEncoding);
                        } catch (FileSystemException e) {
                            e.printStackTrace();
                        }
                        return false;
                    })
                    .map(FtpFileLink::new)
                    .sorted()
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
