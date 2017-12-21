package info.rmapproject.indexing.solr.repository;

import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.agent.RMapAgent;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.model.event.RMapEventType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Stream;

import static info.rmapproject.indexing.IndexUtils.assertNotNull;
import static info.rmapproject.indexing.IndexUtils.irisEqual;
import static info.rmapproject.indexing.IndexUtils.ise;

/**
 * Decomposes a {@link IndexDTO} object to a stream of {@link EventDiscoTuple}s, in preparation for indexing.
 * Determines the {@link info.rmapproject.indexing.solr.model.DiscoSolrDocument#DISCO_STATUS status} of each DiSCO as
 * the {@code IndexDTO} is decomposed.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class SimpleIndexDTOMapper implements IndexDTOMapper {

    private static final String ERR_INFER_STATUS = "Unable to infer the status for RMap DiSCO: %s Event: %s: Agent: %s";

    @Autowired
    private StatusInferencer inferencer;

    /**
     * Constructs a simple IndexDTO mapper.  A {@link #setInferencer(StatusInferencer) StatusInferencer} must be set
     * prior to invoking {@code apply(IndexDTO)}.
     */
    public SimpleIndexDTOMapper() {

    }

    /**
     * Constructs a simple IndexDTO mapper.  The supplied {@link StatusInferencer} is used to determine the status of
     * DiSCOs present in the {@code IndexDTO}.
     *
     * @param inferencer the status inferencer, must not be {@code null}
     * @throws IllegalArgumentException if {@code inferencer} is {@code null}
     */
    public SimpleIndexDTOMapper(StatusInferencer inferencer) {
        assertNotNull(inferencer, "StatusInferencer must not be null.");
        this.inferencer = inferencer;
    }

    /**
     * {@inheritDoc}
     * <h3>Implementation notes</h3>
     * <ul>
     *     <li>Insures an agent and event are present in the DTO</li>
     *     <li>Insures the agent referenced by the event is same agent in the DTO</li>
     *     <li>Builds an {@code EventDiscoTuple} for each non-null event source and target present in the DTO
     *         (recall that the source and/or targets of events are typically DiSCOs).</li>
     *     <li>Determines the {@link info.rmapproject.indexing.solr.model.DiscoSolrDocument#DISCO_STATUS status} of
     *         each source and target DiSCO, using the {@link SimpleIndexDTOMapper#SimpleIndexDTOMapper(StatusInferencer)
     *         supplied inferencer}</li>
     * </ul>
     * @param indexDTO the index DTO object supplied by the caller
     * @return a stream of {@code EventDiscoTuple}s decomposed from the {@code indexDTO}.  Each DiSCO present in the
     *         tuple stream stream will have a {@code DiscoSolrDocument#DISCO_STATUS}.
     * @throws IllegalArgumentException if the supplied {@code indexDTO} is {@code null}
     * @throws IllegalStateException if a {@code StatusInferencer} is not present, or if the {@code indexDTO} is missing
     *                               an agent or event, or the supplied agent isn't the same agent referenced by the
     *                               event
     */
    @Override
    public Stream<EventDiscoTuple> apply(IndexDTO indexDTO) {
        assertNotNull(this.inferencer, ise("A StatusInferencer must be supplied on construction, or set on this " +
                "object prior to invoking apply(IndexDTO)"));
        assertNotNull(indexDTO, "The supplied IndexDTO must not be null.");

        RMapEvent event = assertNotNull(indexDTO.getEvent(), ise("The IndexDTO must not have a null event."));
        RMapAgent agent = assertNotNull(indexDTO.getAgent(), ise("The IndexDTO must not have a null agent."));

        if (!(irisEqual(event.getAssociatedAgent(), agent.getId()))) {
            throw new RuntimeException(String.format(
                    "Missing agent '%s' of event %s", event.getAssociatedAgent().getStringValue(), event));
        }

        // The source IRI will be null in the case of a creation event
        RMapIri source = indexDTO.getEventSourceIri();
        // The target IRI will be null in the case of a delete, tombstone, or inactivation event
        RMapIri target = indexDTO.getEventTargetIri();

        // We do not index the source DiSCO for DERIVATION events; the source of a DERIVATION event does not need to
        // be updated at all in the index. (TODO: version repository implications)
        EventDiscoTuple forSource = null;
        if (source != null && event.getEventType() != RMapEventType.DERIVATION) {
            forSource = new EventDiscoTuple();

            forSource.eventSource = source;
            forSource.eventTarget = target;
            forSource.event = event;
            forSource.agent = agent;
            forSource.disco = indexDTO.getSourceDisco();
            forSource.status = inferencer.inferDiscoStatus(forSource.disco, forSource.event, forSource.agent)
                    .orElseThrow(() -> new RuntimeException(
                            String.format(ERR_INFER_STATUS, indexDTO.getSourceDisco().getId(),
                                    indexDTO.getEvent().getId(), indexDTO.getAgent().getId())));
        }

        EventDiscoTuple forTarget = null;
        if (target != null) {
            forTarget = new EventDiscoTuple();
            forTarget.eventSource = source;
            forTarget.eventTarget = target;
            forTarget.event = event;
            forTarget.agent = agent;
            forTarget.disco = indexDTO.getTargetDisco();
            forTarget.status = inferencer.inferDiscoStatus(forTarget.disco, forTarget.event, forTarget.agent)
                    .orElseThrow(() -> new RuntimeException(
                            String.format(ERR_INFER_STATUS, indexDTO.getTargetDisco().getId(),
                                    indexDTO.getEvent().getId(), indexDTO.getAgent().getId())));
        }

        Stream.Builder<EventDiscoTuple> builder = Stream.builder();

        if (forSource != null) {
            builder.accept(forSource);
        }

        if (forTarget != null) {
            builder.accept(forTarget);
        }

        return builder.build();

    }

    /**
     * The {@link StatusInferencer} used to determine the {@link info.rmapproject.indexing.solr.model.DiscoSolrDocument#DISCO_STATUS status}
     * of DiSCOs present in {@code IndexDTO} objects.
     *
     * @return the inferencer, may be {@code null}
     */
    StatusInferencer getInferencer() {
        return inferencer;
    }

    /**
     * The {@link StatusInferencer} used to determine the {@link info.rmapproject.indexing.solr.model.DiscoSolrDocument#DISCO_STATUS status}
     * of DiSCOs present in {@code IndexDTO} objects.
     *
     * @param inferencer the StatusInferencer, must not be {@code null}
     * @throws IllegalArgumentException if the inferencer is {@code null}
     */
    void setInferencer(StatusInferencer inferencer) {
        assertNotNull(inferencer, "Supplied StatusInferencer must not be null.");
        this.inferencer = inferencer;
    }
}
