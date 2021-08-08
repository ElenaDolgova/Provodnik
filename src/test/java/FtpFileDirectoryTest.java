import org.apache.commons.net.ftp.FTPClient;
import org.junit.jupiter.api.*;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class FtpFileDirectoryTest {

    public final static String USERNAME = "user";
    public final static String PASSWORD = "password";

    private final static String HOME_DIR = "/";
    private final static String HOST = "localhost";
    private final static int PORT = 0;
    private static int ftpPort;

    private final static String FILE = "/file1";

    private FakeFtpServer fakeFtpServer;
    private RemoteFile remoteFile;

    @BeforeEach
    public void before() {
        this.fakeFtpServer = new FakeFtpServer();
        fakeFtpServer.setServerControlPort(PORT);
        fakeFtpServer.addUserAccount(new UserAccount(USERNAME, PASSWORD, HOME_DIR));

        FileSystem fileSystem = new UnixFakeFileSystem();

        fileSystem.add(new FileEntry(FILE, "abcdef 1234567890"));
        fileSystem.add(new FileEntry("/file.zip"));
        fakeFtpServer.setFileSystem(fileSystem);

        fakeFtpServer.start();
        this.remoteFile = new RemoteFile();
        this.ftpPort = fakeFtpServer.getServerControlPort();
    }

    @AfterEach
    public void after() {
        this.fakeFtpServer.stop();
    }

    @Test
    public void testReadFile() throws Exception {
        String contents = remoteFile.readFile(FILE);
        Assertions.assertEquals("contents", contents);
    }

    private static class RemoteFile {

        public static final String USERNAME = "user";
        public static final String PASSWORD = "password";

        public String readFile(String filename) throws IOException {
            FTPClient ftpClient = new FTPClient();
            ftpClient.connect(HOST, ftpPort);
            ftpClient.login(USERNAME, PASSWORD);

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
