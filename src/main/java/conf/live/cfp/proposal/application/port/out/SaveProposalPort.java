package conf.live.cfp.proposal.application.port.out;

import conf.live.cfp.proposal.domain.model.Proposal;

/**
 * Secondary port: persist a proposal.
 */
public interface SaveProposalPort {

    Proposal save(Proposal proposal);
}
