package conf.live.cfp.event.domain.model;

import java.util.UUID;

/**
 * Identity of an event.
 */
public record EventId(UUID value) {

    public static EventId newId() {
        return new EventId(UUID.randomUUID());
    }

    public static EventId fromString(String value) {
        return new EventId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
