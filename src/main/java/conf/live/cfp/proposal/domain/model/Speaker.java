package conf.live.cfp.proposal.domain.model;

import conf.live.cfp.proposal.domain.exception.InvalidProposalException;

import java.util.regex.Pattern;

/**
 * The person submitting a proposal.
 */
public record Speaker(String name, String email) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public Speaker {
        if (name == null || name.isBlank()) {
            throw new InvalidProposalException("Speaker name must not be blank");
        }
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidProposalException("Speaker email must be a valid email address");
        }
    }
}
