package info.rmapproject.indexing.solr.repository;

import info.rmapproject.indexing.IndexUtils;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Decomposes a {@link IndexDTO} object to a stream of {@link EventDiscoTuple}s, in preparation for indexing.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public interface IndexDTOMapper extends Function<IndexDTO, Stream<EventDiscoTuple>> {

    /**
     * Decomposes the supplied {@code IndexDTO} into a stream of {@link EventDiscoTuple}s.  The {@code EventDiscoTuple}
     * is later mapped to Solr documents for indexing.
     *
     * @param indexDTO the index DTO object supplied by the caller
     * @return a stream of {@code EventDiscoTuple} objects derived from the {@code indexDTO}
     */
    @Override
    Stream<EventDiscoTuple> apply(IndexDTO indexDTO);

    default EventDiscoTuple getSourceIndexableThing(IndexDTO indexDTO) {
        return apply(indexDTO)
                .filter(it -> it.eventSource != null && IndexUtils.irisEqual(it.eventSource, it.disco.getId()))
                .findAny()
                .orElseThrow(IndexUtils.ise("Missing source of event."));
    }

    default EventDiscoTuple getTargetIndexableThing(IndexDTO indexDTO) {
        return apply(indexDTO)
                .filter(it -> it.eventTarget != null && IndexUtils.irisEqual(it.eventTarget, it.disco.getId()))
                .findAny()
                .orElseThrow(IndexUtils.ise("Missing target of event."));
    }


}
