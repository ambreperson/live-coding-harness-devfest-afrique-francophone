package conf.live.cfp.proposal.adapter.out.persistence;

import conf.live.cfp.proposal.application.port.out.SaveProposalPort;
import conf.live.cfp.proposal.domain.model.Proposal;
import conf.live.cfp.proposal.domain.model.ProposalId;
import conf.live.cfp.proposal.domain.model.ProposalStatus;
import conf.live.cfp.proposal.domain.model.Speaker;
import org.springframework.stereotype.Component;

/**
 * Secondary adapter implementing {@link SaveProposalPort} on top of Spring Data JPA.
 */
@Component
public class ProposalPersistenceAdapter implements SaveProposalPort {

    private final SpringDataProposalRepository jpaRepository;

    public ProposalPersistenceAdapter(SpringDataProposalRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Proposal save(Proposal proposal) {
        ProposalJpaEntity entity = toEntity(proposal);
        ProposalJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    private ProposalJpaEntity toEntity(Proposal proposal) {
        return new ProposalJpaEntity(
                proposal.id().toString(),
                proposal.title(),
                proposal.description(),
                proposal.speaker().name(),
                proposal.speaker().email(),
                proposal.status().name(),
                proposal.submittedAt(),
                proposal.eventId());
    }

    private Proposal toDomain(ProposalJpaEntity entity) {
        return Proposal.reconstitute(
                ProposalId.fromString(entity.getId()),
                entity.getTitle(),
                entity.getDescription(),
                new Speaker(entity.getSpeakerName(), entity.getSpeakerEmail()),
                ProposalStatus.valueOf(entity.getStatus()),
                entity.getSubmittedAt(),
                entity.getEventId());
    }
}
