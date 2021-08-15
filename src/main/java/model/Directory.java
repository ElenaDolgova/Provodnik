package model;

import exception.FileProcessingException;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface Directory {
    Path getPath();

    String getName();

    boolean isDirectory();

    Directory createDirectory() throws FileProcessingException;

    void processFile(Consumer<InputStream> consumer, String probeContentType) throws FileProcessingException;

    /**
     * The method goes through the directory and gets all the elements from it.
     * After the batchAction, it processes these elements.
     * If filter extension is passed to the method, files that satisfy the passed extension are returned.
     *
     * @param batchAction consumer that processes returned files from the current directory
     * @param ext         filter by extension
     */
    void getFiles(Consumer<List<? extends Directory>> batchAction, String ext) throws FileProcessingException;

    @Nullable
    static String getProbeContentType(Path path) {
        String result = null;
        try {
            result = Files.probeContentType(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    static Stream<Path> streamAllFiles(FileSystem fs, int depth) {
        return StreamSupport.stream(fs.getRootDirectories().spliterator(), false)
                .flatMap(it -> walkFiles(it, depth));
    }

    static Stream<Path> walkFiles(Path it, int depth) {
        try {
            return Files.walk(it, depth);
        } catch (IOException e) {
            throw new FileProcessingException("Unable to walk file tree", e);
        }
    }

    static String getExtension(Path p) {
        return FilenameUtils.getExtension(p.toString());
    }

    static boolean isZip(Path path) {
        String probeContentType = Directory.getProbeContentType(path);
        if (probeContentType == null) {
            return false;
        }
        return probeContentType.contains("zip");
    }
}
