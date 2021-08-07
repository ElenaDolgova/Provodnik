package gui;

import exception.FileProcessingException;
import model.Directory;

import javax.imageio.ImageIO;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneLayout;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.*;
import java.io.IOException;

import static java.awt.event.KeyEvent.VK_ENTER;
import static java.awt.event.KeyEvent.VK_ESCAPE;

public class FilesScrollPane {
    private final JPanel mainFileScrollPane;
    private final JScrollPane jScrollPane;
    private final JPanel northPanel;
    private final JTextField textField;
    private final JLabel spinner;
    private final ImageIcon folderIcon;

    public FilesScrollPane() {
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setLayout(new ScrollPaneLayout());
        this.jScrollPane = scrollPane;
        this.mainFileScrollPane = new JPanel(new BorderLayout());
        this.northPanel = new JPanel(new BorderLayout());

        this.textField = new JTextField();
        this.spinner = new JLabel(
                new ImageIcon(
                        new ImageIcon(getClass().getClassLoader().getResource("loading.gif"))
                                .getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT)
                )
        );
        spinner.setVisible(false);
        this.northPanel.add(this.spinner, BorderLayout.EAST);
        this.northPanel.add(this.textField, BorderLayout.CENTER);

        this.mainFileScrollPane.add(jScrollPane, BorderLayout.CENTER);
        this.mainFileScrollPane.add(northPanel, BorderLayout.NORTH);

        ImageIcon folderIcon = null;
        try {
            Image image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("img/folder.png"));
            folderIcon = new ImageIcon(image.getScaledInstance(15, 15, Image.SCALE_FAST));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.folderIcon = folderIcon;
    }

    public void init(JFrame GLOBAL_FRAME, Renderer renderer) {
        this.textField.addActionListener(getTextFiledListener(renderer));
        GLOBAL_FRAME.getContentPane().add(mainFileScrollPane, BorderLayout.CENTER);

        DefaultListModel<Directory> defaultListModel = new DefaultListModel<>();
        JList<Directory> displayFiles = new JList<>(defaultListModel);
        displayFiles.setCellRenderer(new FileListCellRenderer());
        jScrollPane.setViewportView(displayFiles);
        displayFiles.addMouseListener(FilesScrollPane.getMouseListener(renderer));
        displayFiles.addKeyListener(FilesScrollPane.getKeyListener(renderer));
    }

    private ActionListener getTextFiledListener(Renderer renderer) {
        return e -> {
            String textFieldValue = textField.getText();
            renderer.updateFilesScrollPane(textFieldValue);
        };
    }

    public static MouseAdapter getMouseListener(Renderer renderer) {
        // тест кейс:
        // 1. нажимаем на директорию в середине и у нас удаляется хвост (причем, чтобы память не текла, надо еще удалть ссылки на обхекты)
        // 2. нажимаем на последнюю директорию и ничего не меняется И директории не перестраиваются.
        // 3. Зашли в поддерево, вышли из него -> зашли в более глубокое
        return new MouseAdapter() {
            public void mouseClicked(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
                    fileScrollEvent(mouseEvent, renderer);
                }
            }
        };
    }

    public static KeyListener getKeyListener(Renderer renderer) {
        return new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == VK_ENTER) {
                    fileScrollEvent(keyEvent, renderer);
                } else if (keyEvent.getKeyCode() == VK_ESCAPE) {
                    DirectoryScrollPane.removeLastElementFromDirectory(renderer);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        };
    }

    private static void fileScrollEvent(InputEvent inputEvent, Renderer renderer) {
        PreviewPanel.hideContent();
        JList<Directory> source = (JList<Directory>) inputEvent.getSource();
        Directory displayFiles = source.getSelectedValue();
        // todо тест кейса на добавление не повторяющихся файлов
        // тест на добавление только нового! файлов в директорию

        SwingUtilities.invokeLater(() -> {
            renderer.setSpinnerVisible(true);
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    if (displayFiles.isDirectory()) {
                        try {
                            Directory newDirectory = displayFiles.createDirectory();
                            renderer.addNewDirectory(newDirectory);
                        } catch (FileProcessingException e) {
                            renderer.showWarningPane(e);
                        }
                    } else {
                        renderer.updatePreviewPanel(
                                Directory.getProbeContentType(displayFiles.getPath()), displayFiles);
                    }
                    return null;
                }

                @Override
                protected void done() {
                    SwingUtilities.invokeLater(() -> renderer.setSpinnerVisible(false));
                }
            }.execute();
        });
    }

    public JScrollPane getScrollPane() {
        return jScrollPane;
    }

    private class FileListCellRenderer extends DefaultListCellRenderer {

        private static final long serialVersionUID = -7799441088157759804L;
        private final JLabel label;
        private final Color textSelectionColor = Color.BLACK;
        private final Color backgroundSelectionColor = Color.lightGray;
        private final Color textNonSelectionColor = Color.BLACK;
        private final Color backgroundNonSelectionColor = Color.WHITE;

        FileListCellRenderer() {
            label = new JLabel();
            label.setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean selected,
                boolean expanded) {

            Directory file = (Directory) value;
            if (((Directory) value).isDirectory()) {
                label.setIcon(folderIcon);
            } else {
                label.setIcon(UIManager.getIcon("FileView.fileIcon"));
            }
            label.setText(file.getName());

            if (selected) {
                label.setBackground(backgroundSelectionColor);
                label.setForeground(textSelectionColor);
            } else {
                label.setBackground(backgroundNonSelectionColor);
                label.setForeground(textNonSelectionColor);
            }

            return label;
        }
    }

    public JLabel getSpinner() {
        return spinner;
    }
}
