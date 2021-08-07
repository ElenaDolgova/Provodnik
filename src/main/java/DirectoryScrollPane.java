import org.apache.commons.net.ftp.*;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneLayout;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileSystemView;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.List;

public class DirectoryScrollPane {
    /**
     * Самая левая панель, на которой расположены сверху вниз:
     * - Скролл с рутовыми директориями (актуально для ос имеешь несколько основыных дисков) {@link rootsScrollPane}
     * - Скролл с путем открытых директорий {@link directoryScrollPane}
     * - Кнопка подключения/отключения от ftp сервера {@link connectToFtpButton}
     */
    private final JPanel mainDirectoryPane;
    /**
     * Скролл с рутовыми директориями (актуально для ос имеешь несколько основыных дисков)
     */
    private final JScrollPane rootsScrollPane;
    /**
     * Скролл с путем открытых директорий
     */
    private JScrollPane directoryScrollPane;
    /**
     * Кнопка подключения/отключения от ftp сервера
     */
    private final JButton connectToFtpButton;


    public DirectoryScrollPane() {
        this.rootsScrollPane = initializeRootScrollPane();
        this.connectToFtpButton = new JButton("Connect to ftp");

        this.mainDirectoryPane = new JPanel(new BorderLayout());
        this.mainDirectoryPane.add(this.connectToFtpButton, BorderLayout.SOUTH);
        this.mainDirectoryPane.add(this.directoryScrollPane, BorderLayout.CENTER);
        this.mainDirectoryPane.add(this.rootsScrollPane, BorderLayout.NORTH);
    }

    private JScrollPane initializeRootScrollPane() {
        List<Directory> allRootDirectory = getAllRootDirectories();
        DefaultListModel<Directory> defaultRootList = new DefaultListModel<>();
        defaultRootList.addAll(getAllRootDirectories());
        JList<Directory> displayRootDirectory = new JList<>(defaultRootList);
        JScrollPane rootsScrollPane = new JScrollPane(displayRootDirectory);
        rootsScrollPane.setLayout(new ScrollPaneLayout());
        this.directoryScrollPane = initializeDirectoryScrollPane(allRootDirectory);

        return rootsScrollPane;
    }

    private List<Directory> getAllRootDirectories() {
        List<Directory> allRootDirectory = new ArrayList<>();
        for (File defPath : FileSystemView.getFileSystemView().getRoots()) {
            FileSystem fs = defPath.toPath().getFileSystem();
            allRootDirectory.add(new LocalDirectory(fs, defPath.toPath()));
        }
        return allRootDirectory;
    }

    private JScrollPane initializeDirectoryScrollPane(List<Directory> allRootDirectory) {
        DefaultListModel<Directory> labelJList = new DefaultListModel<>();
        labelJList.addElement(allRootDirectory.get(0));
        JList<Directory> displayDirectory = new JList<>(labelJList);
        JScrollPane scrollPane = new JScrollPane(displayDirectory);
        scrollPane.setLayout(new ScrollPaneLayout());
        this.directoryScrollPane = scrollPane;
        return scrollPane;
    }

    public void init(JFrame GLOBAL_FRAME, Renderer renderer) {
        MouseListener mouseListener = getDirectoryListener(renderer);
        Component viewport = directoryScrollPane.getViewport().getView();
        viewport.addMouseListener(mouseListener);
        this.connectToFtpButton.addActionListener(getFtpButtonMouseListener(renderer));
        renderer.updateFilesScrollPane(getLastDirectoryFromScroll());
        GLOBAL_FRAME.getContentPane().add(mainDirectoryPane, BorderLayout.WEST);
    }

    /**
     * Метод вызывается при клике на определенный элемент на панели с директориями.
     * При это если пользователь нажал на не листовую директорию,
     * то директории перестают отображаться ровно до выбранной и на панели с просмоторщиком файлов начинают
     * отображаться файлы, выбранной директории.
     */
    private MouseAdapter getDirectoryListener(Renderer renderer) {
        // тест кейс:
        // 1. нажимаем на директорию в середине и у нас удаляется хвост (причем, чтобы память не текла, надо еще удалть ссылки на обхекты)
        // 2. нажимаем на последнюю директорию и ничего не меняется И директории не перестраиваются.
        // 3. Зашли в поддерево, вышли из него -> зашли в более глубокое

        return new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    JList<Directory> source = (JList<Directory>) e.getSource();
                    // обновляем содержимое панели с файлами
                    renderer.updateFilesScrollPane(source.getSelectedValue());
                    //схлопываем директорию до нажатой
                    DefaultListModel<Directory> sourceModel = (DefaultListModel<Directory>) source.getModel();
                    for (int i = sourceModel.getSize() - 1; i > source.getSelectedIndex(); --i) {
                        sourceModel.remove(i);
                    }
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
            SwingUtilities.invokeLater(() -> {
                renderer.setThrobberVisible(true);
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() {
                        tryToConnectToFtp(ftpPath, renderer);
                        return null;
                    }

                    @Override
                    protected void done() {
                        SwingUtilities.invokeLater(() -> renderer.setThrobberVisible(false));
                    }
                }.execute();
            });
        };
    }

    private void tryToConnectToFtp(String ftpPath, Renderer renderer) {
        try {
            if (ftpPath != null && ftpPath.length() != 0) {
                FTPClient f = new FTPClient(); //ftp.bmc.com  ftp.efix.pl - картинка  normacs.ru - подпапки
                // aux.detewe.ru с zip
                // ftp://anonymous@ftp.bmc.com
                ftpPath = "aux.detewe.ru";
                f.connect(ftpPath);
                f.login("anonymous", "");
                renderer.clearFileScrollPane();
                FTPDirectory directory = new FTPDirectory(f, "/", "/");
                getClearedDirectory(directoryScrollPane).addElement(directory);
                renderer.updateFilesScrollPane(directory);
                connectToFtpButton.setText("Disconnect");
                changeButtonActionListener(disconnectMouseListener(renderer));
            }
        } catch (UnknownHostException p) {
            String ftpPathNew = JOptionPane.showInputDialog(connectToFtpButton,
                    new String[]{"Извините, попробуйте снова", "Формат: ftp://user:password@host:port"},
                    "Введите данные подключения к ftp серверу",
                    JOptionPane.QUESTION_MESSAGE
            );
            tryToConnectToFtp(ftpPathNew, renderer);
            p.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Действие, отвечающие за кнопку отключение от ftp сервера и возвращение в локальную рутовую директорию
     */
    private ActionListener disconnectMouseListener(Renderer renderer) {
        return e -> {
            renderer.clearFileScrollPane();
            List<Directory> allRootDirectories = getAllRootDirectories();
            getClearedDirectory(rootsScrollPane).addAll(allRootDirectories);
            getClearedDirectory(directoryScrollPane).addElement(allRootDirectories.get(0));

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

    /**
     * @return Очищенный контейнер, в который могут быть положены новые данные, для отображения
     * на панели с диреткориями.
     */
    private DefaultListModel<Directory> getClearedDirectory(JScrollPane scrollPane) {
        JList<Directory> displayDirectory = (JList<Directory>) scrollPane.getViewport().getView();
        DefaultListModel<Directory> sourceModel = (DefaultListModel<Directory>) displayDirectory.getModel();
        sourceModel.clear();
        return sourceModel;
    }

    public Directory getLastDirectoryFromScroll() {
        JList<Directory> displayDirectory = (JList<Directory>) directoryScrollPane.getViewport().getView();
        if (displayDirectory != null) {
            DefaultListModel<Directory> sourceModel = (DefaultListModel<Directory>) displayDirectory.getModel();
            if (sourceModel != null) {
                return sourceModel.lastElement();
            }
        }
        return null;
    }

    public JScrollPane getScrollPane() {
        return directoryScrollPane;
    }
}
