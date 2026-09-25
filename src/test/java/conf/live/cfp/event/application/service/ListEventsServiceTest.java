package conf.live.cfp.event.application.service;

import conf.live.cfp.event.application.port.out.ListEventsPort;
import conf.live.cfp.event.domain.model.Event;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListEventsServiceTest {

    @Mock
    private ListEventsPort listEventsPort;

    @Test
    void should_return_all_events() {
        List<Event> events = List.of(Event.create("A"), Event.create("B"));
        when(listEventsPort.findAll()).thenReturn(events);
        ListEventsService service = new ListEventsService(listEventsPort);

        List<Event> result = service.listEvents();

        assertThat(result).isEqualTo(events);
    }
}
