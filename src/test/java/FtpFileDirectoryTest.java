import model.Directory;
import model.FtpFileDirectory;
import org.apache.commons.net.ftp.FTPClient;
import org.junit.jupiter.api.*;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class FtpFileDirectoryTest {
    public final static String USERNAME = "user";
    public final static String PASSWORD = "password";

    private final static String HOME_DIR = "/";
    private final static String HOST = "localhost";
    private final static int PORT = 0;
    private static int FTP_PORT;

    private FakeFtpServer fakeFtpServer;


    public void before(FileSystem fileSystem) {
        this.fakeFtpServer = new FakeFtpServer();
        fakeFtpServer.setServerControlPort(PORT);
        fakeFtpServer.addUserAccount(new UserAccount(USERNAME, PASSWORD, HOME_DIR));
        fakeFtpServer.setFileSystem(fileSystem);

        fakeFtpServer.start();
        FTP_PORT = fakeFtpServer.getServerControlPort();
    }

    @AfterEach
    public void after() {
        this.fakeFtpServer.stop();
    }

    @Test
    @DisplayName("Проверка пустоты ftp сервера")
    public void testEmptyFtpDirectory() throws Exception {

        FileSystem fileSystem = new UnixFakeFileSystem();

        before(fileSystem);
        RemoteFile remoteFile = new RemoteFile();
        remoteFile.createFtpClient();

        Directory directory = new FtpFileDirectory(remoteFile.ftpClient, HOME_DIR, null);
        Assertions.assertEquals("/", directory.getName());
        List<Directory> response = TestUtilities.getResponseFiles(directory, null);
        Assertions.assertEquals(0, response.size());
    }

    @Test
    @DisplayName("Проверка наличия файла на ftp сервере")
    public void testFtpDirectory() throws Exception {
        FileSystem fileSystem = new UnixFakeFileSystem();
        fileSystem.add(new FileEntry("/file.zip"));

        before(fileSystem);
        RemoteFile remoteFile = new RemoteFile();
        remoteFile.createFtpClient();

        Directory directory = new FtpFileDirectory(remoteFile.ftpClient, HOME_DIR, null);
        Assertions.assertEquals("/", directory.getName());
        List<Directory> response = TestUtilities.getResponseFiles(directory, null);
        Assertions.assertEquals(1, response.size());
    }

    @Test
    @DisplayName("Проверка фильтрации")
    public void testFilterFtpDirectory() throws Exception {
        FileSystem fileSystem = new UnixFakeFileSystem();
        fileSystem.add(new FileEntry("/file.zip"));
        fileSystem.add(new FileEntry("/file.png"));
        fileSystem.add(new FileEntry("/folder/file1.zip"));

        before(fileSystem);
        RemoteFile remoteFile = new RemoteFile();
        remoteFile.createFtpClient();

        Directory directory = new FtpFileDirectory(remoteFile.ftpClient, HOME_DIR, null);
        Assertions.assertEquals("/", directory.getName());
        List<Directory> response = TestUtilities.getResponseFiles(directory, "png");
        Assertions.assertEquals(1, response.size());
        Assertions.assertFalse(response.get(0).isDirectory());
    }

    @Test
    @DisplayName("Проверка Захождения и чтения папки на ftp сервере")
    public void testEnterInsideFolderFtpDirectory() throws Exception {
        FileSystem fileSystem = new UnixFakeFileSystem();
        fileSystem.add(new FileEntry("/file.zip"));
        fileSystem.add(new FileEntry("/file.png"));
        fileSystem.add(new FileEntry("/folder/file1.zip"));
        fileSystem.add(new DirectoryEntry("/folder/folder1"));
        fileSystem.add(new FileEntry("/folder/file.png"));
        fileSystem.add(new DirectoryEntry("/folder/folder2"));

        before(fileSystem);
        RemoteFile remoteFile = new RemoteFile();
        remoteFile.createFtpClient();

        Directory directory = new FtpFileDirectory(remoteFile.ftpClient, HOME_DIR, null);
        Assertions.assertEquals("/", directory.getName());
        List<Directory> response = TestUtilities.getResponseFiles(directory, null);
        Assertions.assertEquals(3, response.size());
        response.forEach(
                it -> {
                    switch (it.getName()) {
                        case "file.zip":
                        case "file.png":
                            break;
                        case "folder":
                            Assertions.assertTrue(it.isDirectory());
                            List<Directory> folderFiles = TestUtilities.getResponseFiles(it, null);
                            Assertions.assertEquals(4, folderFiles.size());

                            folderFiles.forEach(folderFile -> {
                                switch (folderFile.getName()) {
                                    case "file.png":
                                        Assertions.assertFalse(folderFile.isDirectory());
                                        break;
                                    case "file1.zip":
                                    case "folder2":
                                    case "folder1":
                                        Assertions.assertTrue(folderFile.isDirectory(), folderFile.getName());
                                        break;
                                    default:
                                        throw new RuntimeException("Strange file");
                                }
                            });

                            break;
                        default:
                            throw new RuntimeException("Strange file");
                    }
                }
        );
    }

    private static class RemoteFile {
        public static final String USERNAME = "user";
        public static final String PASSWORD = "password";
        public FTPClient ftpClient;

        public void createFtpClient() throws IOException {
            FTPClient ftpClient = new FTPClient();
            ftpClient.connect(HOST, FTP_PORT);
            ftpClient.login(USERNAME, PASSWORD);
            this.ftpClient = ftpClient;
        }

        public String readFile(String filename) throws IOException {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            boolean success = ftpClient.retrieveFile(filename, outputStream);
            ftpClient.disconnect();

            if (!success) {
                throw new IOException("Retrieve file failed: " + filename);
            }
            return outputStream.toString();
        }
    }
}
