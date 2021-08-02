import java.util.SortedSet;

public interface Directory {
    /**
     * @return список файлов текущей директории
     */
    SortedSet<Link> getFiles();

    /**
     * @return имя текущей директории
     */
    String getDirectoryName();
}
