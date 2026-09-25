package conf.live.cfp.event.application.port.out;

import conf.live.cfp.event.domain.model.Event;
import conf.live.cfp.event.domain.model.EventId;

import java.util.Optional;

/**
 * Secondary port: retrieve an event by id.
 */
public interface FindEventPort {

    Optional<Event> findById(EventId id);
}
