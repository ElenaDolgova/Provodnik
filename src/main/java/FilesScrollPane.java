import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class FilesScrollPane {
    private final JPanel mainFileScrollPane;
    private final JScrollPane jScrollPane;
    private final JTextField textField;

    public FilesScrollPane() {
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(Dimensions.DIRECTORY_SCROLL_PANE_WIDTH + 3, 1, Dimensions.FILE_SCROLL_PANE_WIDTH, Dimensions.FILE_SCROLL_PANE_HEIGHT);
        scrollPane.setLayout(new ScrollPaneLayout());
        this.jScrollPane = scrollPane;
        this.mainFileScrollPane = new JPanel(new BorderLayout());
        this.textField = new JTextField();
        this.mainFileScrollPane.add(jScrollPane, BorderLayout.CENTER);
        this.mainFileScrollPane.add(textField, BorderLayout.NORTH);
    }

    public void init(Renderer renderer) {
        this.textField.addActionListener(getTextFiledListener(renderer));
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
                    JList<Link> source = (JList<Link>) e.getSource();
                    Link displayFiles = source.getSelectedValue();
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

    public JPanel getMainFileScrollPane() {
        return mainFileScrollPane;
    }
}
