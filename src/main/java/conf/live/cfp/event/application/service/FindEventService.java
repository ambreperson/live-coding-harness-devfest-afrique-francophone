package conf.live.cfp.event.application.service;

import conf.live.cfp.event.application.port.in.FindEventUseCase;
import conf.live.cfp.event.application.port.out.FindEventPort;
import conf.live.cfp.event.domain.model.Event;
import conf.live.cfp.event.domain.model.EventId;

import java.util.Optional;

/**
 * Use case implementation: find an event by id.
 */
public class FindEventService implements FindEventUseCase {

    private final FindEventPort findEventPort;

    public FindEventService(FindEventPort findEventPort) {
        this.findEventPort = findEventPort;
    }

    @Override
    public Optional<Event> findById(String eventId) {
        return findEventPort.findById(EventId.fromString(eventId));
    }
}
