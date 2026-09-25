package conf.live.cfp.event.application.port.in;

import conf.live.cfp.event.domain.model.Event;

/**
 * Primary port: create a new event.
 */
public interface CreateEventUseCase {

    Event createEvent(CreateEventCommand command);
}
