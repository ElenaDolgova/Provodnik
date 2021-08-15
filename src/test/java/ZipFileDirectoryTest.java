import model.Directory;
import model.LocalFileDirectory;
import model.ZipFileDirectory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ZipFileDirectoryTest {
    private final static String directoryTestPath = "src/test/resources/ZipFileDirectoryTest";

    @Test
    @DisplayName("Открываем пустой zip")
    public void testEmptyZip() throws IOException {
        File file = new File(directoryTestPath + "/testEmptyZip/testEmptyZip.zip");
        FileSystem fs = FileSystems.newFileSystem(file.toPath(), null);
        Directory zipDirectory = new ZipFileDirectory(file.toPath(), fs, true);
        Assertions.assertTrue(zipDirectory.isDirectory());
        List<Directory> response = TestUtilities.getResponseFiles(zipDirectory, null);
        // на первом уровне папка со всеми файлами
        Assertions.assertEquals(1, response.size());
        Assertions.assertEquals("testEmptyZip", response.get(0).getName());

        List<Directory> supResponse = TestUtilities.getResponseFiles(response.get(0), null);
        Assertions.assertEquals(0, supResponse.size());
    }

    @Test
    @DisplayName("Что создается ZipFileDirectory при просмотре файлов в диреткории")
    public void testCreateZipFromLocalDirectory() {
        File file = new File(directoryTestPath + "/testCreateZipFromLocalDirectory/folder");
        FileSystem fs = file.toPath().getFileSystem();
        Directory localDirectory = new LocalFileDirectory(fs, file.toPath());
        List<Directory> response = TestUtilities.getResponseFiles(localDirectory, null);
        // на первом уровне папка со всеми файлами
        Assertions.assertEquals(1, response.size());
        Assertions.assertTrue(response.get(0) instanceof ZipFileDirectory);
    }

    @Test
    @DisplayName("Просмотр zip внутри zip")
    public void testZipInsideZip() throws IOException {
        File file = new File(directoryTestPath + "/testZipInsideZip/testZipInsideZip.zip");
        FileSystem fs = FileSystems.newFileSystem(file.toPath(), null);
        Directory zipDirectory = new ZipFileDirectory(file.toPath(), fs, true);
        Assertions.assertTrue(zipDirectory.isDirectory());
        List<Directory> response = TestUtilities.getResponseFiles(zipDirectory, null);
        // на первом уровне папка со всеми файлами
        Assertions.assertEquals(1, response.size());
        Assertions.assertEquals("testZipInsideZip", response.get(0).getName());

        List<Directory> subResponse = TestUtilities.getResponseFiles(response.get(0), null);
        Assertions.assertEquals(2, subResponse.size());
        subResponse.forEach(zipFile -> {
            if (".DS_Store".equals(zipFile.getName())) {
            } else if ("testEmptyZip.zip".equals(zipFile.getName())) {
                List<Directory> testEmptyZipResponse = TestUtilities.getResponseFiles(zipFile.createDirectory(), null);
                Assertions.assertEquals(1, testEmptyZipResponse.size());
                Assertions.assertEquals("testEmptyZip", testEmptyZipResponse.get(0).getName());

                List<Directory> subTestEmptyZipResponse = TestUtilities.getResponseFiles(testEmptyZipResponse.get(0), null);
                Assertions.assertEquals(0, subTestEmptyZipResponse.size());

            } else {
                throw new RuntimeException("Strange file");
            }
        });
    }

    @Test
    @DisplayName("Тройяная вложенность zip")
    public void testZipInsideZip3() throws IOException {
        File file = new File(directoryTestPath + "/testZipInsideZip3/testZip3Inside.zip");
        FileSystem fs = FileSystems.newFileSystem(file.toPath(), null);
        Directory zipDirectory = new ZipFileDirectory(file.toPath(), fs, true);
        Assertions.assertTrue(zipDirectory.isDirectory());
        List<Directory> response = TestUtilities.getResponseFiles(zipDirectory, null);
        // на первом уровне папка со всеми файлами
        Assertions.assertEquals(1, response.size());
        Assertions.assertEquals("testZip3Inside", response.get(0).getName());

        List<Directory> subResponse = TestUtilities.getResponseFiles(response.get(0), null);
        Assertions.assertEquals(1, subResponse.size());
        subResponse.forEach(zipFile -> {
            if ("testZipInsideZip.zip".equals(zipFile.getName())) {
                List<Directory> testZipInsideZip = TestUtilities.getResponseFiles(zipFile.createDirectory(), null);
                Assertions.assertEquals(1, testZipInsideZip.size());
                Assertions.assertEquals("testZipInsideZip", testZipInsideZip.get(0).getName());

                List<Directory> thirdZip = TestUtilities.getResponseFiles(testZipInsideZip.get(0), null);
                Assertions.assertEquals(2, thirdZip.size());
                thirdZip.forEach(it -> {
                    if (".DS_Store".equals(it.getName())) {
                    } else if ("testEmptyZip.zip".equals(it.getName())) {
                        List<Directory> testEmptyZipResponse = TestUtilities.getResponseFiles(it.createDirectory(), null);
                        Assertions.assertEquals(1, testEmptyZipResponse.size());
                        Assertions.assertEquals("testEmptyZip", testEmptyZipResponse.get(0).getName());

                        List<Directory> subTestEmptyZipResponse = TestUtilities.getResponseFiles(testEmptyZipResponse.get(0), null);
                        Assertions.assertEquals(0, subTestEmptyZipResponse.size());
                    } else {
                        throw new RuntimeException("Strange file");
                    }
                });
            } else {
                throw new RuntimeException("Strange file");
            }
        });
    }

    @Test
    @DisplayName("Фильтрация по расширению py")
    public void testFilterPy() throws IOException {
        File file = new File(directoryTestPath + "/testFilterPy/filterZip.zip");
        FileSystem fs = FileSystems.newFileSystem(file.toPath(), null);
        Directory zipDirectory = new ZipFileDirectory(file.toPath(), fs, true);
        Assertions.assertTrue(zipDirectory.isDirectory());
        List<Directory> response = TestUtilities.getResponseFiles(zipDirectory, null);
        // на первом уровне папка со всеми файлами
        Assertions.assertEquals(1, response.size());
        Assertions.assertEquals("filterZip", response.get(0).getName());

        List<Directory> responseFilterZip = TestUtilities.getResponseFiles(response.get(0), null);
        Assertions.assertEquals(3, responseFilterZip.size());

        List<Directory> responseFilterZipPy = TestUtilities.getResponseFiles(response.get(0), "py");
        Assertions.assertEquals(1, responseFilterZipPy.size());
        Assertions.assertEquals("get-pip.py", responseFilterZipPy.get(0).getName());

        AtomicReference<InputStream> txtStream = new AtomicReference<>();
        responseFilterZipPy.get(0).processFile(txtStream::set, "text");
        try {
            Assertions.assertTrue(txtStream.get().readAllBytes().length > 0);
        } catch (IOException e) {
            throw new RuntimeException("Cant read image");
        }
    }

    @Test
    @DisplayName("Получение файла")
    public void testGetFilePngAfterFilter() throws IOException {
        File file = new File(directoryTestPath + "/testFilterPy/filterZip.zip");
        FileSystem fs = FileSystems.newFileSystem(file.toPath(), null);
        Directory zipDirectory = new ZipFileDirectory(file.toPath(), fs, true);
        Assertions.assertTrue(zipDirectory.isDirectory());
        List<Directory> response = TestUtilities.getResponseFiles(zipDirectory, null);
        // на первом уровне папка со всеми файлами
        Assertions.assertEquals(1, response.size());
        Assertions.assertEquals("filterZip", response.get(0).getName());

        List<Directory> responseFilterZip = TestUtilities.getResponseFiles(response.get(0), null);
        Assertions.assertEquals(3, responseFilterZip.size());

        List<Directory> responseFilterZipPy = TestUtilities.getResponseFiles(response.get(0), "png");
        Assertions.assertEquals(1, responseFilterZipPy.size());
        Assertions.assertEquals("image2.png", responseFilterZipPy.get(0).getName());

        AtomicReference<InputStream> imageStream = new AtomicReference<>();
        responseFilterZipPy.get(0).processFile(imageStream::set, "image");
        try {
            Assertions.assertTrue(imageStream.get().readAllBytes().length > 0);
        } catch (IOException e) {
            throw new RuntimeException("Cant read image");
        }
    }
}
