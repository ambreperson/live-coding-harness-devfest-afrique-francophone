package conf.live.cfp.event.domain.model;

import conf.live.cfp.event.domain.exception.InvalidEventException;

/**
 * A conference event that proposals can be submitted to.
 */
public final class Event {

    private final EventId id;
    private final String name;

    private Event(EventId id, String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidEventException("name must not be blank");
        }
        this.id = id;
        this.name = name;
    }

    public static Event create(String name) {
        return new Event(EventId.newId(), name);
    }

    public EventId id() {
        return id;
    }

    public String name() {
        return name;
    }
}
