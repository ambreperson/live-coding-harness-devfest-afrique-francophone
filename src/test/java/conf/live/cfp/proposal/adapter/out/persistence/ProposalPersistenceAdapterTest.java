package conf.live.cfp.proposal.adapter.out.persistence;

import conf.live.cfp.proposal.domain.model.Proposal;
import conf.live.cfp.proposal.domain.model.Speaker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(ProposalPersistenceAdapter.class)
class ProposalPersistenceAdapterTest {

    @Autowired
    private ProposalPersistenceAdapter adapter;

    @Autowired
    private SpringDataProposalRepository jpaRepository;

    @Test
    void should_save_a_proposal_and_make_it_retrievable() {
        Proposal proposal = Proposal.submit("Hexagonal architecture in practice",
                "A talk about ports and adapters",
                new Speaker("Ada Lovelace", "ada@example.com"),
                Instant.parse("2026-09-25T10:00:00Z"));

        Proposal saved = adapter.save(proposal);

        assertThat(saved).isEqualTo(proposal);

        Optional<ProposalJpaEntity> persisted = jpaRepository.findById(proposal.id().toString());
        assertThat(persisted).isPresent();
        assertThat(persisted.get().getTitle()).isEqualTo("Hexagonal architecture in practice");
        assertThat(persisted.get().getDescription()).isEqualTo("A talk about ports and adapters");
        assertThat(persisted.get().getSpeakerName()).isEqualTo("Ada Lovelace");
        assertThat(persisted.get().getSpeakerEmail()).isEqualTo("ada@example.com");
        assertThat(persisted.get().getStatus()).isEqualTo("SUBMITTED");
        assertThat(persisted.get().getSubmittedAt()).isEqualTo(Instant.parse("2026-09-25T10:00:00Z"));
    }
}
