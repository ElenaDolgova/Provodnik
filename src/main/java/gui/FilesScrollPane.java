package gui;

import model.Directory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

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

            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    PreviewPanel.hideContent();
                    JList<Directory> source = (JList<Directory>) e.getSource();
                    Directory displayFiles = source.getSelectedValue();
                    // todо тест кейса на добавление не повторяющихся файлов
                    // тест на добавление только нового! файлов в директорию
                    try {
                        if (displayFiles.isDirectory()) {
                            Directory newDirectory = displayFiles.createDirectory();
                            renderer.addNewDirectory(newDirectory);
                        } else {
                            renderer.updatePreviewPanel(displayFiles.getProbeContentType(), displayFiles);
                        }
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        };
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
