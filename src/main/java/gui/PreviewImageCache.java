package gui;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.jetbrains.annotations.Nullable;

import javax.swing.ImageIcon;
import java.awt.*;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PreviewImageCache {
    private final Executor executor = Executors.newFixedThreadPool(5);
    private final Map<String, ImageIcon> cache = Caffeine.newBuilder()
            .maximumSize(100_000)
            .<String, ImageIcon>build()
            .asMap();

    public void computeAndCacheAsync(String path, Callable<ImageIcon> getter) {
        executor.execute(() -> {
            try {
                ImageIcon preview = getter.call();
                if (preview != null) {
                    cache.put(path, preview);
                    cache.put(
                            path + "__icon",
                            new ImageIcon(
                                    preview.getImage().getScaledInstance(
                                            Dimensions.FILE_ICON_SIZE,
                                            Dimensions.FILE_ICON_SIZE,
                                            Image.SCALE_FAST
                                    )
                            )
                    );
                }
            } catch (Exception e) {
                e.printStackTrace(); //TODO сделать что-то нормальное
            }
        });
    }

    @Nullable
    public ImageIcon get(String path) {
        return cache.get(path);
    }
}
