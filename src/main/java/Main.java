import org.apache.commons.vfs2.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.StreamSupport;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class Main {

//    public static void main(String[] args) throws IOException {
//        FileSystemManager fsManager = VFS.getManager();
//        FileObject zipFile = fsManager.resolveFile("/Users/elena-dolgova/Desktop/result.zip");
//        FileObject fs = fsManager.createFileSystem(zipFile);
//        listAllFiles(fs, 0);
//        FileObject nestedFs = fsManager.createFileSystem(fs.resolveFile("/result /result.zip"));
//        boolean fileExists = nestedFs.resolveFile("/result/folder/folder1/IMG_0382.jpg").exists();
//        System.out.println(fileExists);
//    }

//    private static void listAllFiles(FileObject fs) throws FileSystemException {
//        System.out.println(fs.getName());
//        if (fs.isFolder() && fs.getChildren().length != 0) {
//            for (FileObject child : fs.getChildren()) {
//                listAllFiles(child);
//            }
//        }
//    }

//    private static void listAllFiles(FileObject fs, int depth) throws FileSystemException {
//        System.out.println(fs.getName());
//        if (fs.isFolder() && fs.getChildren().length != 0 && depth < 2) {
//            for (FileObject child : fs.getChildren()) {
//                listAllFiles(child, depth++);
//            }
//        }
////        ++depth;
//    }

//    public static void main(String[] args) throws IOException {
//        java.nio.file.FileSystem fs = FileSystems.newFileSystem(Paths.get("/Users/elena-dolgova/Desktop/result.zip"), Collections.emptyMap());
//        printAllFiles(fs);
//        java.nio.file.FileSystem nestedFs = FileSystems.newFileSystem(fs.getPath("/result /result.zip"), Collections.emptyMap());
//        System.out.println("GOVNOOOOOOO");
//        printAllFiles(nestedFs);
//        byte[] jpeg = Files.readAllBytes(nestedFs.getPath("/result/folder/folder1/IMG_0382.jpg"));
//        System.out.println("sasat");
//
////        java.nio.file.FileSystem nestedFolderFs = FileSystems.newFileSystem(fs.getPath("/result /insideResult"), Collections.emptyMap());
////        printAllFiles(nestedFolderFs);
//    }

    private static void printAllFiles(java.nio.file.FileSystem fs) {
        StreamSupport.stream(fs.getRootDirectories().spliterator(), false)
                .flatMap((it) -> {
                    try {
                        return Files.walk(it, 2);
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                }).forEach(p-> {
                    File file = new File(p.toString());
                    System.out.println(p.getFileName());
        });
//                .forEach(System.out::println);
    }

    public static void main(String[] args) {
        JFrame.setDefaultLookAndFeelDecorated(true);
        final Main main = new Main();
        javax.swing.SwingUtilities.invokeLater(main::init);
    }

    private void init() {
        JFrame GLOBAL_FRAME = new JFrame("Vitamin Well");
        DirectoryScrollPane DIRECTORY_SCROLL_PANE = new DirectoryScrollPane();

        FilesScrollPane FILES_SCROLL_PANE = new FilesScrollPane();
        PreviewPanel PREVIEW_PANEL = new PreviewPanel();

        GLOBAL_FRAME.setLayout(new BorderLayout());
        GLOBAL_FRAME.setDefaultCloseOperation(EXIT_ON_CLOSE);
        GLOBAL_FRAME.setPreferredSize(new Dimension(Dimensions.MAIN_WIDTH, Dimensions.MAIN_HEIGHT));
        GLOBAL_FRAME.getContentPane().add(DIRECTORY_SCROLL_PANE.getScrollPane(), BorderLayout.WEST);
        GLOBAL_FRAME.getContentPane().add(FILES_SCROLL_PANE.getScrollPane(), BorderLayout.CENTER);
        PREVIEW_PANEL.init(GLOBAL_FRAME);

        Renderer renderer = new Renderer(GLOBAL_FRAME, DIRECTORY_SCROLL_PANE, FILES_SCROLL_PANE, PREVIEW_PANEL);

        DIRECTORY_SCROLL_PANE.init(renderer);

        GLOBAL_FRAME.pack();
        GLOBAL_FRAME.setVisible(true);
    }
}
