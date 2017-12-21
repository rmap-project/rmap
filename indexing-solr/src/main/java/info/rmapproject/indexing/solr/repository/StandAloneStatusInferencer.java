package info.rmapproject.indexing.solr.repository;

import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapStatus;
import info.rmapproject.core.model.agent.RMapAgent;
import info.rmapproject.core.model.disco.RMapDiSCO;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.indexing.IndexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static info.rmapproject.core.model.RMapStatus.ACTIVE;
import static info.rmapproject.core.model.RMapStatus.DELETED;
import static info.rmapproject.core.model.RMapStatus.INACTIVE;
import static info.rmapproject.core.model.RMapStatus.TOMBSTONED;
import static info.rmapproject.core.model.event.RMapEventType.CREATION;
import static info.rmapproject.core.model.event.RMapEventType.DELETION;
import static info.rmapproject.core.model.event.RMapEventType.DERIVATION;
import static info.rmapproject.core.model.event.RMapEventType.REPLACE;
import static info.rmapproject.core.model.event.RMapEventType.TOMBSTONE;
import static info.rmapproject.core.model.event.RMapEventType.UPDATE;
import static info.rmapproject.indexing.IndexUtils.irisEqual;

/**
 * Infers the status of a DiSCO using only the supplied Event, DiSCO, and Agent.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
class StandAloneStatusInferencer implements StatusInferencer {

    private static final Logger LOG = LoggerFactory.getLogger(StandAloneStatusInferencer.class);
    
    /**
     * Infer the {@link RMapStatus status} of the supplied DiSCO using the semantics of the supplied event.  The DiSCO
     * can be considered as input to, or output from, the supplied {@code event}, depending on whether the DiSCO is the
     * source or target referenced by the {@code event}.
     * <p>
     * For example, if the DiSCO is the <em>target</em> of a {@link RMapEventType#CREATION CREATION event}, then it is
     * inferred to have an {@link RMapStatus#ACTIVE ACTIVE status}.  If the DiSCO is the <em>source</em> of a {@link
     * RMapEventType#UPDATE UPDATE event}, then it is inferred to have an {@link RMapStatus#INACTIVE INACTIVE status}.
     * Likewise, if the DiSCO is the <em>target</em> of an {@code UPDATE} event, then it would be inferred to have an
     * {@code ACTIVE} status.
     * </p>
     * <p>
     * The {@code agent} is supplied for completeness, but it not considered in the implementation.
     * </p>
     *
     * @param disco the DiSCO referenced by {@code event}
     * @param event the event referencing the {@code disco}; the {@code disco} may be the source or target of the
     *              {@code event}
     * @param agent the agent that generated the event, supplied for completeness but not used
     * @return the inferred status
     * @throws IllegalStateException if the {@code event} target or source does not reference the supplied
     *                               {@code disco}, or if a reference is missing
     * @throws RuntimeException if the {@code event} source or target IRI is {@code null}
     */
    @Override
    public Optional<RMapStatus> inferDiscoStatus(RMapDiSCO disco, RMapEvent event, RMapAgent agent) {
        LOG.trace("Inferring DiSCO status for DiSCO {}, {} event {}, agent {}",
                (disco != null) ? disco.getId().getStringValue() : "null",
                (event != null) ? event.getEventType().toString() : "null",
                (event != null) ? event.getId().getStringValue() : "null",
                (agent != null) ? agent.getId().getStringValue() : "null");

        IndexUtils.assertNotNull(disco,"Supplied disco must not be null");
        IndexUtils.assertNotNull(event, "Supplied event must not be null");
        IndexUtils.assertNotNull(agent, "Supplied agent must not be null");

        RMapStatus status = null;
        Optional<RMapIri> sourceIri = null;
        Optional<RMapIri> targetIri = null;

        try {
            sourceIri = IndexUtils.findEventIri(event, IndexUtils.EventDirection.SOURCE);
            targetIri = IndexUtils.findEventIri(event, IndexUtils.EventDirection.TARGET);
        } catch (RuntimeException e) {
            throw new RuntimeException(String.format(
                    "Error resolving an event IRI for DiSCO %s, Event %s, Agent %s: %s",
                    disco.getId().getStringValue(), event.getId().getStringValue(), agent.getId().getStringValue(),
                    e.getMessage()), e);
        }

        LOG.trace("Found source event: '{}', target event: '{}'",
                sourceIri.orElse(null), targetIri.orElse(null));

        switch (event.getEventType()) {
            case CREATION:
                if (!targetIri.isPresent()) {
                    throw new IllegalStateException("No CREATION event target for event " +
                            event.getId().getStringValue());
                }

                if (irisEqual(targetIri, disco.getId())) {
                    logInference(CREATION, disco.getId(), IndexUtils.EventDirection.TARGET);
                    status = ACTIVE;
                } else {
                    throw new IllegalStateException(String.format(
                            "Missing DiSCO %s for CREATION event target %s%n",
                            targetIri.get().getStringValue(), event.getId().getStringValue()));
                }

                break;

            case DERIVATION:
                if (targetIri.isPresent() && irisEqual(targetIri, disco.getId())) {
                    logInference(DERIVATION, disco.getId(), IndexUtils.EventDirection.TARGET);
                    status = ACTIVE;
                } else {
                    throw new IllegalStateException("Missing DERIVATION event target IRI for event " +
                            event.getId().getStringValue());
                }
                break;

            case UPDATE:
                if (targetIri.isPresent() && irisEqual(targetIri, disco.getId())) {
                    logInference(UPDATE, disco.getId(), IndexUtils.EventDirection.TARGET);
                    status = ACTIVE;
                } else if (sourceIri.isPresent() && irisEqual(sourceIri, disco.getId())) {
                    logInference(UPDATE, disco.getId(), IndexUtils.EventDirection.SOURCE);
                    status = INACTIVE;
                } else {
                    throw new IllegalStateException("Missing UPDATE event source and target IRI for event " +
                            event.getId().getStringValue());
                }
                break;

            case DELETION:
                if (sourceIri.isPresent() && irisEqual(sourceIri, disco.getId())) {
                    logInference(DELETION, disco.getId(), IndexUtils.EventDirection.SOURCE);
                    status = DELETED;
                } else {
                    throw new IllegalStateException("Missing DELETION event source for event " +
                            event.getId().getStringValue());
                }
                break;

            case TOMBSTONE:
                if (sourceIri.isPresent() && irisEqual(sourceIri, disco.getId())) {
                    logInference(TOMBSTONE, disco.getId(), IndexUtils.EventDirection.SOURCE);
                    status = TOMBSTONED;
                } else {
                    throw new IllegalStateException("Missing TOMBSTONED event source for event " +
                            event.getId().getStringValue());
                }
                break;

            case INACTIVATION:
                if (sourceIri.isPresent() && irisEqual(sourceIri, disco.getId())) {
                    status = INACTIVE;
                } else {
                    throw new IllegalStateException("Missing INACTIVATION event source IRI for event " +
                            event.getId().getStringValue());
                }
                break;

            case REPLACE:
                if (targetIri.isPresent() && irisEqual(targetIri, disco.getId())) {
                    logInference(REPLACE, disco.getId(), IndexUtils.EventDirection.TARGET);
                    status = ACTIVE;
                } else {
                    throw new IllegalStateException("Missing REPLACE event target IRI for event " +
                            event.getId().getStringValue());
                }
                break;

            default:
                throw new RuntimeException("Unknown RMap event type: " + event.getEventType());
        }

        LOG.debug("Inferred DiSCO status for [DiSCO {}, {} event {}, agent {}] to be {}",
                disco.getId().getStringValue(), event.getEventType().toString(), event.getId().getStringValue(),
                agent.getId().getStringValue(), status);

        return Optional.of(status);
    }

    /**
     * Logs the reason behind the {@link #inferDiscoStatus(RMapDiSCO, RMapEvent, RMapAgent) inferencing} result at TRACE
     * level.
     *
     * @param type event type
     * @param iri the iri of the disco
     * @param direction whether the disco is the source of the event, the target of the event, or either (i.e. it
     *                  the direction doesn't matter)
     */
    private void logInference(RMapEventType type, RMapIri iri, IndexUtils.EventDirection direction) {
        if (!LOG.isTraceEnabled()) {
            return;
        }

        if (direction != IndexUtils.EventDirection.EITHER) {
            LOG.trace("{} event {} iri equals disco iri: {}",
                    type, (direction == IndexUtils.EventDirection.SOURCE ? "source" : "target"), iri.getStringValue());
        } else {
            LOG.trace("{} event source or target iri equals disco iri: {}", type, direction, iri.getStringValue());
        }
    }
}
