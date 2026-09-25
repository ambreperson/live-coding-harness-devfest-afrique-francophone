package conf.live.cfp.proposal.domain.model;

import conf.live.cfp.proposal.domain.exception.InvalidProposalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProposalTest {

    private static final Speaker A_SPEAKER = new Speaker("Ada Lovelace", "ada@example.com");
    private static final Instant NOW = Instant.parse("2026-09-25T10:00:00Z");

    @Test
    void should_submit_a_proposal_with_submitted_status() {
        Proposal proposal = Proposal.submit("Hexagonal architecture in practice",
                "A talk about ports and adapters", A_SPEAKER, NOW);

        assertThat(proposal.id()).isNotNull();
        assertThat(proposal.title()).isEqualTo("Hexagonal architecture in practice");
        assertThat(proposal.description()).isEqualTo("A talk about ports and adapters");
        assertThat(proposal.speaker()).isEqualTo(A_SPEAKER);
        assertThat(proposal.status()).isEqualTo(ProposalStatus.SUBMITTED);
        assertThat(proposal.submittedAt()).isEqualTo(NOW);
    }

    @Test
    void should_generate_a_different_id_for_each_submission() {
        Proposal first = Proposal.submit("Title", "Description", A_SPEAKER, NOW);
        Proposal second = Proposal.submit("Title", "Description", A_SPEAKER, NOW);

        assertThat(first.id()).isNotEqualTo(second.id());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void should_reject_blank_title(String blankTitle) {
        assertThatThrownBy(() -> Proposal.submit(blankTitle, "Description", A_SPEAKER, NOW))
                .isInstanceOf(InvalidProposalException.class)
                .hasMessageContaining("title");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void should_reject_blank_description(String blankDescription) {
        assertThatThrownBy(() -> Proposal.submit("Title", blankDescription, A_SPEAKER, NOW))
                .isInstanceOf(InvalidProposalException.class)
                .hasMessageContaining("description");
    }

    @Test
    void should_reject_missing_speaker() {
        assertThatThrownBy(() -> Proposal.submit("Title", "Description", null, NOW))
                .isInstanceOf(InvalidProposalException.class)
                .hasMessageContaining("speaker");
    }
}
