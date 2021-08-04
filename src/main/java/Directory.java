import java.util.List;

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
