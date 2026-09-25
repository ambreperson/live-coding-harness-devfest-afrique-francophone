package conf.live.cfp.proposal.application.service;

import conf.live.cfp.proposal.application.port.in.CreateProposalCommand;
import conf.live.cfp.proposal.application.port.in.CreateProposalUseCase;
import conf.live.cfp.proposal.application.port.out.EventExistsPort;
import conf.live.cfp.proposal.application.port.out.SaveProposalPort;
import conf.live.cfp.proposal.domain.exception.UnknownEventException;
import conf.live.cfp.proposal.domain.model.Proposal;
import conf.live.cfp.proposal.domain.model.Speaker;

import java.time.Clock;
import java.util.Objects;

/**
 * Application service implementing the {@link CreateProposalUseCase}.
 * Framework-agnostic: depends only on domain model and the out-ports.
 */
public class CreateProposalService implements CreateProposalUseCase {

    private final SaveProposalPort saveProposalPort;
    private final EventExistsPort eventExistsPort;
    private final Clock clock;

    public CreateProposalService(SaveProposalPort saveProposalPort, EventExistsPort eventExistsPort, Clock clock) {
        this.saveProposalPort = Objects.requireNonNull(saveProposalPort, "saveProposalPort must not be null");
        this.eventExistsPort = Objects.requireNonNull(eventExistsPort, "eventExistsPort must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public Proposal createProposal(CreateProposalCommand command) {
        if (command.eventId() != null && !eventExistsPort.existsById(command.eventId())) {
            throw new UnknownEventException("No event found with id " + command.eventId());
        }
        Speaker speaker = new Speaker(command.speakerName(), command.speakerEmail());
        Proposal proposal = Proposal.submit(command.title(), command.description(), speaker, clock.instant(),
                command.eventId());
        return saveProposalPort.save(proposal);
    }
}
