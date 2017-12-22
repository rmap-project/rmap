package info.rmapproject.indexing.solr.repository;

import info.rmapproject.core.model.RMapStatus;
import info.rmapproject.core.model.agent.RMapAgent;
import info.rmapproject.core.model.disco.RMapDiSCO;
import info.rmapproject.core.model.event.RMapEvent;

import java.util.Optional;

/**
 * Responsible for determining the {@link RMapStatus status} of a DiSCO.
 * <p>
 * Given an RMap Event and its referenced DiSCO and Agent, implementations must determine the appropriate status for the
 * DiSCO.  The status of a DiSCO will often depend on the semantics of the event.
 * </p>
 * <p>
 * Implementations may feel free to consult external services or data repositories to make an informed decision.  If no
 * informed decision can be made given the information, implementations may throw a {@link RuntimeException} or return
 * an empty {@link Optional}.
 * </p>
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
@FunctionalInterface
interface StatusInferencer {

    /**
     * Infer the {@link RMapStatus status} of the supplied DiSCO using the semantics of the supplied event.  The DiSCO
     * can be considered as input to, or output from, the supplied {@code event}, depending on whether the DiSCO is the
     * source or target referenced by the {@code event}.
     *
     * @param disco the DiSCO referenced by the event; may be the source or target
     * @param event the event
     * @param agent the agent referenced by the event
     * @return the status of the event, may be empty if no determination could be made
     */
    Optional<RMapStatus> inferDiscoStatus(RMapDiSCO disco, RMapEvent event, RMapAgent agent);

}
