package conf.live.cfp.event.config;

import conf.live.cfp.event.application.port.in.CreateEventUseCase;
import conf.live.cfp.event.application.port.in.FindEventUseCase;
import conf.live.cfp.event.application.port.in.ListEventsUseCase;
import conf.live.cfp.event.application.port.out.FindEventPort;
import conf.live.cfp.event.application.port.out.ListEventsPort;
import conf.live.cfp.event.application.port.out.SaveEventPort;
import conf.live.cfp.event.application.service.CreateEventService;
import conf.live.cfp.event.application.service.FindEventService;
import conf.live.cfp.event.application.service.ListEventsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the framework-agnostic application services to their ports, keeping the
 * hexagon (domain + application) free of Spring annotations.
 */
@Configuration
public class EventConfiguration {

    @Bean
    public CreateEventUseCase createEventUseCase(SaveEventPort saveEventPort) {
        return new CreateEventService(saveEventPort);
    }

    @Bean
    public ListEventsUseCase listEventsUseCase(ListEventsPort listEventsPort) {
        return new ListEventsService(listEventsPort);
    }

    @Bean
    public FindEventUseCase findEventUseCase(FindEventPort findEventPort) {
        return new FindEventService(findEventPort);
    }
}
