package gui;

/**
 * Класс с основными размерами оконо
 */
public final class Dimensions {
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
     * Высота окна панели с директориями
     */
    public static final int DIRECTORY_SCROLL_PANE_HEIGHT = MAIN_HEIGHT - 10;
    /**
     * Ширина окна панели с файлами
     */
    public static final int FILE_SCROLL_PANE_WIDTH = MAIN_WIDTH / 6 * 2;
    /**
     * Высота окна панели с файлами
     */
    public static final int FILE_SCROLL_PANE_HEIGHT = MAIN_HEIGHT - 10;
    /**
     * Ширина окна панели с превью
     */
    public static final int PREVIEW_PANEL_WIDTH = MAIN_WIDTH / 6 * 3;
    /**
     * Высота окна панели с превью
     */
    public static final int PREVIEW_PANEL_HEIGHT = MAIN_HEIGHT;
}
