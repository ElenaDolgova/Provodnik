import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.SortedSet;

public final class Renderer {
    private final JFrame GLOBAL_FRAME;
    private final DirectoryScrollPane DIRECTORY_SCROLL_PANE;
    private final FilesScrollPane FILES_SCROLL_PANE;
    private final PreviewPanel PREVIEW_PANEL;

    public Renderer(JFrame GLOBAL_FRAME,
                    DirectoryScrollPane DIRECTORY_SCROLL_PANE,
                    FilesScrollPane FILES_SCROLL_PANE,
                    PreviewPanel PREVIEW_PANEL) {
        this.GLOBAL_FRAME = GLOBAL_FRAME;
        this.DIRECTORY_SCROLL_PANE = DIRECTORY_SCROLL_PANE;
        this.FILES_SCROLL_PANE = FILES_SCROLL_PANE;
        this.PREVIEW_PANEL = PREVIEW_PANEL;
    }

    public void updateFilesScrollPane(Directory directory) {
        JList<Link> displayFiles = getDirectoryFiles(directory);
        FILES_SCROLL_PANE.getScrollPane().setViewportView(displayFiles);
        displayFiles.addMouseListener(getFilesScrollMouseListener(this));
    }

    /**
     * Метод возвращает список файлов текущей директории
     */
    private JList<Link> getDirectoryFiles(Directory directory) {
        SortedSet<Link> set = directory.getFiles();
        DefaultListModel<Link> labelJList = new DefaultListModel<>();
        set.forEach(labelJList::addElement);
        return new JList<>(labelJList);
    }

    /**
     * Вызывается при нажатии на файл в табе с файлами (в каталоге) {@link MainFrame.FILES_SCROLL_PANE}
     */
    // todo а точно ли этот листенер разумно оставлять в preview?
    public static MouseAdapter getFilesScrollMouseListener(Renderer renderer) {
        // тест кейс:
        // 1. нажимаем на директорию в середине и у нас удаляется хвост (причем, чтобы память не текла, надо еще удалть ссылки на обхекты)
        // 2. нажимаем на последнюю директорию и ничего не меняется И директории не перестраиваются.
        // 3. Зашли в поддерево, вышли из него -> зашли в более глубокое

        return new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // todo по пути /Users/tmpAdmin/ не обновляется файловый таб
                JList<Link> source = (JList<Link>) e.getSource();
                Link displayFiles = source.getSelectedValue();
                // todо тест кейса на добавление не повторяющихся файлов
                // тест на добавление только нового! файлов в директорию

                // todo zip внутри zip
                displayFiles.invoke(renderer);
            }
        };
    }


    public JFrame getGLOBAL_FRAME() {
        return GLOBAL_FRAME;
    }

    public DirectoryScrollPane getDIRECTORY_SCROLL_PANE() {
        return DIRECTORY_SCROLL_PANE;
    }

    public FilesScrollPane getFILES_SCROLL_PANE() {
        return FILES_SCROLL_PANE;
    }

    public PreviewPanel getPREVIEW_PANEL() {
        return PREVIEW_PANEL;
    }
}
