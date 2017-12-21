package info.rmapproject.indexing.solr.repository;

import info.rmapproject.indexing.solr.model.DiscoSolrDocument;
import org.springframework.data.solr.repository.SolrCrudRepository;

import java.net.URI;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.query.result.FacetAndHighlightPage;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.repository.Facet;
import org.springframework.data.solr.repository.Highlight;
import org.springframework.data.solr.repository.Pivot;
import org.springframework.data.solr.repository.Query;

import info.rmapproject.indexing.IndexUtils;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 * @author khanson
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

    /**
     * DiSCO search on aggregated resources, description, additional statements, disco URI, agent name, and agent URI.  Filters for status, 
     * agent URI and creation date of DiSCO can be added.
     * @param search - search string e.g. (*rmap* AND *disco*)
     * @param statusFilter e.g. active
     * @param agentFilter e.g. ark:/00000/afjksdfj
     * @param createDateFilter e.g. 2015-05-20T00:00:00.000Z TO *
     * @param pageable
     * @return
     */
    @Query(value="{!type=edismax v='?0' qf='disco_aggregated_resource_uris disco_description disco_creator_uri disco_providerid disco_related_statements disco_uri agent_name agent_uri'}", 
    		filters={"disco_event_direction:TARGET","disco_status:?1","agent_uri:?2","event_start_time:[?3]"})
    @Highlight(fields="disco_description disco_aggregated_resource_uris disco_related_statements disco_creator_uri disco_providerid", prefix=IndexUtils.HL_PREFIX, postfix=IndexUtils.HL_POSTFIX)
    @Facet(fields={"disco_status"}, pivots={@Pivot({"agent_uri","agent_name"})})
    FacetAndHighlightPage<DiscoSolrDocument> findDiscoSolrDocumentsGeneralSearch(String search, String statusFilter, String agentFilter, String createDateFilter, Pageable pageable);
    
    /**
     * Search DiscoSolrDocuments by query on statements and highlight matches in query statements.
     * @param search
     * @param statusFilter
     * @param pageable
     * @return
     */
    @Query(value="disco_related_statements:?0", filters={"disco_event_direction:TARGET","disco_status:?1"})
    @Highlight(fields="disco_related_statements", prefix=IndexUtils.HL_PREFIX, postfix=IndexUtils.HL_POSTFIX)
    HighlightPage<DiscoSolrDocument> findDiscoSolrDocumentsUsingRelatedStmtsAndHighlight(String search, String statusFilter, Pageable pageable);
    
}
