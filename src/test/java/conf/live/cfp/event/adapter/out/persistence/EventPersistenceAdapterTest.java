package conf.live.cfp.event.adapter.out.persistence;

import conf.live.cfp.event.domain.model.Event;
import conf.live.cfp.event.domain.model.EventId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

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
}
