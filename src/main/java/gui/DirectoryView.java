package gui;

import model.Directory;
import model.FtpFileDirectory;
import model.FtpServerOptionPane;
import model.LocalFileDirectory;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.*;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.List;

public class DirectoryView implements View {
    /**
     * The leftmost panel, which is located from top to bottom:
     * - Scroll with rooted directories (It is relevant for the OS with several disks) {@link #rootsScrollPane}
     * - Scroll with open directories {@link #directoryScrollPane}
     * - The button for connecting/disconnecting from the ftp server {@link #connectToFtpButton}
     */
    private final JSplitPane mainDirectoryPane;
    /**
     * Scroll with rooted directories (It is relevant for the OS with several disks) {@link #rootsScrollPane}
     */
    private final JScrollPane rootsScrollPane;
    /**
     * Scroll with open directories {@link #directoryScrollPane}
     */
    private JScrollPane directoryScrollPane;
    /**
     * The button for connecting/disconnecting from the ftp server {@link #connectToFtpButton}
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
     * The method is called when a specific element is clicked on the panel with directories.
     * And if the user clicked on a non-leaf directory,
     * the directories stop being displayed exactly until the selected one.
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
     * Deletes the leaf directory.
     */
    public static void removeLastElementFromDirectory(Renderer renderer) {
        Directory directory = renderer.squeezeDirectoriesByOne();
        renderer.updateFilesScrollPane(directory);
    }

    /**
     * Listener for the connecting to ftp server button.
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
                changeButtonActionListener(disconnectMouseListener(ftpClient, renderer));
            }
        } catch (UnknownHostException e) {
            showErrorMessage("Invalid host, try again", e);
        } catch (NumberFormatException e) {
            showErrorMessage("Invalid port", e);
        } catch (ConnectException e) {
            showErrorMessage(e.getMessage(), e);
        } catch (IOException e) {
            showErrorMessage("Unknown error, try later", e);
        }
    }

    private void showErrorMessage(String message, Exception e) {
        JOptionPane optionPane = new JOptionPane(message, JOptionPane.ERROR_MESSAGE);
        JDialog dialog = optionPane.createDialog("Error while connecting");
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);
        e.printStackTrace();
    }

    private FTPClient createFtpClient(FtpServerOptionPane.FtpServerOption option) throws IOException {
        FTPClient ftpClient = new FTPClient();
        ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out), true));
        if (option.getPort() != null) {
            ftpClient.connect(option.getHost(), option.getPort());
        } else {
            ftpClient.connect(option.getHost());
        }
        ftpClient.enterLocalPassiveMode();
        ftpClient.login(option.getLogin(), option.getPassword());
        ftpClient.setAutodetectUTF8(true);
        if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
            throw new ConnectException(ftpClient.getReplyString());
        }
        return ftpClient;
    }

    /**
     * The action responsible for the disconnecting button from the ftp server
     * and returning to the local root directory.
     */
    private ActionListener disconnectMouseListener(FTPClient ftpClient, Renderer renderer) {
        return e -> {
            try {
                ftpClient.disconnect();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
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
