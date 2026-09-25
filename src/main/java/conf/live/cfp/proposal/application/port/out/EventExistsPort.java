package conf.live.cfp.proposal.application.port.out;

/**
 * Secondary port: check whether an event exists.
 */
public interface EventExistsPort {

    boolean existsById(String eventId);
}
