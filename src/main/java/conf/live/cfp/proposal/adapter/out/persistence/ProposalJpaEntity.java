package conf.live.cfp.proposal.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "proposals")
public class ProposalJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 4000)
    private String description;

    @Column(nullable = false)
    private String speakerName;

    @Column(nullable = false)
    private String speakerEmail;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Instant submittedAt;

    @Column(name = "event_id", nullable = true)
    private String eventId;

    protected ProposalJpaEntity() {
        // required by JPA
    }

    public ProposalJpaEntity(String id, String title, String description, String speakerName,
                              String speakerEmail, String status, Instant submittedAt, String eventId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.speakerName = speakerName;
        this.speakerEmail = speakerEmail;
        this.status = status;
        this.submittedAt = submittedAt;
        this.eventId = eventId;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getSpeakerName() {
        return speakerName;
    }

    public String getSpeakerEmail() {
        return speakerEmail;
    }

    public String getStatus() {
        return status;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public String getEventId() {
        return eventId;
    }
}
