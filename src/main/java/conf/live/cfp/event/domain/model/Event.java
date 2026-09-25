package conf.live.cfp.event.domain.model;

/**
 * A conference event that proposals can be submitted to.
 */
public final class Event {

    private final EventId id;
    private final String name;

    public Event(EventId id, String name) {
        this.id = id;
        this.name = name;
    }

    public EventId id() {
        return id;
    }

    public String name() {
        return name;
    }
}
