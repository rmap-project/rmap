package info.rmapproject.indexing.solr.repository;

import info.rmapproject.indexing.solr.model.DiscoSolrDocument;
import org.springframework.data.solr.repository.SolrCrudRepository;

import java.net.URI;
import java.util.Set;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public interface DiscoRepository extends SolrCrudRepository<DiscoSolrDocument, Long>,
        KafkaMetadataRepository<DiscoSolrDocument> {

    Set<DiscoSolrDocument> findDiscoSolrDocumentsByDiscoAggregatedResourceUris(URI discoAggregatedResourceUri);

    Set<DiscoSolrDocument> findDiscoSolrDocumentsByDiscoAggregatedResourceUrisContains(String uriSubstring);

    Set<DiscoSolrDocument> findDiscoSolrDocumentsByDiscoStatus(String discoStatus);

    Set<DiscoSolrDocument> findDiscoSolrDocumentsByDiscoUri(String discoUri);

    Set<DiscoSolrDocument> findDiscoSolrDocumentsByDiscoUriAndDiscoStatus(String discoUri, String discoStatus);

    Set<DiscoSolrDocument> findDiscoSolrDocumentsByEventLineageProgenitorUri(String eventLineageProgenitorUri);

    Set<DiscoSolrDocument> findDiscoSolrDocumentsByEventLineageProgenitorUriAndDiscoStatus(String eventLineageProgenitorUri, String discoStatus);

}
