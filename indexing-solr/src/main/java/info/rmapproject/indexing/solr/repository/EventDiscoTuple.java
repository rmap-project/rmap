package info.rmapproject.indexing.solr.repository;

import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapStatus;
import info.rmapproject.core.model.agent.RMapAgent;
import info.rmapproject.core.model.disco.RMapDiSCO;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.indexing.solr.model.DiscoSolrDocument;

/**
 * Represents a unique pairing of an {@link RMapEvent} and {@link RMapDiSCO}.  An instance of {@code EventDiscoTuple}
 * {@link EventDiscoTupleMapper maps} one-to-one with a {@link DiscoSolrDocument Solr document}.  An {@code
 * EventDiscoTuple} carries additional information describing the event and DiSCO.  This includes information about the
 * {@link RMapAgent agent} that is associated with the event, the {@link RMapStatus status} of the DiSCO, and IRIs
 * referencing the source and target of the event.
 * <p>
 * A {@link IndexDTO} instance will decompose to at least one (at most two) {@code EventDiscoTuple} instance; one
 * tuple for the {@code (Event, Source DiSCO)} and one tuple for the {@code (Event, Target DiSCO)} (Event source
 * and targets are DiSCOs).  Practically speaking, each instance of a {@code EventDiscoTuple} is represented by a Solr
 * document in the index.
 * </p>
 */
public class EventDiscoTuple {

    /**
     * The event, which has {@link #disco the disco} as a source or target
     */
    RMapEvent event;

    /**
     * The disco, which is either the source or target of the {@link #event}
     */
    RMapDiSCO disco;

    /**
     * The agent, which is associated with {@link #event}
     */
    RMapAgent agent;

    /**
     * The status of the {@link #disco}
     */
    RMapStatus status;

    /**
     * The IRI to the object considered the source of the {@link #event}
     */
    RMapIri eventSource;

    /**
     * The IRI to the object considered the target of the {@link #event}
     */
    RMapIri eventTarget;
}
