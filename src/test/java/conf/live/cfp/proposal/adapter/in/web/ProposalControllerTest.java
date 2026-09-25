package conf.live.cfp.proposal.adapter.in.web;

import tools.jackson.databind.ObjectMapper;
import conf.live.cfp.proposal.application.port.in.CreateProposalCommand;
import conf.live.cfp.proposal.application.port.in.CreateProposalUseCase;
import conf.live.cfp.proposal.domain.exception.InvalidProposalException;
import conf.live.cfp.proposal.domain.model.Proposal;
import conf.live.cfp.proposal.domain.model.ProposalId;
import conf.live.cfp.proposal.domain.model.Speaker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProposalController.class)
class ProposalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateProposalUseCase createProposalUseCase;

    @Test
    void should_return_201_with_the_created_proposal() throws Exception {
        Proposal created = Proposal.reconstitute(
                ProposalId.fromString("11111111-1111-1111-1111-111111111111"),
                "Hexagonal architecture in practice",
                "A talk about ports and adapters",
                new Speaker("Ada Lovelace", "ada@example.com"),
                conf.live.cfp.proposal.domain.model.ProposalStatus.SUBMITTED,
                Instant.parse("2026-09-25T10:00:00Z"));
        when(createProposalUseCase.createProposal(any(CreateProposalCommand.class))).thenReturn(created);

        mockMvc.perform(post("/api/proposals")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new CreateProposalRequest(
                                "Hexagonal architecture in practice",
                                "A talk about ports and adapters",
                                "Ada Lovelace",
                                "ada@example.com",
                                null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("11111111-1111-1111-1111-111111111111"))
                .andExpect(jsonPath("$.title").value("Hexagonal architecture in practice"))
                .andExpect(jsonPath("$.description").value("A talk about ports and adapters"))
                .andExpect(jsonPath("$.speakerName").value("Ada Lovelace"))
                .andExpect(jsonPath("$.speakerEmail").value("ada@example.com"))
                .andExpect(jsonPath("$.status").value("SUBMITTED"));
    }

    @Test
    void should_return_201_with_the_event_id_when_proposal_is_linked_to_an_event() throws Exception {
        Proposal created = Proposal.reconstitute(
                ProposalId.fromString("11111111-1111-1111-1111-111111111111"),
                "Hexagonal architecture in practice",
                "A talk about ports and adapters",
                new Speaker("Ada Lovelace", "ada@example.com"),
                conf.live.cfp.proposal.domain.model.ProposalStatus.SUBMITTED,
                Instant.parse("2026-09-25T10:00:00Z"),
                "22222222-2222-2222-2222-222222222222");
        when(createProposalUseCase.createProposal(any(CreateProposalCommand.class))).thenReturn(created);

        mockMvc.perform(post("/api/proposals")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new CreateProposalRequest(
                                "Hexagonal architecture in practice",
                                "A talk about ports and adapters",
                                "Ada Lovelace",
                                "ada@example.com",
                                "22222222-2222-2222-2222-222222222222"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventId").value("22222222-2222-2222-2222-222222222222"));
    }

    @Test
    void should_return_400_when_title_is_blank() throws Exception {
        mockMvc.perform(post("/api/proposals")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new CreateProposalRequest(
                                "", "A talk about ports and adapters", "Ada Lovelace", "ada@example.com", null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return_400_when_speaker_email_is_invalid() throws Exception {
        mockMvc.perform(post("/api/proposals")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new CreateProposalRequest(
                                "Title", "Description", "Ada Lovelace", "not-an-email", null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return_400_when_use_case_rejects_the_proposal() throws Exception {
        when(createProposalUseCase.createProposal(any(CreateProposalCommand.class)))
                .thenThrow(new InvalidProposalException("Proposal title must not be blank"));

        mockMvc.perform(post("/api/proposals")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new CreateProposalRequest(
                                "Title", "Description", "Ada Lovelace", "ada@example.com", null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return_400_when_use_case_rejects_an_unknown_event() throws Exception {
        when(createProposalUseCase.createProposal(any(CreateProposalCommand.class)))
                .thenThrow(new conf.live.cfp.proposal.domain.exception.UnknownEventException(
                        "Unknown event: 33333333-3333-3333-3333-333333333333"));

        mockMvc.perform(post("/api/proposals")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new CreateProposalRequest(
                                "Title", "Description", "Ada Lovelace", "ada@example.com",
                                "33333333-3333-3333-3333-333333333333"))))
                .andExpect(status().isBadRequest());
    }
}
