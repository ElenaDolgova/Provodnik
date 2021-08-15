package gui;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.jetbrains.annotations.Nullable;

import javax.swing.ImageIcon;
import java.awt.*;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class PreviewImageCache {
    private final Executor executor = Executors.newFixedThreadPool(5);
    private final Map<String, ImageIcon> cache = Caffeine.newBuilder()
            .maximumSize(100_000)
            .<String, ImageIcon>build()
            .asMap();

    public void computeAndCacheAsync(String path, int width, int height, Supplier<Image> getter) {
        executor.execute(() -> {
            try {
                Image img = getter.get();
                if (img != null) {
                    cache.putIfAbsent(String.format("%s__%dx%d", path, width, height), new ImageIcon(img.getScaledInstance(width, -1, Image.SCALE_SMOOTH)));
                    cache.putIfAbsent(
                            String.format("%s__%dx%d", path, Dimensions.FILE_ICON_SIZE, Dimensions.FILE_ICON_SIZE),
                            new ImageIcon(img.getScaledInstance(
                                    Dimensions.FILE_ICON_SIZE,
                                    Dimensions.FILE_ICON_SIZE,
                                    Image.SCALE_FAST
                            ))
                    );
                }
            } catch (Exception e) {
                e.printStackTrace(); //TODO сделать что-то нормальное
            }
        });
    }

    @Nullable
    public ImageIcon get(String path, int width, int height) {
        return cache.get(String.format("%s__%dx%d", path, width, height));
    }

    @Nullable
    public ImageIcon computeIfAbsent(String path, int width, int height, Supplier<Image> supplier) {
        return cache.computeIfAbsent(String.format("%s__%dx%d", path, width, height), (unused) -> {
            Image img = supplier.get();
            if (img != null) {
                return new ImageIcon(img.getScaledInstance(width, height, Image.SCALE_SMOOTH));
            } else {
                return null;
            }
        });
    }
}
