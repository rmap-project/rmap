package info.rmapproject.indexing.solr.repository;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 * @param <F> type being mapped from
 * @param <T> type being mapped to
 */
@FunctionalInterface
interface IndexMapper<F, T> {

    T map(F from);

}
