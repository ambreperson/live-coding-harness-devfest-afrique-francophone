package conf.live.cfp.event.domain.exception;

/**
 * Raised when an event invariant is violated.
 */
public class InvalidEventException extends RuntimeException {

    public InvalidEventException(String message) {
        super(message);
    }
}
