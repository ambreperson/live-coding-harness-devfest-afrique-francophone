package conf.live.cfp.event.application.port.in;

import conf.live.cfp.event.domain.model.Event;

import java.util.List;

/**
 * Primary port: list all events.
 */
public interface ListEventsUseCase {

    List<Event> listEvents();
}
