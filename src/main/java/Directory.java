import java.util.List;
import java.util.SortedSet;

public interface Directory {
    /**
     * @return список файлов текущей директории
     */
    List<Link> getFiles();

    /**
     * @return имя текущей директории
     */
    String getDirectoryName();
}
