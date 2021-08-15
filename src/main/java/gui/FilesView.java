package gui;

import exception.FileProcessingException;
import model.Directory;

import javax.imageio.ImageIO;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneLayout;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.EventObject;

import static java.awt.event.KeyEvent.VK_ENTER;
import static java.awt.event.KeyEvent.VK_ESCAPE;

public class FilesView {
    private final JPanel mainFileScrollPane;
    private final JScrollPane jScrollPane;
    private final JPanel northPanel;
    private final JTextField textField;
    private final JLabel spinner;
    private final ImageIcon folderIcon;
    private final PreviewPanelView previewPanelView;

    public FilesView(PreviewPanelView previewPanelView) {
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setLayout(new ScrollPaneLayout());
        this.jScrollPane = scrollPane;
        this.mainFileScrollPane = new JPanel(new BorderLayout());
        this.northPanel = new JPanel(new BorderLayout());
        this.previewPanelView = previewPanelView;

        this.textField = new JTextField();
        URL loadingGifUrl = getClass().getClassLoader().getResource("img/loading.gif");
        if (loadingGifUrl == null) {
            throw new IllegalStateException("Unable to open img/loading.gif");
        }
        this.spinner = new JLabel(
                new ImageIcon(
                        new ImageIcon(loadingGifUrl)
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
            InputStream resource = getClass().getClassLoader().getResourceAsStream("img/folder.png");
            if (resource == null) {
                throw new IllegalStateException("Unable to open img/folder.png");
            }
            Image image = ImageIO.read(resource);
            folderIcon = new ImageIcon(image.getScaledInstance(15, 15, Image.SCALE_FAST));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.folderIcon = folderIcon;
    }

    public void init(Renderer renderer) {
        this.textField.addActionListener(getTextFiledListener(renderer));

        DefaultListModel<Directory> defaultListModel = new DefaultListModel<>();
        JList<Directory> displayFiles = new JList<>(defaultListModel);
        displayFiles.setCellRenderer(new FileListCellRenderer());
        jScrollPane.setViewportView(displayFiles);
        displayFiles.addMouseListener(getMouseDisplayFilesListener(renderer));
        displayFiles.addListSelectionListener(listSelectionEvent -> previewEvent(listSelectionEvent, renderer));
        displayFiles.addKeyListener(displayFilesListener(renderer));
    }

    private ActionListener getTextFiledListener(Renderer renderer) {
        return e -> {
            String textFieldValue = textField.getText();
            renderer.updateFilesScrollPane(textFieldValue);
        };
    }

    public MouseAdapter getMouseDisplayFilesListener(Renderer renderer) {
        return new MouseAdapter() {
            public void mouseClicked(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
                    fileScrollEvent(mouseEvent, renderer);
                }
            }
        };
    }

    /**
     * Листенер для хождения по файлам в файловом скролле с помощью клавищ
     */
    public KeyListener displayFilesListener(Renderer renderer) {
        return new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
                System.out.println("keyTyped " + keyEvent.getKeyCode());
            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == VK_ESCAPE) {
                    DirectoryView.removeLastElementFromDirectory(renderer);
                } else if (keyEvent.getKeyCode() == VK_ENTER) {
                    fileScrollEvent(keyEvent, renderer);
                }
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {
                previewEvent(keyEvent, renderer);
            }
        };
    }

    /**
     * Метод отрисовывает новую директорию на скролле с директориями и заполняет файлами файловый скролл
     * ИЛИ
     * выводит превью файла на панель с превью
     *
     * @param inputEvent выбранный элемент файлового скролла
     */
    private void fileScrollEvent(InputEvent inputEvent, Renderer renderer) {
        previewPanelView.hideContent();
        JList<Directory> source = (JList<Directory>) inputEvent.getSource();
        Directory displayFiles = source.getSelectedValue();

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
                        String probeContentType = Directory.getProbeContentType(displayFiles.getPath());
                        if (probeContentType != null) {
                            renderer.updatePreviewPanel(probeContentType, displayFiles);
                        }
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

    private void previewEvent(EventObject inputEvent, Renderer renderer) {
        previewPanelView.hideContent();
        JList<Directory> source = (JList<Directory>) inputEvent.getSource();
        Directory displayFiles = source.getSelectedValue();

        if (displayFiles != null && !displayFiles.isDirectory()) {
            SwingUtilities.invokeLater(() -> {
                renderer.setSpinnerVisible(true);
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() {
                        String probeContentType = Directory.getProbeContentType(displayFiles.getPath());
                        if (probeContentType != null) {
                            renderer.updatePreviewPanel(probeContentType, displayFiles);
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

    public JPanel getMainFileScrollPane() {
        return mainFileScrollPane;
    }
}
