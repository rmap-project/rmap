package info.rmapproject.spring.triplestore.support;

/**
 * Initializes, clears, or destroys a triplestore.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public interface TriplestoreInitializer {

    void initializeTriplestore();

    void destroyTriplestore();

    void clearTriplestore();

}
