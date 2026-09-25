package conf.live.cfp.proposal.adapter.in.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Web request payload to submit a new proposal.
 */
public record CreateProposalRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotBlank String speakerName,
        @NotBlank @Email String speakerEmail) {
}
