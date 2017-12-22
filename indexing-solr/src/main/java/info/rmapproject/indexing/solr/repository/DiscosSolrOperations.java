package info.rmapproject.indexing.solr.repository;

import info.rmapproject.core.model.RMapStatus;
import info.rmapproject.indexing.solr.model.DiscoSolrDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.PartialUpdate;
import org.springframework.data.solr.core.query.SimpleQuery;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static info.rmapproject.indexing.solr.model.DiscoSolrDocument.DISCO_STATUS;
import static info.rmapproject.indexing.solr.model.DiscoSolrDocument.DOC_ID;
import static info.rmapproject.indexing.solr.model.DiscoSolrDocument.DOC_LAST_UPDATED;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class DiscosSolrOperations {

    private static final Logger LOG = LoggerFactory.getLogger(DiscosSolrOperations.class);
    
    private SolrTemplate template;
    
    private String coreName;

    DiscosSolrOperations(SolrTemplate template, String coreName) {
        this.template = template;
        this.coreName = coreName;
    }

    /**
     * Updates the {@link DiscoSolrDocument#DISCO_STATUS disco_status} of Solr documents that have a
     * {@link DiscoSolrDocument#DISCO_URI disco_uri} matching the supplied {@code discoUri}.  The matching Solr
     * documents may be filtered by supplying a {@code Predicate}, in which case only the filtered Solr documents will
     * be updated.
     * <p>
     * Note that if the {@code filter} is being used to perform a potentially expensive computation, or if the response
     * from the index contains many matches that will be filtered by a trivial computation, it may be worth considering
     * adding a repository-specific method expressing the more narrow criteria, and invoking that method instead.  It is
     * likely the index will be able to apply the filtering logic in a more performant manner than the supplied
     * {@code filter}.
     * </p>
     * <p>
     * Implementation note: this method uses the {@link SolrTemplate} in order to perform a <em>partial update</em> of
     * the matching documents.  This is for two reasons: 1) partial updates are more efficient, 2) round-tripping the
     * entire {@link DiscoSolrDocument} is not possible due to how the {@link org.apache.solr.common.util.JavaBinCodec}
     * writes dates in Solr responses.
     * </p>
     *
     * @param discoUri  the URI of the DiSCO
     * @param status the status matching DiSCOs will be updated to
     * @param matching    an optional {@code Predicate} used to selectively apply status updates, may be {@code null}
     */
    public void updateStatus(String discoUri, RMapStatus status, Predicate<DiscoSolrDocument> matching) {
        LOG.debug("Updating the status of the following documents with DiSCO iri {} to {}", discoUri, status);

        Set<DiscoPartialUpdate> statusUpdates;

        Page<DiscoSolrDocument> results = template.query(coreName,
                new SimpleQuery(prepareDiscoUriQuery(discoUri)), DiscoSolrDocument.class);

        try (Stream<DiscoSolrDocument> documentStream =
                     results.stream()) {

            Stream<DiscoSolrDocument> filtered = documentStream;
            if (matching != null) {
                filtered = documentStream.filter(matching);
            }

            statusUpdates = preparePartialUpdateOverDocuments(filtered, (partialUpdate) -> {
                partialUpdate.setValueOfField(DISCO_STATUS, status.toString());
                partialUpdate.setValueOfField(DOC_LAST_UPDATED, System.currentTimeMillis());
                LOG.debug("Set document id {} status to {}", partialUpdate.getIdField().getValue(), status);
            });
        }

        if (statusUpdates.size() > 0) {
            template.saveBeans(coreName, statusUpdates);
            template.commit(coreName);
        }    
    }

    /**
     * Removes {@link DiscoSolrDocument documents} from the index that participate in the specified lineage. All
     * documents with a {@link DiscoSolrDocument#EVENT_LINEAGE_PROGENITOR_URI lineage URI} equal to {@code lineageUri}
     * will be deleted from the index, regardless of the document {@link DiscoSolrDocument#DISCO_STATUS status}.
     *
     * @param lineageUri the lineage to delete from the index
     */
    public void deleteDocumentsForLineage(String lineageUri) {
        Page<DiscoSolrDocument> results = template.query(coreName,
                new SimpleQuery(prepareDiscoLineageUriQuery(lineageUri)), DiscoSolrDocument.class);

        Set<String> idsToDelete;
        try (Stream<DiscoSolrDocument> documentStream =
                     results.stream()) {
            idsToDelete = documentStream.map(DiscoSolrDocument::getDocId).collect(toSet());
        }

        LOG.debug("Deleting the following documents with lineage progenitor uri {}: {}", lineageUri,
                idsToDelete.stream().collect(joining(",")));

        if (idsToDelete.size() > 0) {
            template.deleteByIds(coreName, idsToDelete);
            template.commit(coreName);
        }
    }

    /**
     * Creates a {@link PartialUpdate} instance for each {@code DiscoSolrDocument}. The supplied {@code Consumer} is
     * applied to each {@code PartialUpdate}, setting the state of each update in preparation for being sent to the
     * index.
     *
     * @param documents the {@code DiscoSolrDocument}s to update
     * @param updater   sets the state of each {@code PartialUpdate}
     * @return a {@code Set} of {@code PartialUpdate} instances their state containing the commands to be sent to the
     * index
     */
    private Set<DiscoPartialUpdate> preparePartialUpdateOverDocuments(Stream<DiscoSolrDocument> documents,
                                                                      Consumer<DiscoPartialUpdate> updater) {
        return documents.map(doc -> new DiscoPartialUpdate(DOC_ID, doc.getDocId(), doc.getDiscoUri()))
                .peek(updater)
                .collect(toSet());
    }

    /**
     * Creates a properly-escaped Solr query for retrieving a DiscoSolrDocument by the DISCO_URI field.
     *
     * @param discoUri the disco uri
     * @return the query, properly escaped, ready to be executed
     */
    static String prepareDiscoUriQuery(String discoUri) {
        return DiscoSolrDocument.DISCO_URI + ":" + discoUri.replaceAll(":", "\\\\:");
    }

    static String prepareDiscoLineageUriQuery(String lineageUri) {
        return DiscoSolrDocument.EVENT_LINEAGE_PROGENITOR_URI + ":" + lineageUri.replaceAll(":", "\\\\:");
    }
}
