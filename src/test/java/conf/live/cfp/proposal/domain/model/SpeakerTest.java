package conf.live.cfp.proposal.domain.model;

import conf.live.cfp.proposal.domain.exception.InvalidProposalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpeakerTest {

    @Test
    void should_create_a_speaker_with_name_and_email() {
        Speaker speaker = new Speaker("Ada Lovelace", "ada@example.com");

        assertThat(speaker.name()).isEqualTo("Ada Lovelace");
        assertThat(speaker.email()).isEqualTo("ada@example.com");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void should_reject_blank_name(String blankName) {
        assertThatThrownBy(() -> new Speaker(blankName, "ada@example.com"))
                .isInstanceOf(InvalidProposalException.class)
                .hasMessageContaining("name");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "not-an-email", "missing-at.com", "@missing-local.com"})
    void should_reject_invalid_email(String invalidEmail) {
        assertThatThrownBy(() -> new Speaker("Ada Lovelace", invalidEmail))
                .isInstanceOf(InvalidProposalException.class)
                .hasMessageContaining("email");
    }
}
