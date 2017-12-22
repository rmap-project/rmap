package info.rmapproject.indexing.solr.repository;

import java.util.function.Function;

/**
 * Maps {@link EventDiscoTuple} instances to {@link info.rmapproject.indexing.solr.model.DiscoSolrDocument}s.  Unlike
 * the {@link IndexDTOMapper}, a single {@code EventDiscoTuple} will map to a single {@code DiscoSolrDocument}.
 *
 * @param <T> the Solr document type
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
interface EventDiscoTupleMapper<T> extends Function<EventDiscoTuple, T> {

    /**
     * Converts a {@code EventDiscoTuple} to a {@code DiscoSolrDocument} for indexing.
     *
     * @param eventDiscoTuple the event disco tuple
     * @return the solr document
     */
    @Override
    T apply(EventDiscoTuple eventDiscoTuple);

}
