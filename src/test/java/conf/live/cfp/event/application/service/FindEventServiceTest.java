package conf.live.cfp.event.application.service;

import conf.live.cfp.event.application.port.out.FindEventPort;
import conf.live.cfp.event.domain.model.Event;
import conf.live.cfp.event.domain.model.EventId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindEventServiceTest {

    @Mock
    private FindEventPort findEventPort;

    @Test
    void should_return_the_event_when_id_exists() {
        EventId id = EventId.newId();
        Event event = Event.reconstitute(id, "Name");
        when(findEventPort.findById(id)).thenReturn(Optional.of(event));
        FindEventService service = new FindEventService(findEventPort);

        Optional<Event> result = service.findById(id.toString());

        assertThat(result).contains(event);
    }

    @Test
    void should_return_empty_when_id_is_malformed() {
        FindEventService service = new FindEventService(findEventPort);

        Optional<Event> result = service.findById("not-a-uuid");

        assertThat(result).isEmpty();
        verifyNoInteractions(findEventPort);
    }
}
