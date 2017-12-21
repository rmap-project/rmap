package info.rmapproject.indexing;

/**
 * Thrown when a thread performing an indexing task is interrupted.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class IndexingInterruptedException extends Exception {

    private static final long serialVersionUID = 1L;

    public IndexingInterruptedException(String message) {
        super(message);
    }

    public IndexingInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }

    public IndexingInterruptedException(Throwable cause) {
        super(cause);
    }

    public IndexingInterruptedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
