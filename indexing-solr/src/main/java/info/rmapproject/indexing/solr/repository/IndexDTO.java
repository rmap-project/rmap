package info.rmapproject.indexing.solr.repository;

import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.agent.RMapAgent;
import info.rmapproject.core.model.disco.RMapDiSCO;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.indexing.IndexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import static info.rmapproject.indexing.IndexUtils.findEventIri;
import static info.rmapproject.indexing.IndexUtils.irisEqual;

/**
 * Encapsulates the unit of information sent to the indexer for indexing.  An {@code IndexDTO} forms a connected graph
 * between an event, the DiSCOs referenced by the event, and the agent responsible for the event. Depending on the
 * semantics of the event, the source or target DiSCO may be {@code null} (e.g. a {@link
 * info.rmapproject.core.model.event.RMapEventType#CREATION CREATION} event will not have a source DiSCO).
 * <p>
 * Indexing is event-driven.  Events are considered to have a source and target.  The objects referenced by the source
 * and target can be considered as input and output, respectively, to the event.  For example, the source of an
 * {@link info.rmapproject.core.model.event.RMapEventType#UPDATE UPDATE event} is the DiSCO that was operated on by
 * the event, and the target is the DiSCO that was produced by the event.  Agents are associated with the {@code event},
 * and are either directly or indirectly responsible for the event's occurrence.
 * </p>
 * <p>
 * Non-null IRIs present in the {@link #getEvent() RMapEvent} <em>must</em> reference DiSCOs in this object.  For
 * example, if the {@code event}'s source IRI is {@code <http://example.com/disco/1>}, then the {@link
 * #getSourceDisco() source DiSCO} must be present, and have the same IRI.
 * </p>
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class IndexDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(IndexDTO.class);

    // Fields are non-final to enable (de)serialization

    private RMapEvent event;
    private RMapAgent agent;
    private RMapDiSCO sourceDisco;
    private RMapDiSCO targetDisco;
    private RMapIri eventSourceIri;
    private RMapIri eventTargetIri;
    private Map<String, String> metadata;

    /**
     * Constructs a connected graph of objects to be indexed.
     * <p>
     * The {@code event} and {@code agent} must not be {@code null}.  If the {@code event} references a DiSCO, then the
     * supplied DiSCO must not be {@code null}, and the IRI of the reference must be equal to the IRI of the DiSCO.
     * </p>
     * <p>
     * For example, if the {@code event} had a target IRI of {@code <http://example.org/disco/1>}, then
     * {@code targetDisco} <em>must not</em> be {@code null} and <em>must</em> have an IRI equal to
     * {@code <http://example.org/disco/1>}.  If the {@code event} has <em>no</em> source IRI, then the {@code
     * sourceDisco} may be {@code null}.
     * </p>
     * @param event the RMapEvent being indexed
     * @param agent the RMapAgent responsible for the event
     * @param sourceDisco the DiSCO referenced by the event's source IRI, may be {@code null}
     * @param targetDisco the DiSCO referenced by the event's target IRI, may be {@code null}
     * @throws IllegalArgumentException if {@code event} or {@code agent} are {@code null}, or if a reference is missing
     */
    public IndexDTO(RMapEvent event, RMapAgent agent, RMapDiSCO sourceDisco, RMapDiSCO targetDisco) {
        this.event = IndexUtils.assertNotNull(event);
        this.agent = IndexUtils.assertNotNull(agent);

        if (!irisEqual(event.getAssociatedAgent(), agent.getId())) {
            throw new IllegalArgumentException("Incorrect agent IRI: expected " + event.getAssociatedAgent() + ", but" +
                    " found " + agent.getId());
        }

        Optional<RMapIri> iri;

        if ((iri = findEventIri(event, IndexUtils.EventDirection.SOURCE)).isPresent()) {
            if (sourceDisco == null) {
                throw new IllegalArgumentException("Expected to find a source DiSCO with iri " +
                        iri.get().getStringValue() + ", but the source DiSCO was null.");
            }

            this.sourceDisco = sourceDisco;
            this.eventSourceIri = iri.get();

            if (!irisEqual(eventSourceIri, sourceDisco.getId())) {
                throw new IllegalStateException("Expected DiSCO IRI " + sourceDisco.getId().getStringValue() + " to " +
                        "be equal to event source IRI " + eventSourceIri.getStringValue());
            }
        } else {
            this.sourceDisco = null;
            this.eventSourceIri = null;
        }

        if ((iri = findEventIri(event, IndexUtils.EventDirection.TARGET)).isPresent()) {
            if (targetDisco == null) {
                throw new IllegalArgumentException("Expected to find a target DiSCO with iri " +
                        iri.get().getStringValue() + ", but the target DiSCO was null.");
            }

            this.targetDisco = targetDisco;
            this.eventTargetIri = iri.get();

            if (!irisEqual(eventTargetIri, targetDisco.getId())) {
                throw new IllegalStateException("Expected DiSCO IRI " + targetDisco.getId().getStringValue() + " to " +
                        "be equal to event target IRI " + eventTargetIri.getStringValue());
            }
        } else {
            this.targetDisco = null;
            this.eventTargetIri = null;
        }
    }

    public RMapEvent getEvent() {
        return event;
    }

    public RMapAgent getAgent() {
        return agent;
    }

    public RMapDiSCO getSourceDisco() {
        return sourceDisco;
    }

    public RMapDiSCO getTargetDisco() {
        return targetDisco;
    }

    public RMapIri getEventSourceIri() {
        return eventSourceIri;
    }

    public RMapIri getEventTargetIri() {
        return eventTargetIri;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IndexDTO indexDTO = (IndexDTO) o;

        if (event != null ? !event.equals(indexDTO.event) : indexDTO.event != null) return false;
        if (agent != null ? !agent.equals(indexDTO.agent) : indexDTO.agent != null) return false;
        if (sourceDisco != null ? !sourceDisco.equals(indexDTO.sourceDisco) : indexDTO.sourceDisco != null)
            return false;
        if (targetDisco != null ? !targetDisco.equals(indexDTO.targetDisco) : indexDTO.targetDisco != null)
            return false;
        if (eventSourceIri != null ? !eventSourceIri.equals(indexDTO.eventSourceIri) : indexDTO.eventSourceIri != null)
            return false;
        if (eventTargetIri != null ? !eventTargetIri.equals(indexDTO.eventTargetIri) : indexDTO.eventTargetIri != null)
            return false;
        return metadata != null ? metadata.equals(indexDTO.metadata) : indexDTO.metadata == null;
    }

    @Override
    public int hashCode() {
        int result = event != null ? event.hashCode() : 0;
        result = 31 * result + (agent != null ? agent.hashCode() : 0);
        result = 31 * result + (sourceDisco != null ? sourceDisco.hashCode() : 0);
        result = 31 * result + (targetDisco != null ? targetDisco.hashCode() : 0);
        result = 31 * result + (eventSourceIri != null ? eventSourceIri.hashCode() : 0);
        result = 31 * result + (eventTargetIri != null ? eventTargetIri.hashCode() : 0);
        result = 31 * result + (metadata != null ? metadata.hashCode() : 0);
        return result;
    }
}
