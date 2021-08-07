import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

public class DirectoryTest {
    private final static String directoryTestPath = "src/test/resources/DirectoryTest";

//    @Test
//    @DisplayName("Проверяем пустую директорию")
//    public void localDirectoryEmptyTest() {
//        File file = new File(directoryTestPath + "/localDirectoryTest/folder1");
//        Path path = Paths.get(file.getAbsolutePath());
//        FileSystem fs = path.getFileSystem();
//        model.Directory localDirectory = new LocalDirectory(fs, path);
//
//        Assertions.assertEquals("folder1", localDirectory.getDirectoryName());
//        Assertions.assertTrue(localDirectory.getFiles().isEmpty());
//        Assertions.assertTrue(localDirectory.getFiles("zip").isEmpty());
//        Assertions.assertTrue(localDirectory.getFiles("txt").isEmpty());
//    }
//
//    @Test
//    @DisplayName("Проверяем директорию, состоящую из папок")
//    public void localDirectoryFolderTest() {
//        File file = new File(directoryTestPath + "/localDirectoryTest");
//        Path path = Paths.get(file.getAbsolutePath());
//        FileSystem fs = path.getFileSystem();
//        model.Directory localDirectory = new LocalDirectory(fs, path);
//
//        // Проверили наличие двух папок в директории
//        Assertions.assertEquals("localDirectoryTest", localDirectory.getDirectoryName());
//        Assertions.assertEquals(2, localDirectory.getFiles().size());
//
//        localDirectory.getFiles().forEach(
//                folder -> {
//                    Assertions.assertTrue(folder.isDirectory());
//                    Assertions.assertNull(folder.getProbeContentType());
//                    Assertions.assertEquals(Paths.get(file.getAbsolutePath() + "/" + folder.getName()), folder.getPath());
//                    try {
//                        model.Directory directory = folder.createDirectory();
//                        Assertions.assertEquals(folder.getName(), directory.getDirectoryName());
//                        Assertions.assertTrue(directory.getFiles().isEmpty());
//                    } catch (IOException e) {
//                        throw new RuntimeException("There is failed test: localDirectoryTest");
//                    }
//                });
//    }
//
//    @Test
//    @DisplayName("Проверяем директорию, состоящую из картинок")
//    public void localDirectoryImageAndTextTest() {
//        String localDirectoryImageAndTextTest = "localDirectoryImageAndTextTest";
//        File file = new File(directoryTestPath + "/" + localDirectoryImageAndTextTest);
//        Path path = Paths.get(file.getAbsolutePath());
//        FileSystem fs = path.getFileSystem();
//        model.Directory localDirectory = new LocalDirectory(fs, path);
//
//        // Проверили наличие двух папок в директории
//        Assertions.assertEquals(localDirectoryImageAndTextTest, localDirectory.getDirectoryName());
//        Assertions.assertEquals(2, localDirectory.getFiles().size());
//
//        localDirectory.getFiles().forEach(
//                folder -> {
//                    Assertions.assertFalse(folder.isDirectory());
//                    Assertions.assertTrue(folder.getProbeContentType().contains("image"));
//                    Assertions.assertEquals(Paths.get(file.getAbsolutePath() + "/" + folder.getName()), folder.getPath());
//                    try {
//                        Assertions.assertTrue(folder.getInputStreamOfFile().readAllBytes().length > 0);
//                    } catch (IOException e) {
//                        throw new RuntimeException("There is failed test: localDirectoryImageAndTextTest");
//                    }
//                });
//    }
//
//    @Test
//    @DisplayName("Ходим по папкам")
//    public void localDirectoryFoldersTest() {
//        String localDirectoryImageAndTextTest = "localDirectoryFoldersTest";
//        File file = new File(directoryTestPath + "/" + localDirectoryImageAndTextTest);
//        Path path = Paths.get(file.getAbsolutePath());
//        FileSystem fs = path.getFileSystem();
//        model.Directory localDirectory = new LocalDirectory(fs, path);
//
//        // Проверили наличие двух папок в корневой директории: folder1 и folder2
//        Assertions.assertEquals(localDirectoryImageAndTextTest, localDirectory.getDirectoryName());
//        Collection<Link> files1 = localDirectory.getFiles();
//        Assertions.assertEquals(2, files1.size());
//        files1.forEach(folder -> Assertions.assertTrue(folder.isDirectory()));
//
//        files1.forEach(folder -> {
//                    if (folder.getName().equals("folder1")) {
//                        try {
//                            model.Directory directory = folder.createDirectory();
//                            Collection<Link> files11 = directory.getFiles();
//                            Assertions.assertEquals(3, files11.size());
//
//                            files11.forEach(folder11 -> {
//                                        if (folder11.getName().equals("folder11")) {
//                                            Assertions.assertTrue(folder11.isDirectory());
//                                            try {
//                                                model.Directory directory11 = folder11.createDirectory();
//                                                Assertions.assertTrue(directory11.getFiles().isEmpty());
//                                            } catch (IOException e) {
//                                                throw new RuntimeException("There is failed test: localDirectoryFoldersTest");
//                                            }
//                                        } else if (folder11.getName().equals("folder12")) {
//                                            Assertions.assertTrue(folder11.isDirectory());
//                                            try {
//                                                model.Directory directory11 = folder11.createDirectory();
//                                                Assertions.assertEquals(2, directory11.getFiles().size());
//                                            } catch (IOException e) {
//                                                throw new RuntimeException("There is failed test: localDirectoryFoldersTest");
//                                            }
//                                        } else if (folder11.getName().equals("folder13")) {
//                                            Assertions.assertTrue(folder11.isDirectory());
//                                            try {
//                                                model.Directory directory11 = folder11.createDirectory();
//                                                Assertions.assertEquals(1, directory11.getFiles().size());
//                                            } catch (IOException e) {
//                                                throw new RuntimeException("There is failed test: localDirectoryFoldersTest");
//                                            }
//                                        } else {
//                                            throw new RuntimeException("There is no another folders: localDirectoryFoldersTest");
//                                        }
//                                    }
//                            );
//
//                        } catch (IOException e) {
//                            throw new RuntimeException("There is failed test: localDirectoryFoldersTest");
//                        }
//                    }
//                }
//        );
//    }
}
