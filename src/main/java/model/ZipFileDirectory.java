package model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ZipFileDirectory implements Directory {
    private final Path path;
    private final FileSystem fs;
    private final boolean isFirstZip;

    public ZipFileDirectory(Path path, FileSystem fs, boolean isFirstZip) {
        this.path = path;
        this.fs = fs;
        this.isFirstZip = isFirstZip;
    }

    @Override
    public void getFiles(Consumer<List<? extends Directory>> batchAction, String ext) {
        if (isZip(path)) {
            List<ZipFileDirectory> collect = Directory.streamAllFiles(fs, 1)
                    .filter(path -> {
                        if (ext == null || ext.length() == 0) return true;
                        return ext.equals(Directory.getExtension(path));
                    })
                    .filter(path -> {
                        Path parent = path.getParent();
                        // zip на macos создаются с доп дирекотрий корня
                        return parent != null && !path.startsWith("/__MACOSX");
                    })
                    .map(path -> new ZipFileDirectory(path, fs, false))
                    .sorted()
                    .collect(Collectors.toList());
            batchAction.accept(collect);
        }
        int depth = path.getNameCount() + 1;
        batchAction.accept(Directory.streamAllFiles(fs, depth)
                .filter(p -> p.startsWith("/" + path))
                .filter(p -> ext == null || ext.length() == 0 || p.endsWith(ext))
                .filter(p -> p.getNameCount() > path.getNameCount())
                .map(path -> new ZipFileDirectory(path, fs, false))
                .collect(Collectors.toList()));
    }

    @Override
    public Directory createDirectory() throws IOException {
        if (isZip(path)) {
            FileSystem newFileSystem;
            if (isFirstZip) {
                // создается просто самый первый zip
                newFileSystem = FileSystems.newFileSystem(path, null);
            } else {
                //  zip внутри zip Создается новая файловая подсистема
                newFileSystem = FileSystems.newFileSystem(fs.getPath(path.toString()), null);
            }
            return new ZipFileDirectory(path, newFileSystem, isFirstZip);
        }
        // когда директория внутри zip
        return new ZipFileDirectory(path, fs, isFirstZip);
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
    public void processFile(Consumer<InputStream> consumer) throws IOException {
        byte[] bytes = Files.readAllBytes(fs.getPath(path.toString()));
        consumer.accept(new ByteArrayInputStream(bytes));
    }

    @Override
    public String getName() {
        return String.valueOf(path.getFileName());
    }

    public static boolean isZip(Path path) {
        return "application/zip".equals(Directory.getProbeContentType(path));
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int compareTo(Directory o) {
        if (o == null) {
            return 1;
        }
        return getName().compareTo(o.getName());
    }
}
