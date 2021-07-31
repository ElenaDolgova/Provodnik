import java.util.function.Supplier;

/**
 * Класс описывает один элемент на табе с директориями {@link DirectoryScrollPane}
 */
public class DirectoryLink {
    /**
     * Полный путь отображаемой директории
     */
    private final String fullPath;
    /**
     * Имя диреткории
     */
    private final String directoryName;
    /**
     * Вызывается при нажатии на директорию
     */
    private final Supplier<String> mouseClickSupplier;

    public DirectoryLink(String fullPath, String directoryName, Supplier<String> mouseClickSupplier) {
        this.fullPath = fullPath;
        this.directoryName = directoryName;
        this.mouseClickSupplier = mouseClickSupplier;
    }

    public String getFullPath() {
        return fullPath;
    }

    public String getDirectoryName() {
        return directoryName;
    }

    public Supplier<String> getMouseClickSupplier() {
        return mouseClickSupplier;
    }

    @Override
    public String toString() {
        return getDirectoryName();
    }
}
