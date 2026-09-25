package conf.live.cfp.proposal.adapter.in.web;

import conf.live.cfp.proposal.domain.model.Proposal;

import java.time.Instant;

/**
 * Web representation of a proposal.
 */
public record ProposalResponse(
        String id,
        String title,
        String description,
        String speakerName,
        String speakerEmail,
        String status,
        Instant submittedAt,
        String eventId) {

    public static ProposalResponse from(Proposal proposal) {
        return new ProposalResponse(
                proposal.id().toString(),
                proposal.title(),
                proposal.description(),
                proposal.speaker().name(),
                proposal.speaker().email(),
                proposal.status().name(),
                proposal.submittedAt(),
                proposal.eventId());
    }
}
