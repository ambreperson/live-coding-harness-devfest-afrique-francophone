package conf.live.cfp.proposal.domain.model;

import java.util.UUID;

/**
 * Identity of a proposal.
 */
public record ProposalId(UUID value) {

    public static ProposalId newId() {
        return new ProposalId(UUID.randomUUID());
    }

    public static ProposalId fromString(String value) {
        return new ProposalId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
