package model;

import exception.FileProcessingException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static model.Directory.isZip;

public class LocalFileDirectory implements Directory {
    private final FileSystem fs;
    private final Path path;

    public LocalFileDirectory(FileSystem fs, Path path) {
        this.fs = fs;
        this.path = path;
    }

    @Override
    public void getFiles(Consumer<List<? extends Directory>> batchAction, String ext) {
        batchAction.accept(Directory.walkFiles(path, 1)
                .filter(p -> p.getNameCount() == path.getNameCount() + 1)
                .filter(p -> ext == null || ext.length() == 0 || ext.equals(Directory.getExtension(p)))
                .map(this::getDirectory)
                .sorted(Comparator.comparing(Directory::getName))
                .collect(Collectors.toList()));
    }

    private Directory getDirectory(Path path) {
        try {
            if (isZip(path)) {
                FileSystem fs = FileSystems.newFileSystem(path, null);
                return new ZipFileDirectory(path, fs, true);
            }
        } catch (IOException e) {
            throw new FileProcessingException("Unable to create local filesystem", e);
        }
        return new LocalFileDirectory(fs, path);
    }

    @Override
    public Directory createDirectory() {
        return new LocalFileDirectory(fs, path);
    }

    @Override
    public void processFile(Consumer<InputStream> consumer) {
        try {
            byte[] bytes = Files.readAllBytes(fs.getPath(path.toString()));
            consumer.accept(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            throw new FileProcessingException("Can't process file for preview", e);
        }
    }

    @Override
    public String getName() {
        Path fileName = path.getFileName();
        return Objects.requireNonNullElse(fileName, path).toString();
    }

    @Override
    public boolean isDirectory() {
        return path.toFile().isDirectory();
    }


    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public String toString() {
        return getName();
    }
}
