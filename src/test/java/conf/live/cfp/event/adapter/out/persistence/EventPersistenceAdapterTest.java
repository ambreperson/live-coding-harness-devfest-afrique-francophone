package conf.live.cfp.event.adapter.out.persistence;

import conf.live.cfp.event.domain.model.Event;
import conf.live.cfp.event.domain.model.EventId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(EventPersistenceAdapter.class)
class EventPersistenceAdapterTest {

    @Autowired
    private EventPersistenceAdapter adapter;

    @Autowired
    private SpringDataEventRepository jpaRepository;

    @Test
    void should_save_an_event_and_make_it_retrievable() {
        Event event = new Event(EventId.newId(), "DevFest Afrique Francophone");

        adapter.save(event);

        Optional<EventJpaEntity> persisted = jpaRepository.findById(event.id().toString());
        assertThat(persisted).isPresent();
        assertThat(persisted.get().getName()).isEqualTo("DevFest Afrique Francophone");
    }

    @Test
    void should_find_all_saved_events() {
        Event first = new Event(EventId.newId(), "DevFest Afrique Francophone");
        Event second = new Event(EventId.newId(), "DevFest Nantes");

        adapter.save(first);
        adapter.save(second);

        List<Event> events = adapter.findAll();

        assertThat(events).extracting(Event::name)
                .containsExactlyInAnyOrder("DevFest Afrique Francophone", "DevFest Nantes");
    }

    @Test
    void should_find_an_event_by_id() {
        Event event = new Event(EventId.newId(), "DevFest Afrique Francophone");
        adapter.save(event);

        Optional<Event> found = adapter.findById(event.id());

        assertThat(found).isPresent();
        assertThat(found.get().id()).isEqualTo(event.id());
        assertThat(found.get().name()).isEqualTo("DevFest Afrique Francophone");
    }
}
