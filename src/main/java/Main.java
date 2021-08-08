import gui.Dimensions;
import gui.DirectoryView;
import gui.FilesView;
import gui.PreviewPanelView;
import gui.Renderer;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

// todo
// + разобраться с рутовыми директориями в windows
// + разобраться с ftp подключением на windows
// + разобраться со скачиванием файла с ftp на windows
// + перенести gif из ресурсов
// + очищать кеш после дисконнекта
// + пароль и логин на ftp
// + расширить окошки внутренние
// + зафиксировать размер внутренних окошек. Сделать так, чтобы не скакало
// + кодировка
// + добавить последнюю посещенную директорию на скролл с файлами
// + подсвечивать рут, в котором находимся
// тесты
// добавить назад на файловый скролл
// документация
// добавить иконки для файлов, которые можно сомтреть в превью
// превью на текстовые файлы чтобы можно было мотать
// отрисовка pdf и xml файлов
// пароль и логин на zip
public class Main {
    public static void main(String[] args) {
        JFrame.setDefaultLookAndFeelDecorated(true);
        final Main main = new Main();
        javax.swing.SwingUtilities.invokeLater(main::init);
    }

    private void init() {
        final JFrame globalFrame = new JFrame("Provodnik");
        globalFrame.setLayout(new BorderLayout());
        globalFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        globalFrame.setPreferredSize(new Dimension(Dimensions.MAIN_WIDTH, Dimensions.MAIN_HEIGHT));

        final DirectoryView directoryScrollPane = new DirectoryView();
        final PreviewPanelView previewPanel = new PreviewPanelView();
        final FilesView filesScrollPane = new FilesView(previewPanel);
        final JSplitPane rightSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                filesScrollPane.getMainFileScrollPane(),
                previewPanel.getPanel()
        );

        final JSplitPane mainSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                directoryScrollPane.getMainDirectoryPane(),
                rightSplit
        );

        gui.Renderer renderer = new Renderer(directoryScrollPane, filesScrollPane, previewPanel);
        filesScrollPane.init(renderer);
        directoryScrollPane.init(renderer);
        previewPanel.init();
        globalFrame.getContentPane().add(mainSplit);

        globalFrame.pack();
        globalFrame.setVisible(true);
    }
}
