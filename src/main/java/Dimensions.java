/**
 * Класс с основными размерами оконо
 */
public final class Dimensions {
    // todo работать на различных операционных системах
    // todo расширять окошки как внешние так и внутренние
    /**
     * Ширина основного окна
     */
    public static final int MAIN_WIDTH = 1000;
    /**
     * Высота основного окна
     */
    public static final int MAIN_HEIGHT = 600;

    /**
     * Ширина окна панели с директориями
     */
    public static final int DIRECTORY_SCROLL_PANE_WIDTH = MAIN_WIDTH / 6;

    /**
     * Ширина окна панели с директориями
     */
    public static final int FILE_SCROLL_PANE_WIDTH = MAIN_WIDTH / 6 * 2;
}
