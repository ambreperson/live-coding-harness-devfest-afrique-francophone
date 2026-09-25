package conf.live.cfp.event.application.port.out;

import conf.live.cfp.event.domain.model.Event;

import java.util.List;

/**
 * Secondary port: retrieve all events.
 */
public interface ListEventsPort {

    List<Event> findAll();
}
