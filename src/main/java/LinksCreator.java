import javax.swing.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;

public final class LinksCreator {

    private LinksCreator() {
    }

    public static DefaultListModel<DirectoryLink> createDirectoryLinks(File fullPath) {
        DefaultListModel<DirectoryLink> labelJList = new DefaultListModel<>();
        createLinks(fullPath).forEach(labelJList::addElement);
        return labelJList;
    }

    private static List<DirectoryLink> createLinks(File fullPath) {
        List<DirectoryLink> links = new ArrayList<>(fullPath.toPath().getNameCount());

        String parent = fullPath.toPath().toAbsolutePath().toString();
        while (parent != null) {
            Path path = Paths.get(parent).toAbsolutePath();
            DirectoryLink directoryLink = createDirectoryLink(path);
            links.add(directoryLink);
            parent = path.toFile().getParent();
        }
        Collections.reverse(links);
        return links;
    }

    private static DirectoryLink createDirectoryLink(Path path) {
        final String directoryName = createDirectoryName(path);
        return new DirectoryLink(path.toString(), directoryName, getMouseClickSupplier(path, directoryName));
    }

    /**
     * Метод создает имя, отображаемой директории на панели с текущими диреткориями
     * Если path - это корневая директория, то path.getFileName() возвращает null.
     */
    private static String createDirectoryName(Path path) {
        return path.getFileName() == null ? "/" : path.getFileName() + "/";
    }

    /**
     * @param path
     * @param directoryName
     * @return
     */
    private static Supplier<String> getMouseClickSupplier(Path path, String directoryName) {
        return () -> {
            // тест кейс:
            // 1. нажимаем на директорию в середине и у нас удаляется хвост (причем, чтобы память не текла, надо еще удалть ссылки на обхекты)
            // 2. нажимаем на последнюю директорию и ничего не меняется И директории не перестраиваются.
            // 3. Зашли в поддерево, вышли из него -> зашли в более глубокое


            //todo на правой панельке отобразить содержимое папки; обратить внимание на файловую систему. Она должна быть разной для мак оси и винды
            System.out.println("I am here " + directoryName);
            path.getFileSystem().getFileStores()
                    .forEach(file -> {
                        System.out.println(file.type());
                    });
            File file = path.toFile();
            if (file.isDirectory()) {
                Arrays.stream(Objects.requireNonNull(file.listFiles())).forEach(p -> System.out.println(p.toString()));
            }
            System.out.println(path.getFileName());
            System.out.println();
            return "";
        };
    }
}
