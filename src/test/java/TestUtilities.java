import model.Directory;

import java.util.ArrayList;
import java.util.List;

public class TestUtilities {
    private TestUtilities() {
    }

    public static List<Directory> getResponseFiles(Directory directory, String ext) {
        List<Directory> response = new ArrayList<>();
        directory.getFiles(response::addAll, ext);
        return response;
    }
}
