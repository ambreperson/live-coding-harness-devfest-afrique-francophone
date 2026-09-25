package conf.live.cfp.event.application.port.in;

import conf.live.cfp.event.domain.model.Event;

import java.util.Optional;

/**
 * Primary port: find an event by id.
 */
public interface FindEventUseCase {

    Optional<Event> findById(String eventId);
}
