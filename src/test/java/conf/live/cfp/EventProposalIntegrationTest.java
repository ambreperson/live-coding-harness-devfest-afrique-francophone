package conf.live.cfp;

import conf.live.cfp.event.adapter.in.web.CreateEventRequest;
import conf.live.cfp.proposal.adapter.in.web.CreateProposalRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EventProposalIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void should_create_and_list_events_through_the_http_api() throws Exception {
        mockMvc.perform(post("/api/events")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new CreateEventRequest("DevFest Afrique Francophone"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("DevFest Afrique Francophone"));

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == 'DevFest Afrique Francophone')]").exists());
    }

    @Test
    void should_submit_a_proposal_linked_to_an_existing_event() throws Exception {
        String eventResponseBody = mockMvc.perform(post("/api/events")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new CreateEventRequest("DevFest Nantes"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String eventId = objectMapper.readTree(eventResponseBody).get("id").asString();

        CreateProposalRequest request = new CreateProposalRequest(
                "Hexagonal architecture in practice",
                "A talk about ports and adapters",
                "Ada Lovelace",
                "ada@example.com",
                eventId);

        mockMvc.perform(post("/api/proposals")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventId").value(eventId));
    }

    @Test
    void should_reject_a_proposal_referencing_an_unknown_event() throws Exception {
        CreateProposalRequest request = new CreateProposalRequest(
                "Hexagonal architecture in practice",
                "A talk about ports and adapters",
                "Ada Lovelace",
                "ada@example.com",
                "11111111-1111-1111-1111-111111111111");

        mockMvc.perform(post("/api/proposals")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_submit_a_proposal_without_an_event_as_before() throws Exception {
        CreateProposalRequest request = new CreateProposalRequest(
                "Hexagonal architecture in practice",
                "A talk about ports and adapters",
                "Ada Lovelace",
                "ada@example.com",
                null);

        mockMvc.perform(post("/api/proposals")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventId").doesNotExist());
    }
}
