package conf.live.cfp.proposal.application.port.in;

import conf.live.cfp.proposal.domain.model.Proposal;

/**
 * Primary port: submit a new proposal for a conference.
 */
public interface CreateProposalUseCase {

    Proposal createProposal(CreateProposalCommand command);
}
