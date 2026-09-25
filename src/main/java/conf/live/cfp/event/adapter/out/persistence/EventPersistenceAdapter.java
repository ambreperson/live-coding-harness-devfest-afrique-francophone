package conf.live.cfp.event.adapter.out.persistence;

import conf.live.cfp.event.application.port.out.ListEventsPort;
import conf.live.cfp.event.application.port.out.SaveEventPort;
import conf.live.cfp.event.domain.model.Event;
import conf.live.cfp.event.domain.model.EventId;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Secondary adapter implementing {@link SaveEventPort} and {@link ListEventsPort} on top of Spring Data JPA.
 */
@Component
public class EventPersistenceAdapter implements SaveEventPort, ListEventsPort {

    private final SpringDataEventRepository jpaRepository;

    public EventPersistenceAdapter(SpringDataEventRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Event save(Event event) {
        EventJpaEntity entity = toEntity(event);
        EventJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<Event> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    private EventJpaEntity toEntity(Event event) {
        return new EventJpaEntity(event.id().toString(), event.name());
    }

    private Event toDomain(EventJpaEntity entity) {
        return new Event(EventId.fromString(entity.getId()), entity.getName());
    }
}
