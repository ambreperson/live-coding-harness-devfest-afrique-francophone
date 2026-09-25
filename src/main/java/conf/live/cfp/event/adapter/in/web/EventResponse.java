package conf.live.cfp.event.adapter.in.web;

import conf.live.cfp.event.domain.model.Event;

/**
 * Web representation of an event.
 */
public record EventResponse(String id, String name) {

    public static EventResponse from(Event event) {
        return new EventResponse(event.id().toString(), event.name());
    }
}
