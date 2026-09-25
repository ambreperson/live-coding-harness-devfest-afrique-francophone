package conf.live.cfp.proposal.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataProposalRepository extends JpaRepository<ProposalJpaEntity, String> {
}
