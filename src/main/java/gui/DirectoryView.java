package gui;

import model.Directory;
import model.FtpFileDirectory;
import model.LocalFileDirectory;
import org.apache.commons.net.PrintCommandListener;
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
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class DirectoryView {
    /**
     * Самая левая панель, на которой расположены сверху вниз:
     * - Скролл с рутовыми директориями (актуально для ос имеешь несколько основыных дисков) {@link #rootsScrollPane}
     * - Скролл с путем открытых директорий {@link #directoryScrollPane}
     * - Кнопка подключения/отключения от ftp сервера {@link #connectToFtpButton}
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


    public DirectoryView() {
        this.rootsScrollPane = initializeRootScrollPane();
        this.directoryScrollPane = initializeDirectoryScrollPane();
        this.connectToFtpButton = new JButton("Connect to ftp");

        this.mainDirectoryPane = new JPanel(new BorderLayout());
        this.mainDirectoryPane.add(this.connectToFtpButton, BorderLayout.SOUTH);
        this.mainDirectoryPane.add(this.directoryScrollPane, BorderLayout.CENTER);
        this.mainDirectoryPane.add(this.rootsScrollPane, BorderLayout.NORTH);
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

    public void init(JFrame GLOBAL_FRAME, Renderer renderer) {
        MouseListener mouseListener = getDirectoryListener(renderer);
        directoryScrollPane.getViewport().getView().addMouseListener(mouseListener);
        rootsScrollPane.getViewport().getView().addMouseListener(getRootDirectoryListener(renderer));
        connectToFtpButton.addActionListener(getFtpButtonMouseListener(renderer));
        renderer.updateFilesScrollPane(getLastDirectoryFromScroll(rootsScrollPane));
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
     *
     * @param renderer
     */
    public static void removeLastElementFromDirectory(Renderer renderer) {
        //схлопываем директорию до нажатой
        Directory directory = renderer.squeezeDirectoriesByOne();
        // обновляем содержимое панели с файлами
        renderer.updateFilesScrollPane(directory);
    }

    private ActionListener getFtpButtonMouseListener(Renderer renderer) {
        return e -> {
            String ftpPath = JOptionPane.showInputDialog(connectToFtpButton,
                    new String[]{"Формат: ftp://user:password@host:port"},
                    "Введите данные подключения к ftp серверу",
                    JOptionPane.QUESTION_MESSAGE
            );
            SwingUtilities.invokeLater(() -> {
                renderer.setSpinnerVisible(true);
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() {
                        tryToConnectToFtp(ftpPath, renderer);
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

    private void tryToConnectToFtp(String ftpPath, Renderer renderer) {
        try {
            if (ftpPath != null && ftpPath.length() != 0) {
                FTPClient ftpClient = new FTPClient(); //ftp.bmc.com  ftp.efix.pl - картинка  normacs.ru - подпапки
                // aux.detewe.ru с zip
                // ftp://anonymous@ftp.bmc.com

                ftpClient.addProtocolCommandListener(
                        new PrintCommandListener(new PrintWriter(System.out), true)
                );
                ftpClient.setAutodetectUTF8(true);

                ftpPath = "aux.detewe.ru";
                ftpClient.connect(ftpPath);
                ftpClient.enterLocalPassiveMode();
                ftpClient.login("anonymous", "");
                renderer.clearFileScrollPane();
                FtpFileDirectory directory = new FtpFileDirectory(ftpClient, "/", null);
                getClearedDirectory(directoryScrollPane);
                getClearedDirectory(rootsScrollPane).addElement(directory);
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

    public Directory getLastDirectoryFromScroll(JScrollPane scrollPane) {
        JList<Directory> displayDirectory = (JList<Directory>) scrollPane.getViewport().getView();
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
