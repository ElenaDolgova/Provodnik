package gui;

import org.jetbrains.annotations.Nullable;

import javax.swing.ImageIcon;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.*;

public class PreviewImageCache {
    private final Executor executor = Executors.newFixedThreadPool(5);
    private final Map<Path, ImageIcon> cache = new ConcurrentHashMap<>(); //TODO cache retention cofein

    public void computeAndCache(Path path, Callable<ImageIcon> getter) {
        executor.execute(() -> {
            try {
                ImageIcon preview = getter.call();
                cache.put(path, preview);
            } catch (Exception e) {
                e.printStackTrace(); //TODO сделать что-то нормальное
            }
        });
    }

    @Nullable
    public ImageIcon get(Path path) {
        return cache.get(path);
    }
}
