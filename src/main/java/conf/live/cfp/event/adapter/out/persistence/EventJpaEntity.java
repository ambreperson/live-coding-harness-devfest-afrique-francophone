package conf.live.cfp.event.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "events")
public class EventJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    protected EventJpaEntity() {
        // required by JPA
    }

    public EventJpaEntity(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
