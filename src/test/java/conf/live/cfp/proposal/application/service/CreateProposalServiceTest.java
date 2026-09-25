package conf.live.cfp.proposal.application.service;

import conf.live.cfp.proposal.application.port.in.CreateProposalCommand;
import conf.live.cfp.proposal.application.port.out.EventExistsPort;
import conf.live.cfp.proposal.application.port.out.SaveProposalPort;
import conf.live.cfp.proposal.domain.exception.InvalidProposalException;
import conf.live.cfp.proposal.domain.exception.UnknownEventException;
import conf.live.cfp.proposal.domain.model.Proposal;
import conf.live.cfp.proposal.domain.model.ProposalStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateProposalServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-25T10:00:00Z");

    @Mock
    private SaveProposalPort saveProposalPort;

    @Mock
    private EventExistsPort eventExistsPort;

    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    @Test
    void should_submit_and_persist_a_valid_proposal() {
        CreateProposalService service = new CreateProposalService(saveProposalPort, eventExistsPort, clock);
        CreateProposalCommand command = new CreateProposalCommand(
                "Hexagonal architecture in practice", "A talk about ports and adapters",
                "Ada Lovelace", "ada@example.com", null);
        when(saveProposalPort.save(any(Proposal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Proposal result = service.createProposal(command);

        assertThat(result.title()).isEqualTo("Hexagonal architecture in practice");
        assertThat(result.description()).isEqualTo("A talk about ports and adapters");
        assertThat(result.speaker().name()).isEqualTo("Ada Lovelace");
        assertThat(result.speaker().email()).isEqualTo("ada@example.com");
        assertThat(result.status()).isEqualTo(ProposalStatus.SUBMITTED);
        assertThat(result.submittedAt()).isEqualTo(NOW);

        ArgumentCaptor<Proposal> captor = ArgumentCaptor.forClass(Proposal.class);
        verify(saveProposalPort).save(captor.capture());
        assertThat(captor.getValue()).isEqualTo(result);
    }

    @Test
    void should_reject_an_invalid_command_without_calling_the_port() {
        CreateProposalService service = new CreateProposalService(saveProposalPort, eventExistsPort, clock);
        CreateProposalCommand command = new CreateProposalCommand("", "A talk about ports and adapters",
                "Ada Lovelace", "ada@example.com", null);

        assertThatThrownBy(() -> service.createProposal(command))
                .isInstanceOf(InvalidProposalException.class);

        verifyNoInteractions(saveProposalPort);
    }

    @Test
    void should_submit_and_persist_a_proposal_linked_to_an_existing_event() {
        CreateProposalService service = new CreateProposalService(saveProposalPort, eventExistsPort, clock);
        CreateProposalCommand command = new CreateProposalCommand(
                "Hexagonal architecture in practice", "A talk about ports and adapters",
                "Ada Lovelace", "ada@example.com", "11111111-1111-1111-1111-111111111111");
        when(eventExistsPort.existsById("11111111-1111-1111-1111-111111111111")).thenReturn(true);
        when(saveProposalPort.save(any(Proposal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Proposal result = service.createProposal(command);

        assertThat(result.eventId()).isEqualTo("11111111-1111-1111-1111-111111111111");
        verify(saveProposalPort).save(any(Proposal.class));
    }

    @Test
    void should_reject_a_proposal_referencing_an_unknown_event() {
        CreateProposalService service = new CreateProposalService(saveProposalPort, eventExistsPort, clock);
        CreateProposalCommand command = new CreateProposalCommand(
                "Hexagonal architecture in practice", "A talk about ports and adapters",
                "Ada Lovelace", "ada@example.com", "11111111-1111-1111-1111-111111111111");
        when(eventExistsPort.existsById("11111111-1111-1111-1111-111111111111")).thenReturn(false);

        assertThatThrownBy(() -> service.createProposal(command))
                .isInstanceOf(UnknownEventException.class);

        verifyNoInteractions(saveProposalPort);
    }

    @Test
    void should_submit_a_proposal_without_calling_event_exists_port_when_no_event_given() {
        CreateProposalService service = new CreateProposalService(saveProposalPort, eventExistsPort, clock);
        CreateProposalCommand command = new CreateProposalCommand(
                "Hexagonal architecture in practice", "A talk about ports and adapters",
                "Ada Lovelace", "ada@example.com", null);
        when(saveProposalPort.save(any(Proposal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Proposal result = service.createProposal(command);

        assertThat(result.eventId()).isNull();
        verifyNoInteractions(eventExistsPort);
    }
}
