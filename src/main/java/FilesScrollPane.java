import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FilesScrollPane {
    private final JScrollPane jScrollPane;

    public FilesScrollPane() {
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(Dimensions.DIRECTORY_SCROLL_PANE_WIDTH + 3, 1, Dimensions.FILE_SCROLL_PANE_WIDTH, Dimensions.FILE_SCROLL_PANE_HEIGHT);
        scrollPane.setLayout(new ScrollPaneLayout());
        this.jScrollPane = scrollPane;
    }

    /**
     *
     */
    public static MouseAdapter getMouseListener() {
        // тест кейс:
        // 1. нажимаем на директорию в середине и у нас удаляется хвост (причем, чтобы память не текла, надо еще удалть ссылки на обхекты)
        // 2. нажимаем на последнюю директорию и ничего не меняется И директории не перестраиваются.
        // 3. Зашли в поддерево, вышли из него -> зашли в более глубокое

        return new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JList<Link> source = (JList<Link>) e.getSource();

                Link displayFiles = source.getSelectedValue();
                // todо тест кейса на добавление не повторяющихся файлов
                // тест на добавление файлов в директорию

                if (displayFiles instanceof LocalFileLink && displayFiles.isDirectory()) {
                    Directory newDirectory = new LocalDirectory(displayFiles);
                    updatedFilesForLocalDisk(newDirectory);
                    return;
                }

                // todo zip внутри zip
                try {
                    if ("application/zip".equals(Files.probeContentType(displayFiles.createPath()))) {
                        Directory newDirectory = new ZipDirectory(displayFiles);
                        updatedFilesForLocalDisk(newDirectory);
                    } else if (displayFiles.isDirectory() && displayFiles instanceof ZipFileLink zipFileLink) {
                        Directory newDirectory = new ZipDirectory(zipFileLink, zipFileLink.getZipFile());
                        updatedFilesForLocalDisk(newDirectory);
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }

                Image image = displayFiles.getImage();
                if (image != null) {
                    MainFrame.PREVIEW_PANEL.update(image);
                    return;
                }

            }
        };
    }

    private static void updatedFilesForLocalDisk(Directory newDirectory) {
        JList<Directory> displayDirectory =
                (JList<Directory>) MainFrame.DIRECTORY_SCROLL_PANE.getScrollPane().getViewport().getView();
        DefaultListModel<Directory> sourceModel = (DefaultListModel<Directory>) displayDirectory.getModel();
        if (!sourceModel.get(sourceModel.getSize() - 1).equals(newDirectory)) {
            // добавляем новую директорию на панель с директориями
            sourceModel.addElement(newDirectory);
            // обновляем панельку с фалами
            newDirectory.updateFilesScrollPane();
        }
    }

    public JScrollPane getScrollPane() {
        return jScrollPane;
    }
}
