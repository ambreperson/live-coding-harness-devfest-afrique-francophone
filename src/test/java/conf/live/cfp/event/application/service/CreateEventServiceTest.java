package conf.live.cfp.event.application.service;

import conf.live.cfp.event.application.port.in.CreateEventCommand;
import conf.live.cfp.event.application.port.out.SaveEventPort;
import conf.live.cfp.event.domain.model.Event;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateEventServiceTest {

    @Mock
    private SaveEventPort saveEventPort;

    @Test
    void should_create_and_persist_a_valid_event() {
        CreateEventService service = new CreateEventService(saveEventPort);
        CreateEventCommand command = new CreateEventCommand("Name");
        when(saveEventPort.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event result = service.createEvent(command);

        assertThat(result.name()).isEqualTo("Name");

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(saveEventPort).save(captor.capture());
        assertThat(captor.getValue().name()).isEqualTo("Name");
    }
}
