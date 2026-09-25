package conf.live.cfp.proposal.domain.exception;

/**
 * Raised when a proposal references an event that does not exist.
 */
public class UnknownEventException extends RuntimeException {

    public UnknownEventException(String message) {
        super(message);
    }
}
