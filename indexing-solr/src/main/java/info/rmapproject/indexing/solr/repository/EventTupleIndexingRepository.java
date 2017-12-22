package info.rmapproject.indexing.solr.repository;

import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Implementations are able to index {@link EventDiscoTuple} objects.
 *
 * @param <T> the index domain object type (i.e. the Solr document object type)
 */
public interface EventTupleIndexingRepository<T> {

    /**
     * Accept a stream of tuples, and index them accordingly.
     *
     * @param tupleStream the {@code EventDiscoTuple} stream to be indexed
     */
    void index(Stream<EventDiscoTuple> tupleStream);

    /**
     * Accept a stream of tuples, and index them accordingly.  Implementations are expected to apply the the
     * {@code decorator} to the indexable domain object.
     *
     * @param tupleStream the {@code EventDiscoTuple} stream to be indexed
     * @param decorator an consumer supplied by the caller which is able to decorate the indexable domain object
     */
    void index(Stream<EventDiscoTuple> tupleStream, Consumer<T> decorator);

}
