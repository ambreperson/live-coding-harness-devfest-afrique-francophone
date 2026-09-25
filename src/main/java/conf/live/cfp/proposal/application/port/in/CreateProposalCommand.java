package conf.live.cfp.proposal.application.port.in;

/**
 * Input command to submit a new proposal.
 */
public record CreateProposalCommand(String title, String description, String speakerName, String speakerEmail) {
}
