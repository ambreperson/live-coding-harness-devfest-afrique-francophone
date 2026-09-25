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

    @Bean
    public CreateProposalUseCase createProposalUseCase(SaveProposalPort saveProposalPort,
                                                        EventExistsPort eventExistsPort, Clock clock) {
        return new CreateProposalService(saveProposalPort, eventExistsPort, clock);
    }
}
