import org.apache.commons.vfs2.FileObject;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Класс описывает один элемент на табе с директориями {@link DirectoryScrollPane}
 */
public record LocalDirectory(FileObject fileObject) implements Directory {

    @Override
    public List<Link> getFiles() {
        try {
            //todo тест кейс, а когда file.getChildren() мб null?
            return Arrays.stream(fileObject.getChildren())
                    .map(LocalDirectory::getLink)
                    .collect(Collectors.toList());
        } catch (org.apache.commons.vfs2.FileSystemException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    private static Link getLink(FileObject fo) {
        try {
            if ("application/zip".equals(fo.getContent().getContentInfo().getContentType())) {
                FileSystem fs = FileSystems.newFileSystem(
                        Paths.get(fo.getPath().toString()), Collections.emptyMap());
                return new ZipFileLink(fo.getPath(), fs, true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new LocalFileLink(fo);
    }

    @Override
    public String getDirectoryName() {
        return fileObject.getName().getBaseName();
    }

    @Override
    public String toString() {
        return getDirectoryName();
    }
}
