package conf.live.cfp.event.application.port.out;

import conf.live.cfp.event.domain.model.Event;

/**
 * Secondary port: persist an event.
 */
public interface SaveEventPort {

    Event save(Event event);
}
