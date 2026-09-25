package conf.live.cfp.proposal.adapter.out.event;

import conf.live.cfp.event.application.port.in.FindEventUseCase;
import conf.live.cfp.proposal.application.port.out.EventExistsPort;
import org.springframework.stereotype.Component;

/**
 * Secondary adapter implementing {@link EventExistsPort} on top of the {@code event} domain's
 * {@link FindEventUseCase}, so the {@code proposal} domain never depends on the event domain's
 * persistence details, only on its public in-port.
 */
@Component
public class EventExistsAdapter implements EventExistsPort {

    private final FindEventUseCase findEventUseCase;

    public EventExistsAdapter(FindEventUseCase findEventUseCase) {
        this.findEventUseCase = findEventUseCase;
    }

    @Override
    public boolean existsById(String eventId) {
        return findEventUseCase.findById(eventId).isPresent();
    }
}
