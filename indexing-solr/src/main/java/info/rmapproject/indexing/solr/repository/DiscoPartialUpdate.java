package info.rmapproject.indexing.solr.repository;

import org.springframework.data.solr.core.query.Field;
import org.springframework.data.solr.core.query.PartialUpdate;

import static info.rmapproject.indexing.IndexUtils.assertNotNullOrEmpty;

/**
 * Wraps a Solr {@link PartialUpdate} for the purpose of carrying additional metadata, such as the URI of the
 * DiSCO being updated.  Having this additional metadata carried along with the update allows the unit tests to
 * more easily identify candidate {@code PartialUpdate} instances that need to be verified.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class DiscoPartialUpdate extends PartialUpdate {

    private String discoIri;

    public DiscoPartialUpdate(String idFieldName, Object idFieldValue, String discoIri) {
        super(idFieldName, idFieldValue);
        assertNotNullOrEmpty(discoIri);
        this.discoIri = discoIri;
    }

    public DiscoPartialUpdate(Field idField, Object idFieldValue, String discoIri) {
        super(idField, idFieldValue);
        assertNotNullOrEmpty(discoIri);
        this.discoIri = discoIri;
    }

    /**
     * The IRI of the Disco that is affected by this update
     *
     * @return the iri
     */
    public String getDiscoIri() {
        return discoIri;
    }

    /**
     * The IRI of the Disco that is affected by this update
     *
     * @param discoIri the iri
     */
    public void setDiscoIri(String discoIri) {
        assertNotNullOrEmpty(discoIri);
        this.discoIri = discoIri;
    }
}
