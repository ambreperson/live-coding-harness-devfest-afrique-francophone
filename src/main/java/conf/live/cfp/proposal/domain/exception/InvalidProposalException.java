package conf.live.cfp.proposal.domain.exception;

/**
 * Raised when a proposal invariant is violated.
 */
public class InvalidProposalException extends RuntimeException {

    public InvalidProposalException(String message) {
        super(message);
    }
}
