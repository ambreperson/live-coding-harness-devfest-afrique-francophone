package conf.live.cfp.proposal.application.service;

import conf.live.cfp.proposal.application.port.in.CreateProposalCommand;
import conf.live.cfp.proposal.application.port.in.CreateProposalUseCase;
import conf.live.cfp.proposal.application.port.out.SaveProposalPort;
import conf.live.cfp.proposal.domain.model.Proposal;
import conf.live.cfp.proposal.domain.model.Speaker;

import java.time.Clock;
import java.util.Objects;

/**
 * Application service implementing the {@link CreateProposalUseCase}.
 * Framework-agnostic: depends only on domain model and the {@link SaveProposalPort}.
 */
public class CreateProposalService implements CreateProposalUseCase {

    private final SaveProposalPort saveProposalPort;
    private final Clock clock;

    public CreateProposalService(SaveProposalPort saveProposalPort, Clock clock) {
        this.saveProposalPort = Objects.requireNonNull(saveProposalPort, "saveProposalPort must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public Proposal createProposal(CreateProposalCommand command) {
        Speaker speaker = new Speaker(command.speakerName(), command.speakerEmail());
        Proposal proposal = Proposal.submit(command.title(), command.description(), speaker, clock.instant());
        return saveProposalPort.save(proposal);
    }
}
