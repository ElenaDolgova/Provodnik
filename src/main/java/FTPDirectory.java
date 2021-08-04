import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.FileSystem;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FTPDirectory implements Directory {

    private final FileSystemManager fsManager;
    private final FileSystemOptions opts;
    private final FileObject fileObject;

    public FTPDirectory(String name) throws FileSystemException {
        this.fsManager = VFS.getManager();
        this.opts = new FileSystemOptions();
//        String name = "ftp://anonymous@ftp.ubi.com";

        this.fileObject = fsManager.resolveFile(name, opts);
    }

    private static Stream<Path> streamAllFiles(FileSystem fs, int depth) {

        return StreamSupport.stream(fs.getRootDirectories().spliterator(), false)
                .flatMap((it) -> {
                    try {
                        return Files.walk(it, depth);
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                });
    }

    //https://www.mmnt.net/
    public static void main(String[] args) throws IOException {
//        FileSystem vfs = FileSystems.newFileSystem(
//                // URI.create("vfs:ftp://anonymous@ftp.kinook.com"), Collections.emptyMap()
//                URI.create("vfs:ftp://anonymous@ftp.ubi.com/corporate"), Collections.emptyMap()
//        );
////        FileSystem nestedFs = FileSystems.newFileSystem(vfs.getPath("/corporate"), Collections.emptyMap());
//        streamAllFiles(vfs, 2)
//                .forEach(System.out::println);

//        String vfsUri = "ftp://anonymous@ftp.ubi.com";


        FileSystemManager fsManager = VFS.getManager();
        FileSystemOptions opts = new FileSystemOptions();
//        FtpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, false);

        FileObject[] children = fsManager.resolveFile("ftp://anonymous@ftp.ubi.com", opts).getChildren();
        Arrays.stream(children).forEach(it -> {
            System.out.println("File: " + it);
            System.out.println(it.getName());
            System.out.println(it.getFileSystem());
//            System.out.println(it.getPath());
            System.out.println(it.getURI());
            try {
                FileContent fileContent = it.getContent();
                System.out.println(fileContent.getContentInfo());
                System.out.println("getContentEncoding " + fileContent.getContentInfo().getContentEncoding());
                System.out.println("getContentType " + fileContent.getContentInfo().getContentType());
                System.out.println("path " + fileContent.getFile().getName().getPath());
                System.out.println("baseName " + fileContent.getFile().getName().getBaseName());
                System.out.println("getExtension " + fileContent.getFile().getName().getExtension());

            } catch (org.apache.commons.vfs2.FileSystemException e) {
                e.printStackTrace();
            }
            try {
                System.out.println(it.getURL());
            } catch (org.apache.commons.vfs2.FileSystemException e) {
                e.printStackTrace();
            }
            try {
                System.out.println(it.getType().getName());
            } catch (org.apache.commons.vfs2.FileSystemException e) {
                e.printStackTrace();
            }
            try {
                System.out.println(it.getParent());
            } catch (org.apache.commons.vfs2.FileSystemException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public List<Link> getFiles() {
        return null;
    }

    @Override
    public String getDirectoryName() {
        return null;
    }
}
