package conf.live.cfp.event.application.service;

import conf.live.cfp.event.application.port.in.CreateEventCommand;
import conf.live.cfp.event.application.port.in.CreateEventUseCase;
import conf.live.cfp.event.application.port.out.SaveEventPort;
import conf.live.cfp.event.domain.model.Event;

/**
 * Use case implementation: create a new event.
 */
public class CreateEventService implements CreateEventUseCase {

    private final SaveEventPort saveEventPort;

    public CreateEventService(SaveEventPort saveEventPort) {
        this.saveEventPort = saveEventPort;
    }

    @Override
    public Event createEvent(CreateEventCommand command) {
        Event event = Event.create(command.name());
        return saveEventPort.save(event);
    }
}
