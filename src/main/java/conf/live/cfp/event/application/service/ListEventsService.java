package conf.live.cfp.event.application.service;

import conf.live.cfp.event.application.port.in.ListEventsUseCase;
import conf.live.cfp.event.application.port.out.ListEventsPort;
import conf.live.cfp.event.domain.model.Event;

import java.util.List;

/**
 * Use case implementation: list all events.
 */
public class ListEventsService implements ListEventsUseCase {

    private final ListEventsPort listEventsPort;

    public ListEventsService(ListEventsPort listEventsPort) {
        this.listEventsPort = listEventsPort;
    }

    @Override
    public List<Event> listEvents() {
        return listEventsPort.findAll();
    }
}
