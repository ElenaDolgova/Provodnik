package gui;

import model.Directory;
import model.FtpFileDirectory;
import model.FtpServerOptionPane;
import model.LocalFileDirectory;
import org.apache.commons.net.ftp.*;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.List;

public class DirectoryView {
    /**
     * Самая левая панель, на которой расположены сверху вниз:
     * - Скролл с рутовыми директориями (актуально для ос имеешь несколько основыных дисков) {@link #rootsScrollPane}
     * - Скролл с путем открытых директорий {@link #directoryScrollPane}
     * - Кнопка подключения/отключения от ftp сервера {@link #connectToFtpButton}
     */
    private final JSplitPane mainDirectoryPane;
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


    public DirectoryView() {
        this.rootsScrollPane = initializeRootScrollPane();
        this.directoryScrollPane = initializeDirectoryScrollPane();
        this.connectToFtpButton = new JButton("Connect to ftp");

        JPanel lowPanel = new JPanel(new BorderLayout());
        lowPanel.add(this.connectToFtpButton, BorderLayout.SOUTH);
        lowPanel.add(this.directoryScrollPane, BorderLayout.CENTER);
        this.mainDirectoryPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                rootsScrollPane,
                lowPanel
        );
    }

    private JScrollPane initializeRootScrollPane() {
        List<Directory> allRootDirectory = getAllRootDirectories();
        DefaultListModel<Directory> defaultRootList = new DefaultListModel<>();
        defaultRootList.addAll(allRootDirectory);
        JList<Directory> displayRootDirectory = new JList<>(defaultRootList);
        displayRootDirectory.setSelectedIndex(defaultRootList.getSize() - 1);
        JScrollPane rootsScrollPane = new JScrollPane(displayRootDirectory);
        rootsScrollPane.setLayout(new ScrollPaneLayout());
        return rootsScrollPane;
    }

    private List<Directory> getAllRootDirectories() {
        List<Directory> allRootDirectory = new ArrayList<>();
        for (File defPath : File.listRoots()) {
            FileSystem fs = defPath.toPath().getFileSystem();
            allRootDirectory.add(new LocalFileDirectory(fs, defPath.toPath()));
        }
        return allRootDirectory;
    }

    private JScrollPane initializeDirectoryScrollPane() {
        DefaultListModel<Directory> labelJList = new DefaultListModel<>();
        JList<Directory> displayDirectory = new JList<>(labelJList);
        JScrollPane scrollPane = new JScrollPane(displayDirectory);
        scrollPane.setLayout(new ScrollPaneLayout());
        directoryScrollPane = scrollPane;
        return scrollPane;
    }

    public void init(Renderer renderer) {
        MouseListener mouseListener = getDirectoryListener(renderer);
        directoryScrollPane.getViewport().getView().addMouseListener(mouseListener);
        rootsScrollPane.getViewport().getView().addMouseListener(getRootDirectoryListener(renderer));
        connectToFtpButton.addActionListener(getFtpButtonMouseListener(renderer));
        renderer.updateFilesScrollPane(getLastDirectoryFromScroll(rootsScrollPane));
    }

    /**
     * Метод вызывается при клике на определенный элемент на панели с директориями.
     * При это если пользователь нажал на не листовую директорию,
     * то директории перестают отображаться ровно до выбранной, и на панели с просмоторщиком файлов начинают
     * отображаться файлы выбранной директории.
     */
    private MouseAdapter getDirectoryListener(Renderer renderer) {
        return new MouseAdapter() {
            public void mouseClicked(MouseEvent inputEvent) {
                if (inputEvent.getClickCount() == 2) {
                    JList<Directory> source = (JList<Directory>) inputEvent.getSource();
                    // обновляем содержимое панели с файлами
                    renderer.updateFilesScrollPane(source.getSelectedValue());
                    //схлопываем директорию до нажатой
                    renderer.squeezeDirectories(source.getSelectedIndex());
                }
            }
        };
    }

    private MouseAdapter getRootDirectoryListener(Renderer renderer) {
        return new MouseAdapter() {
            public void mouseClicked(MouseEvent inputEvent) {
                if (inputEvent.getClickCount() == 2) {
                    JList<Directory> source = (JList<Directory>) inputEvent.getSource();
                    // обновляем содержимое панели с файлами
                    renderer.updateFilesScrollPane(source.getSelectedValue());
                    getClearedDirectory(directoryScrollPane);
                }
            }
        };
    }

    /**
     * Удаляет листовую директорию
     */
    public static void removeLastElementFromDirectory(Renderer renderer) {
        //схлопываем директорию до нажатой
        Directory directory = renderer.squeezeDirectoriesByOne();
        // обновляем содержимое панели с файлами
        renderer.updateFilesScrollPane(directory);
    }

    /**
     * Листенер для кнопки подключения к ftp серверу
     */
    private ActionListener getFtpButtonMouseListener(Renderer renderer) {
        return e -> {
            FtpServerOptionPane optionPane = new FtpServerOptionPane();
            FtpServerOptionPane.FtpServerOption option =
                    optionPane.showConfirmDialog(connectToFtpButton, "Enter the data for connecting to the ftp server");
            SwingUtilities.invokeLater(() -> {
                renderer.setSpinnerVisible(true);
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() {
                        tryToConnectToFtp(option, renderer);
                        return null;
                    }

                    @Override
                    protected void done() {
                        SwingUtilities.invokeLater(() -> renderer.setSpinnerVisible(false));
                    }
                }.execute();
            });
        };
    }

    private void tryToConnectToFtp(FtpServerOptionPane.FtpServerOption option, Renderer renderer) {
        try {
            if (option != null && option.getHost() != null && option.getHost().length() != 0) {
                FTPClient ftpClient = createFtpClient(option);
                renderer.clearFileScrollPane();
                FtpFileDirectory directory = new FtpFileDirectory(ftpClient, "/", null);
                getClearedDirectory(directoryScrollPane);
                getClearedDirectory(rootsScrollPane).addElement(directory);
                renderer.updateFilesScrollPane(directory);
                connectToFtpButton.setText("Disconnect");
                changeButtonActionListener(disconnectMouseListener(renderer));
            }
        } catch (UnknownHostException p) {
            FtpServerOptionPane optionPane = new FtpServerOptionPane();
            tryToConnectToFtp(optionPane.showConfirmDialog(connectToFtpButton,
                    "There is invalid connect data to ftp server. Try again"), renderer);
            p.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private FTPClient createFtpClient(FtpServerOptionPane.FtpServerOption option) throws IOException {
        FTPClient ftpClient = new FTPClient();
        ftpClient.setAutodetectUTF8(true);
        ftpClient.enterLocalPassiveMode();
        if (option.getPort() != null) {
            ftpClient.connect(option.getHost(), option.getPort());
        } else {
            ftpClient.connect(option.getHost());
        }
        ftpClient.login(option.getLogin(), option.getPassword());
        return ftpClient;
    }

    /**
     * Действие, отвечающие за кнопку отключение от ftp сервера и возвращение в локальную рутовую директорию
     */
    private ActionListener disconnectMouseListener(Renderer renderer) {
        return e -> {
            FtpFileDirectory.clearCache();
            renderer.clearFileScrollPane();
            List<Directory> allRootDirectories = getAllRootDirectories();
            getClearedDirectory(rootsScrollPane).addAll(allRootDirectories);
            JList<Directory> directoryJList = (JList<Directory>) rootsScrollPane.getViewport().getView();
            directoryJList.setSelectedIndex(allRootDirectories.size() - 1);
            getClearedDirectory(directoryScrollPane);

            connectToFtpButton.setText("Connect to Ftp");
            changeButtonActionListener(getFtpButtonMouseListener(renderer));
            renderer.updateFilesScrollPane(getLastDirectoryFromScroll(rootsScrollPane));
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

    @Nullable
    public Directory getLastDirectoryFromScroll(JScrollPane scrollPane) {
        JList<Directory> displayDirectory = (JList<Directory>) scrollPane.getViewport().getView();
        if (displayDirectory != null) {
            DefaultListModel<Directory> sourceModel = (DefaultListModel<Directory>) displayDirectory.getModel();
            if (sourceModel != null && sourceModel.getSize() != 0) {
                return sourceModel.lastElement();
            }
        }
        return null;
    }

    public JScrollPane getDirectoryScrollPane() {
        return directoryScrollPane;
    }

    public JScrollPane getRootsScrollPane() {
        return rootsScrollPane;
    }

    public JSplitPane getMainDirectoryPane() {
        return mainDirectoryPane;
    }
}
