package exception;

/**
 * Исключение, в которое обрачивается любое исключение выброшенное программой.
 * Ловится в в рендере или во View и отображается ошибкой пользователю.
 * Например, может появляться, когда просмоторщику не будет хватать прав на просмотр директории
 */
public class FileProcessingException extends RuntimeException {
    public FileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
