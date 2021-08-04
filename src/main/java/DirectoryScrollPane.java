import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneLayout;
import javax.swing.filechooser.FileSystemView;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;

/**
 *
 */
// todo один интерфейс для FileScrollPane тоже?
public class DirectoryScrollPane {
    // todo если под винду, то не понятно, что такое стартовый путь
    // todo понять, почему /Users/elena-dolgova/arcadia/arcadia/mbi/mbi/ не считает диреторией
//    private static final File START_PATH = new File("/Users/elena-dolgova/Desktop");
    private static final String FTP_PATH = "ftp://anonymous@ftp.bmc.com";

    private final JScrollPane jScrollPane;

    public DirectoryScrollPane() {

        JList<Directory> displayDirectory = null;
        try {
            System.out.println("1 " + FileSystemView.getFileSystemView().getHomeDirectory());
            System.out.println("2 " + FileSystemView.getFileSystemView().getRoots()[0]);
            File defaultPath = FileSystemView.getFileSystemView().getRoots()[0];
            FileSystem fs = defaultPath.toPath().getFileSystem();

            FileSystemManager fsManager = VFS.getManager();
            FileObject fileObject = fsManager.resolveFile(defaultPath.toURI());
            displayDirectory = new JList<>(createLocalDirectoryLinks(fs, defaultPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        JScrollPane scrollPane = new JScrollPane(displayDirectory);
        scrollPane.setBounds(1, 1, Dimensions.DIRECTORY_SCROLL_PANE_WIDTH, Dimensions.DIRECTORY_SCROLL_PANE_HEIGHT);
        scrollPane.setLayout(new ScrollPaneLayout());
        this.jScrollPane = scrollPane;
    }

    public DirectoryScrollPane(int a) {
        FileSystemManager fsManager;
        JList<Directory> displayDirectory = null;
        try {
            fsManager = VFS.getManager();
            FileSystemOptions opts = new FileSystemOptions();
            FileObject fileObject = fsManager.resolveFile(FTP_PATH, opts);
            displayDirectory = new JList<>(createFtpDirectoryLinks(fileObject));
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
        JScrollPane scrollPane = new JScrollPane(displayDirectory);
        scrollPane.setBounds(1, 1, Dimensions.DIRECTORY_SCROLL_PANE_WIDTH, Dimensions.DIRECTORY_SCROLL_PANE_HEIGHT);
        scrollPane.setLayout(new ScrollPaneLayout());
        this.jScrollPane = scrollPane;
    }

    public void init(Renderer renderer) {
        MouseListener mouseListener = getMouseListener(renderer);
        Component viewport = jScrollPane.getViewport().getView();
        viewport.addMouseListener(mouseListener);
    }

    public DefaultListModel<Directory> createLocalDirectoryLinks(FileSystem fs,
                                                                 File defaultPath) throws FileSystemException {
        DefaultListModel<Directory> labelJList = new DefaultListModel<>();
        labelJList.addElement(new LocalDirectory(fs, defaultPath.toPath()));
        return labelJList;
    }

    public DefaultListModel<Directory> createFtpDirectoryLinks(FileObject fileObject) throws FileSystemException {
        DefaultListModel<Directory> labelJList = new DefaultListModel<>();
        labelJList.addElement(new FTPDirectory(fileObject));
        return labelJList;
    }

    /**
     * Метод вызывается при клике на определенный элемент на панели с директориями.
     * При это если пользователь нажал на не листовую директорию,
     * то директории перестают отображаться ровно до выбранной и на панели с просмоторщиком файлов начинают
     * отображаться файлы, выбранной директории.
     */
    // todo не с первого раза работают кнопки, может попробовать другой листенер?
    private MouseAdapter getMouseListener(Renderer renderer) {
        // тест кейс:
        // 1. нажимаем на директорию в середине и у нас удаляется хвост (причем, чтобы память не текла, надо еще удалть ссылки на обхекты)
        // 2. нажимаем на последнюю директорию и ничего не меняется И директории не перестраиваются.
        // 3. Зашли в поддерево, вышли из него -> зашли в более глубокое

        return new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JList<Directory> source = (JList<Directory>) e.getSource();
                // убираем таб с превью
                PreviewPanel.getImage().setVisible(false);
                PreviewPanel.getTextArea().setVisible(false);
                // обновляем содержимое панели с файлами
                renderer.updateFilesScrollPane(source.getSelectedValue());
                //схлопываем директорию до нажатой
                DefaultListModel<Directory> sourceModel = (DefaultListModel<Directory>) source.getModel();
                for (int i = sourceModel.getSize() - 1; i > source.getSelectedIndex(); --i) {
                    sourceModel.remove(i);
                }
            }
        };
    }

    public JScrollPane getScrollPane() {
        return jScrollPane;
    }
}
