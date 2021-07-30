import java.awt.event.MouseListener;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DirectoryLink {
    private final String fullPath;
    private final String directoryName;
    private final Supplier<String> supplier;

    public DirectoryLink(String fullPath, String directoryName, Supplier<String> supplier) {
        this.fullPath = fullPath;
        this.directoryName = directoryName;
        this.supplier = supplier;
    }

    public String getFullPath() {
        return fullPath;
    }

    public String getDirectoryName() {
        return directoryName;
    }

    public Supplier<String> getSupplier() {
        return supplier;
    }

    @Override
    public String toString() {
        return directoryName;
    }
}
