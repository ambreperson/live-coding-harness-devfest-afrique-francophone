package conf.live.cfp.proposal.config;

import conf.live.cfp.proposal.application.port.in.CreateProposalUseCase;
import conf.live.cfp.proposal.application.port.out.EventExistsPort;
import conf.live.cfp.proposal.application.port.out.SaveProposalPort;
import conf.live.cfp.proposal.application.service.CreateProposalService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Wires the framework-agnostic application service to its port, keeping the
 * hexagon (domain + application) free of Spring annotations.
 */
@Configuration
public class ProposalConfiguration {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    // TODO(Phase 2 - event persistence adapter): this is a temporary stand-in for
    // EventExistsPort, wired here only so the module compiles/starts after Phase 1d.
    // It must be replaced by a real adapter backed by the event domain's persistence
    // (e.g. delegating to FindEventPort) once that adapter exists; until then it
    // always reports events as unknown, so proposals can only be submitted without
    // an eventId in a running application.
    @Bean
    public EventExistsPort eventExistsPort() {
        return eventId -> false;
    }

    @Bean
    public CreateProposalUseCase createProposalUseCase(SaveProposalPort saveProposalPort,
                                                        EventExistsPort eventExistsPort, Clock clock) {
        return new CreateProposalService(saveProposalPort, eventExistsPort, clock);
    }
}
