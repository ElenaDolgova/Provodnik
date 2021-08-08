import gui.Dimensions;
import gui.DirectoryView;
import gui.FilesView;
import gui.PreviewPanelView;
import gui.Renderer;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.Dimension;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

// todo
// + разобраться с рутовыми директориями в windows
// + разобраться с ftp подключением на windows
// разобраться со скачиванием файла с ftp на windows
// + перенести gif из ресурсов
// пароль и логин на ftp
// пароль и логин на zip
// расширить окошки внутренние
// + кодировка
// + добавить последнюю посещенную директорию на скролл с файлами
// подсвечивать рут, в котором находимся
// тесты
// превью на текстовые файлы чтобы можно было мотать
// отрисовка pdf и xml файлов
public class Main {
    public static void main(String[] args) {
        JFrame.setDefaultLookAndFeelDecorated(true);
        final Main main = new Main();
        javax.swing.SwingUtilities.invokeLater(main::init);
    }

    private void init() {
        final JFrame GLOBAL_FRAME = new JFrame("Vitamin Well");
        GLOBAL_FRAME.setLayout(new BorderLayout());
        GLOBAL_FRAME.setDefaultCloseOperation(EXIT_ON_CLOSE);
        GLOBAL_FRAME.setPreferredSize(new Dimension(Dimensions.MAIN_WIDTH, Dimensions.MAIN_HEIGHT));

        final DirectoryView DIRECTORY_SCROLL_PANE = new DirectoryView();
        final FilesView FILES_SCROLL_PANE = new FilesView();
        final PreviewPanelView PREVIEW_PANEL = new PreviewPanelView();

        gui.Renderer renderer = new Renderer(DIRECTORY_SCROLL_PANE, FILES_SCROLL_PANE, PREVIEW_PANEL);
        FILES_SCROLL_PANE.init(GLOBAL_FRAME, renderer);
        DIRECTORY_SCROLL_PANE.init(GLOBAL_FRAME, renderer);
        PREVIEW_PANEL.init(GLOBAL_FRAME);

        GLOBAL_FRAME.pack();
        GLOBAL_FRAME.setVisible(true);
    }
}
