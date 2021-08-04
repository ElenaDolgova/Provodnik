import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface Directory {
    /**
     * @return список файлов текущей директории
     */
    Collection<Link> getFiles();

    /**
     * @return список файлов текущей директории c филтрацией по расширению ext
     */
    Collection<Link> getFiles(String ext);

    /**
     * @return имя текущей директории
     */
    String getDirectoryName();

    static Stream<Path> streamAllFiles(FileSystem fs, int depth) {
        return StreamSupport.stream(fs.getRootDirectories().spliterator(), false)
                .flatMap((it) -> walkFiles(it, depth));
    }

    static Stream<Path> walkFiles(Path it, int depth) {
        try {
            return Files.walk(it, depth);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    static String getExtension(Path p) {
        return FilenameUtils.getExtension(p.toString());
    }
}
