package info.rmapproject.spring.triplestore.support;

import java.net.URL;

/**
 * Integration test fixture used to create, remove, or clear the contents of the triplestore used by the RMap API
 * webapp and the RMap HTML UI.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public interface TriplestoreManager {

    /**
     * Creates a new, empty, triplestore.
     *
     * @return the URL to the newly-created triplestore's API
     */
    URL createTriplestore();

    /**
     * Deletes all triples from the triplestore, and removes the triplestore itself.
     *
     * @return the URL to the removed triplestore's API
     */
    URL removeTriplestore();

    /**
     * Deletes all triples from the triplestore, but does <em>not</em> remove the triplestore itself.
     *
     * @return the URL to the cleared triplestore's API
     */
    URL clearTriplestore();

}
