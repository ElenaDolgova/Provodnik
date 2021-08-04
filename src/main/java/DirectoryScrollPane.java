import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;

/**
 *
 */
// todo один интерфейс для FileScrollPane тоже?
public class DirectoryScrollPane {
    // todo если под винду, то не понятно, что такое стартовый путь
    private static final String FTP_PATH = "ftp://anonymous@ftp.bmc.com";

    private final JPanel mainDirectoryPane;
    private final JScrollPane jScrollPane;
    private final JButton connectToFtpButton;

    // Приложение всегда открывается в локальной файловой системе
    public DirectoryScrollPane() {
        JList<Directory> displayDirectory = null;
        try {
//            System.out.println("1 " + FileSystemView.getFileSystemView().getRoots()[0]);
            File defaultPath = FileSystemView.getFileSystemView().getRoots()[0];
            FileSystem fs = defaultPath.toPath().getFileSystem();
            displayDirectory = new JList<>(createLocalDirectoryLinks(fs, defaultPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        JScrollPane scrollPane = new JScrollPane(displayDirectory);
        scrollPane.setBounds(1, 1, Dimensions.DIRECTORY_SCROLL_PANE_WIDTH, Dimensions.DIRECTORY_SCROLL_PANE_HEIGHT);
        scrollPane.setLayout(new ScrollPaneLayout());
        this.jScrollPane = scrollPane;
        this.mainDirectoryPane = new JPanel(new BorderLayout());
        this.connectToFtpButton = new JButton("Connect to ftp");
        this.mainDirectoryPane.add(this.connectToFtpButton, BorderLayout.SOUTH);
        this.mainDirectoryPane.add(this.jScrollPane, BorderLayout.CENTER);
    }

    public void init(Renderer renderer) {
        MouseListener mouseListener = getDirectoryListener(renderer);
        Component viewport = jScrollPane.getViewport().getView();
        viewport.addMouseListener(mouseListener);
        this.connectToFtpButton.addActionListener(getFtpButtonMouseListener(renderer));
        renderer.updateFilesScrollPane(getLastDirectoryFromScroll());
    }

    public DefaultListModel<Directory> createLocalDirectoryLinks(FileSystem fs,
                                                                 File defaultPath) throws FileSystemException {
        DefaultListModel<Directory> labelJList = new DefaultListModel<>();
        labelJList.addElement(new LocalDirectory(fs, defaultPath.toPath()));
        return labelJList;
    }

    /**
     * Метод вызывается при клике на определенный элемент на панели с директориями.
     * При это если пользователь нажал на не листовую директорию,
     * то директории перестают отображаться ровно до выбранной и на панели с просмоторщиком файлов начинают
     * отображаться файлы, выбранной директории.
     */
    // todo не с первого раза работают кнопки, может попробовать другой листенер?
    private MouseAdapter getDirectoryListener(Renderer renderer) {
        // тест кейс:
        // 1. нажимаем на директорию в середине и у нас удаляется хвост (причем, чтобы память не текла, надо еще удалть ссылки на обхекты)
        // 2. нажимаем на последнюю директорию и ничего не меняется И директории не перестраиваются.
        // 3. Зашли в поддерево, вышли из него -> зашли в более глубокое

        return new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JList<Directory> source = (JList<Directory>) e.getSource();
                // убираем таб с превью
                PreviewPanel.unvisible();
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

    private ActionListener getFtpButtonMouseListener(Renderer renderer) {
        return e -> {
            String ftpPath = JOptionPane.showInputDialog(connectToFtpButton,
                    new String[]{"Формат: ftp://user:password@host:port"},
                    "Введите данные подключения к ftp серверу",
                    JOptionPane.QUESTION_MESSAGE
            );
            tryToConnectToFtp(ftpPath, renderer);
        };
    }

    private void tryToConnectToFtp(String ftpPath, Renderer renderer) {
        try {
            if (StringUtils.isNotBlank(ftpPath)) {
                renderer.clearFileScrollPane();
                PreviewPanel.unvisible();
                FileObject fileObject = VFS.getManager().resolveFile(ftpPath, new FileSystemOptions());
                FTPDirectory directory = new FTPDirectory(fileObject);
                getClearedDirectory().addElement(directory);
                renderer.updateFilesScrollPane(directory);
                connectToFtpButton.setText("Disconnect");
                changeButtonActionListener(getLocalButtonMouseListener(renderer));
            }
        } catch (FileSystemException p) {
            String ftpPathNew = JOptionPane.showInputDialog(connectToFtpButton,
                    new String[]{"Извините, попробуйте снова", "Формат: ftp://user:password@host:port"},
                    "Введите данные подключения к ftp серверу",
                    JOptionPane.QUESTION_MESSAGE
            );
            tryToConnectToFtp(ftpPathNew, renderer);
            p.printStackTrace();
        }
    }

    private ActionListener getLocalButtonMouseListener(Renderer renderer) {
        return e -> {
            renderer.clearFileScrollPane();
            PreviewPanel.unvisible();
            File defaultPath = FileSystemView.getFileSystemView().getRoots()[0];
            FileSystem fs = defaultPath.toPath().getFileSystem();
            getClearedDirectory().addElement(new LocalDirectory(fs, defaultPath.toPath()));
            connectToFtpButton.setText("Connect to Ftp");
            changeButtonActionListener(getFtpButtonMouseListener(renderer));
            renderer.updateFilesScrollPane(getLastDirectoryFromScroll());
        };
    }

    private void changeButtonActionListener(ActionListener listenerForAdding) {
        if (connectToFtpButton.getActionListeners().length > 0) {
            var listener = connectToFtpButton.getActionListeners()[0];
            connectToFtpButton.removeActionListener(listener);
            connectToFtpButton.addActionListener(listenerForAdding);
        }
    }

    private DefaultListModel<Directory> getClearedDirectory() {
        JList<Directory> displayDirectory = (JList<Directory>) jScrollPane.getViewport().getView();
        DefaultListModel<Directory> sourceModel = (DefaultListModel<Directory>) displayDirectory.getModel();
        sourceModel.clear();
        return sourceModel;
    }

    public Directory getLastDirectoryFromScroll() {
        JList<Directory> displayDirectory = (JList<Directory>) jScrollPane.getViewport().getView();
        if (displayDirectory != null) {
            DefaultListModel<Directory> sourceModel = (DefaultListModel<Directory>) displayDirectory.getModel();
            if (sourceModel != null) {
                return sourceModel.lastElement();
            }
        }
        return null;
    }

    public JPanel getMainDirectoryPane() {
        return mainDirectoryPane;
    }

    public JScrollPane getScrollPane() {
        return jScrollPane;
    }
}
