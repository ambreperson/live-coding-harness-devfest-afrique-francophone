package conf.live.cfp.event.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EventTest {

    @Test
    void should_create_an_event_with_a_name() {
        Event event = Event.create("DevFest Afrique Francophone");

        assertThat(event.id()).isNotNull();
        assertThat(event.name()).isEqualTo("DevFest Afrique Francophone");
    }
}
