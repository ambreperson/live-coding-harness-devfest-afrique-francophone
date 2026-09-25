package conf.live.cfp.proposal;

import conf.live.cfp.proposal.adapter.in.web.CreateProposalRequest;
import conf.live.cfp.proposal.adapter.out.persistence.SpringDataProposalRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProposalCreationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SpringDataProposalRepository proposalRepository;

    @Test
    void should_submit_a_proposal_through_the_http_api_and_persist_it() throws Exception {
        CreateProposalRequest request = new CreateProposalRequest(
                "Hexagonal architecture in practice",
                "A talk about ports and adapters",
                "Ada Lovelace",
                "ada@example.com");

        String responseBody = mockMvc.perform(post("/api/proposals")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andReturn().getResponse().getContentAsString();

        String proposalId = objectMapper.readTree(responseBody).get("id").asString();

        assertThat(proposalRepository.findById(proposalId)).isPresent();
    }
}
