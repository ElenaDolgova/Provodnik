import model.Directory;
import model.LocalFileDirectory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

public class LocalFileDirectoryTest {
    private final static String directoryTestPath = "src/test/resources/LocalFileDirectoryTest";

    @Test
    @DisplayName("Проверяем директорию без фильтрации")
    public void localDirectoryEmptyTest() {
        File file = new File(directoryTestPath + "/localDirectoryTest/folder1");
        Path path = Paths.get(file.getAbsolutePath());
        FileSystem fs = path.getFileSystem();
        Directory localDirectory = new LocalFileDirectory(fs, path);

        Collection<Directory> response = TestUtilities.getResponseFiles(localDirectory, null);
        Assertions.assertEquals("folder1", localDirectory.getName());
        Assertions.assertEquals(1, response.size());
    }


    @Test
    @DisplayName("Проверяем директорию на фильтрацию по расширению zip")
    public void localDirectoryEmptyTestZip() {
        File file = new File(directoryTestPath + "/localDirectoryTest/folder1");
        Path path = Paths.get(file.getAbsolutePath());
        FileSystem fs = path.getFileSystem();
        Directory localDirectory = new LocalFileDirectory(fs, path);

        Collection<Directory> responseZip = TestUtilities.getResponseFiles(localDirectory, "zip");
        Assertions.assertTrue(responseZip.isEmpty());
    }

    @Test
    @DisplayName("Проверяем директорию на фильтрацию по расширению txt")
    public void localDirectoryEmptyTestTxt() {
        File file = new File(directoryTestPath + "/localDirectoryTest/folder1");
        Path path = Paths.get(file.getAbsolutePath());
        FileSystem fs = path.getFileSystem();
        Directory localDirectory = new LocalFileDirectory(fs, path);

        Collection<Directory> responseTxt = TestUtilities.getResponseFiles(localDirectory, "txt");
        Assertions.assertEquals(1, responseTxt.size());
    }

    @Test
    @DisplayName("Проверяем директорию, состоящую из папок")
    public void localDirectoryFolderTest() {
        File file = new File(directoryTestPath + "/localDirectoryTest");
        Path path = Paths.get(file.getAbsolutePath());
        FileSystem fs = path.getFileSystem();
        Directory localDirectory = new LocalFileDirectory(fs, path);

        // Проверили наличие двух папок в директории
        Assertions.assertEquals("localDirectoryTest", localDirectory.getName());

        Collection<Directory> response = TestUtilities.getResponseFiles(localDirectory, null);
        Assertions.assertEquals(2, response.size());

        response.forEach(
                folder -> {
                    Assertions.assertTrue(folder.isDirectory());
                    Assertions.assertNull(Directory.getProbeContentType(folder.getPath()));
                    Assertions.assertEquals(Paths.get(file.getAbsolutePath() + "/" + folder.getName()), folder.getPath());
                    Directory directory = folder.createDirectory();
                    Assertions.assertEquals(folder.getName(), directory.getName());

                    Collection<Directory> subDirectory = TestUtilities.getResponseFiles(directory, null);

                    Assertions.assertFalse(subDirectory.isEmpty());
                });
    }

    @Test
    @DisplayName("Проверяем директорию, состоящую из картинок и текста. Фильтрация по расширению png")
    public void localDirectoryImageAndTextTest() {
        String localDirectoryImageAndTextTest = "localDirectoryImageAndTextTest";
        File file = new File(directoryTestPath + "/" + localDirectoryImageAndTextTest);
        Path path = Paths.get(file.getAbsolutePath());
        FileSystem fs = path.getFileSystem();
        Directory localDirectory = new LocalFileDirectory(fs, path);

        // Проверили наличие двух папок в директории
        Assertions.assertEquals(localDirectoryImageAndTextTest, localDirectory.getName());

        Collection<Directory> response = TestUtilities.getResponseFiles(localDirectory, "png");
        Assertions.assertEquals(2, response.size());

        response.forEach(
                folder -> {
                    Assertions.assertFalse(folder.isDirectory());
                    Assertions.assertTrue(Directory.getProbeContentType(folder.getPath()).contains("image"));
                    Assertions.assertEquals(Paths.get(file.getAbsolutePath() + "/" + folder.getName()), folder.getPath());
                    AtomicReference<InputStream> imageStream = new AtomicReference<>();
                    folder.processFile(imageStream::set, "image");
                    try {
                        Assertions.assertTrue(imageStream.get().readAllBytes().length > 0);
                    } catch (IOException e) {
                        throw new RuntimeException("Cant read image");
                    }
                });
    }

    @Test
    @DisplayName("Проверяем директорию, состоящую из картинок и текста. Фильтрация по расширению txt")
    public void localDirectoryFilterTxtTest() {
        String localDirectoryImageAndTextTest = "localDirectoryImageAndTextTest";
        File file = new File(directoryTestPath + "/" + localDirectoryImageAndTextTest);
        Path path = Paths.get(file.getAbsolutePath());
        FileSystem fs = path.getFileSystem();
        Directory localDirectory = new LocalFileDirectory(fs, path);

        // Проверили наличие двух папок в директории
        Assertions.assertEquals(localDirectoryImageAndTextTest, localDirectory.getName());

        Collection<Directory> response = TestUtilities.getResponseFiles(localDirectory, "txt");
        Assertions.assertEquals(1, response.size());

        response.forEach(
                folder -> {
                    Assertions.assertFalse(folder.isDirectory());
                    Assertions.assertTrue(Directory.getProbeContentType(folder.getPath()).contains("text"));
                    Assertions.assertEquals(Paths.get(file.getAbsolutePath() + "/" + folder.getName()), folder.getPath());
                    AtomicReference<InputStream> imageStream = new AtomicReference<>();
                    folder.processFile(imageStream::set, "text");
                    try {
                        Assertions.assertTrue(imageStream.get().readAllBytes().length > 0);
                    } catch (IOException e) {
                        throw new RuntimeException("Cant read txt file");
                    }
                });
    }

    @Test
    @DisplayName("Ходим по папкам и подпапкам")
    public void localDirectoryFoldersTest() {
        String localDirectoryImageAndTextTest = "localDirectoryFoldersTest";
        File file = new File(directoryTestPath + "/" + localDirectoryImageAndTextTest);
        Path path = Paths.get(file.getAbsolutePath());
        FileSystem fs = path.getFileSystem();
        Directory localDirectory = new LocalFileDirectory(fs, path);

        // Проверили наличие двух папок в корневой директории: folder1 и folder2
        Assertions.assertEquals(localDirectoryImageAndTextTest, localDirectory.getName());
        Collection<Directory> files1 = TestUtilities.getResponseFiles(localDirectory, null);
        Assertions.assertEquals(2, files1.size());
        files1.forEach(folder -> Assertions.assertTrue(folder.isDirectory()));

        files1.forEach(folder -> {
                    if (folder.getName().equals("folder1")) {
                        model.Directory directory = folder.createDirectory();
                        Collection<Directory> files11 = TestUtilities.getResponseFiles(directory, null);
                        Assertions.assertEquals(3, files11.size());

                        files11.forEach(folder11 -> {
                                    switch (folder11.getName()) {
                                        case "folder11":
                                            Assertions.assertTrue(folder11.isDirectory());
                                            Assertions.assertFalse(
                                                    TestUtilities.getResponseFiles(
                                                            folder11.createDirectory(), null).isEmpty());
                                            break;
                                        case "folder12":
                                            Assertions.assertTrue(folder11.isDirectory());
                                            Assertions.assertEquals(2,
                                                    TestUtilities.getResponseFiles(folder11.createDirectory().createDirectory(), null).size());
                                            break;
                                        case "folder13":
                                            Assertions.assertTrue(folder11.isDirectory());
                                            Assertions.assertEquals(1, TestUtilities.getResponseFiles(folder11.createDirectory(), null).size());
                                            break;
                                        default:
                                            throw new RuntimeException("There is no another folders: localDirectoryFoldersTest");
                                    }
                                }
                        );
                    }
                }
        );
    }
}
