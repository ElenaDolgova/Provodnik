package exception;

/**
 * An exception that turns into any exception thrown by the program.
 * FileProcessingException is caught in the {@link gui.Renderer} or
 * in the {@link gui.View} and is displayed as an error to the user.
 * For example, it may appear when the viewer does not have enough rights to view the directory.
 */
public class FileProcessingException extends RuntimeException {
    public FileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
