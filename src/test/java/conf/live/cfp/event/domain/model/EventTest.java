package conf.live.cfp.event.domain.model;

import conf.live.cfp.event.domain.exception.InvalidEventException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventTest {

    @Test
    void should_create_an_event_with_a_name() {
        Event event = Event.create("DevFest Afrique Francophone");

        assertThat(event.id()).isNotNull();
        assertThat(event.name()).isEqualTo("DevFest Afrique Francophone");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void should_reject_blank_name(String blankName) {
        assertThatThrownBy(() -> Event.create(blankName))
                .isInstanceOf(InvalidEventException.class)
                .hasMessageContaining("name");
    }
}
