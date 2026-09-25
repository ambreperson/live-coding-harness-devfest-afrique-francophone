package conf.live.cfp.event.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataEventRepository extends JpaRepository<EventJpaEntity, String> {
}
