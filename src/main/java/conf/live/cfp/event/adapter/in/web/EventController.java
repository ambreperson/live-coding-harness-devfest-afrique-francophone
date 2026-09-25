package conf.live.cfp.event.adapter.in.web;

import conf.live.cfp.event.application.port.in.CreateEventCommand;
import conf.live.cfp.event.application.port.in.CreateEventUseCase;
import conf.live.cfp.event.domain.model.Event;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Primary adapter exposing the event use cases over HTTP.
 */
@RestController
@RequestMapping("/api/events")
public class EventController {

    private final CreateEventUseCase createEventUseCase;

    public EventController(CreateEventUseCase createEventUseCase) {
        this.createEventUseCase = createEventUseCase;
    }

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody CreateEventRequest request) {
        Event created = createEventUseCase.createEvent(new CreateEventCommand(request.name()));
        EventResponse response = EventResponse.from(created);
        return ResponseEntity.created(URI.create("/api/events/" + response.id())).body(response);
    }
}
