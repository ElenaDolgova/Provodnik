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
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static model.Directory.getExtension;
import static model.Directory.isZip;
import static model.Directory.streamAllFiles;

public class ZipFileDirectory implements Directory {
    private final Path path;
    private final FileSystem fs;
    private final boolean isFirstZip;
    private String name;

    public ZipFileDirectory(Path path, FileSystem fs, boolean isFirstZip) {
        this.path = path;
        this.fs = fs;
        this.isFirstZip = isFirstZip;
    }

    public ZipFileDirectory(Path path, FileSystem fs, String name, boolean isFirstZip) {
        this.path = path;
        this.fs = fs;
        this.isFirstZip = isFirstZip;
        this.name = name;
    }

    @Override
    public void getFiles(Consumer<List<? extends Directory>> batchAction, String ext) {
        if (isZip(path)) {
            List<ZipFileDirectory> collect = streamAllFiles(fs, 1)
                    .filter(path -> {
                        if (ext == null || ext.length() == 0) return true;
                        return ext.equals(getExtension(path));
                    })
                    .filter(path -> {
                        Path parent = path.getParent();
                        // zip на macos создаются с доп дирекотрий корня
                        return parent != null && !path.startsWith("/__MACOSX");
                    })
                    .map(path -> new ZipFileDirectory(path, fs, false))
                    .sorted(Comparator.comparing(Directory::getName))
                    .collect(Collectors.toList());
            batchAction.accept(collect);
        }
        int depth = path.getNameCount() + 1;
        batchAction.accept(streamAllFiles(fs, depth)
                .filter(p -> p.startsWith("/" + path))
                .filter(p -> ext == null || ext.length() == 0 || ext.equals(getExtension(p)))
                .filter(p -> p.getNameCount() > path.getNameCount())
                .map(path -> new ZipFileDirectory(path, fs, false))
                .collect(Collectors.toList()));
    }

    @Override
    public Directory createDirectory() {
        if (isZip(path)) {
            try {
                if (!isFirstZip) {
                    FileSystem newFileSystem = FileSystems.newFileSystem(fs.getPath(path.toString()), null);
                    return new ZipFileDirectory(path, newFileSystem, false);
                }
            } catch (IOException e) {
                throw new FileProcessingException("Can't create file system on zip", e);
            }
        }
        return this;
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public boolean isDirectory() {
        return Files.isDirectory(path) || isZip(path);
    }

    @Override
    public void processFile(Consumer<InputStream> consumer) {
        try {
            byte[] bytes = Files.readAllBytes(fs.getPath(path.toString()));
            consumer.accept(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            throw new FileProcessingException("Can't read file", e);
        }
    }

    @Override
    public String getName() {
        if (name != null) {
            return name;
        }
        return String.valueOf(path.getFileName());
    }

    @Override
    public String toString() {
        return getName();
    }
}
