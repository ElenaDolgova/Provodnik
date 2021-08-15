package gui;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.jetbrains.annotations.Nullable;

import javax.swing.ImageIcon;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PreviewImageCache {
    private final Executor executor = Executors.newFixedThreadPool(5);
    private final Map<Path, ImageIcon> cache = Caffeine.newBuilder()
            .maximumSize(100_000)
            .<Path, ImageIcon>build()
            .asMap();

    public void computeAndCache(Path path, Callable<ImageIcon> getter) {
        executor.execute(() -> {
            try {
                ImageIcon preview = getter.call();
                if (preview != null) {
                    cache.put(path, preview);
                }
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
