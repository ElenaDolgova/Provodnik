public interface Directory {
    /**
     * Обление оображения файлов на {@link FilesScrollPane}
     */
    void updateFilesScrollPane();

    /**
     * @return имя текущей директории
     */
    String getDirectoryName();
}
