package conf.live.cfp.proposal.domain.model;

import conf.live.cfp.proposal.domain.exception.InvalidProposalException;

import java.time.Instant;
import java.util.Objects;

/**
 * A talk proposal submitted by a speaker for a conference.
 */
public final class Proposal {

    private final ProposalId id;
    private final String title;
    private final String description;
    private final Speaker speaker;
    private final ProposalStatus status;
    private final Instant submittedAt;
    private final String eventId;

    private Proposal(ProposalId id, String title, String description, Speaker speaker,
                      ProposalStatus status, Instant submittedAt, String eventId) {
        if (title == null || title.isBlank()) {
            throw new InvalidProposalException("Proposal title must not be blank");
        }
        if (description == null || description.isBlank()) {
            throw new InvalidProposalException("Proposal description must not be blank");
        }
        if (speaker == null) {
            throw new InvalidProposalException("Proposal speaker must not be null");
        }
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.title = title;
        this.description = description;
        this.speaker = speaker;
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.submittedAt = Objects.requireNonNull(submittedAt, "submittedAt must not be null");
        this.eventId = eventId;
    }

    public static Proposal submit(String title, String description, Speaker speaker, Instant submittedAt) {
        return submit(title, description, speaker, submittedAt, null);
    }

    public static Proposal submit(String title, String description, Speaker speaker, Instant submittedAt,
                                   String eventId) {
        return new Proposal(ProposalId.newId(), title, description, speaker, ProposalStatus.SUBMITTED, submittedAt,
                eventId);
    }

    public static Proposal reconstitute(ProposalId id, String title, String description, Speaker speaker,
                                         ProposalStatus status, Instant submittedAt) {
        return reconstitute(id, title, description, speaker, status, submittedAt, null);
    }

    public static Proposal reconstitute(ProposalId id, String title, String description, Speaker speaker,
                                         ProposalStatus status, Instant submittedAt, String eventId) {
        return new Proposal(id, title, description, speaker, status, submittedAt, eventId);
    }

    public ProposalId id() {
        return id;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public Speaker speaker() {
        return speaker;
    }

    public ProposalStatus status() {
        return status;
    }

    public Instant submittedAt() {
        return submittedAt;
    }

    public String eventId() {
        return eventId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Proposal proposal)) return false;
        return id.equals(proposal.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
