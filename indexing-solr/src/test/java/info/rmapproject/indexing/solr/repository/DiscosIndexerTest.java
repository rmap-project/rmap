package info.rmapproject.indexing.solr.repository;

import info.rmapproject.indexing.solr.AbstractSpringIndexingTest;
import info.rmapproject.indexing.solr.model.DiscoSolrDocument;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.QueryStringHolder;
import org.springframework.data.solr.core.query.result.SolrResultPage;
import org.springframework.data.solr.repository.SolrCrudRepository;

import static info.rmapproject.core.model.RMapStatus.ACTIVE;
import static info.rmapproject.core.model.RMapStatus.DELETED;
import static info.rmapproject.core.model.RMapStatus.INACTIVE;
import static info.rmapproject.core.model.RMapStatus.TOMBSTONED;
import static info.rmapproject.core.model.event.RMapEventType.CREATION;
import static info.rmapproject.core.model.event.RMapEventType.DELETION;
import static info.rmapproject.core.model.event.RMapEventType.DERIVATION;
import static info.rmapproject.core.model.event.RMapEventType.INACTIVATION;
import static info.rmapproject.core.model.event.RMapEventType.TOMBSTONE;
import static info.rmapproject.core.model.event.RMapEventType.UPDATE;
import static info.rmapproject.indexing.IndexUtils.EventDirection.SOURCE;
import static info.rmapproject.indexing.IndexUtils.EventDirection.TARGET;
import static info.rmapproject.indexing.solr.model.DiscoSolrDocument.CORE_NAME;
import static info.rmapproject.indexing.solr.repository.DiscosSolrOperations.prepareDiscoLineageUriQuery;
import static info.rmapproject.indexing.solr.repository.DiscosSolrOperations.prepareDiscoUriQuery;
import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class DiscosIndexerTest extends AbstractSpringIndexingTest {

    private DiscosIndexer underTest;

    private SolrTemplate mockTemplate;

    @Before
    @SuppressWarnings("unchecked")
    public void setUpMocks() throws Exception {
        this.mockTemplate = mock(SolrTemplate.class);
        underTest = new DiscosIndexer(mock(EventDiscoTupleMapper.class), mock(SolrCrudRepository.class), mockTemplate);
    }

    /**
     * Nothing happens post-index when the document represents the target of an event.
     */
    @Test
    public void testPostIndexEventTargets() {
        DiscoSolrDocument targetOfEvent = new DiscoSolrDocument.Builder().discoEventDirection(TARGET.name()).build();

        underTest.postIndex(targetOfEvent);

        verifyZeroInteractions(mockTemplate);
    }

    /**
     * Nothing happens on post-index for creation events.
     */
    @Test
    public void testPostIndexCreate() {
        DiscoSolrDocument createDoc = new DiscoSolrDocument.Builder()
                .discoEventDirection(SOURCE.name())
                .eventType(CREATION.name())
                .build();

        underTest.postIndex(createDoc);

        verifyZeroInteractions(mockTemplate);
    }

    /**
     * Nothing happens on post-index for derivation events.
     */
    @Test
    public void testPostIndexDerive() {
        DiscoSolrDocument createDoc = new DiscoSolrDocument.Builder()
                .discoEventDirection(SOURCE.name())
                .eventType(DERIVATION.name())
                .build();

        underTest.postIndex(createDoc);

        verifyZeroInteractions(mockTemplate);
    }

    /**
     * When an document representing an update event is post-indexed, the disco that is the source of the event is
     * to be considered inactive (supplanted by the disco that is the target of the event).  So, post-index, existing
     * documents in the solr index that reference the source disco are inactivated.
     */
    @Test
    public void testPostIndexUpdate() {
        String discoUri = "http://a/disco/to/be/inactivated";
        DiscoSolrDocument updateDoc = new DiscoSolrDocument.Builder()
                .docId("justindexed")
                .eventType(UPDATE.name())
                .discoEventDirection(SOURCE.name())
                .discoUri(discoUri)
                .discoStatus(ACTIVE.name())
                .build();

        DiscoSolrDocument existingToBeInactivated = new DiscoSolrDocument.Builder()
                .docId("tobeinactivated")
                .discoUri(discoUri)
                .discoStatus(ACTIVE.name())
                .build();

        DiscoSolrDocument existingAlreadyInactive = new DiscoSolrDocument.Builder()
                .docId("alreadyinactive")
                .discoUri(discoUri)
                .discoStatus(INACTIVE.name())
                .build();

        SolrResultPage<DiscoSolrDocument> queryResults = new SolrResultPage<>(
                asList(updateDoc, existingToBeInactivated, existingAlreadyInactive));

        when(mockTemplate.saveBeans(eq(CORE_NAME), anySet())).thenAnswer(inv -> {
            UpdateAssertions.Builder builder = new UpdateAssertions.Builder();
            return builder
                    .updateMustContainHavingStatus(partialUpdate -> docIdMatches(partialUpdate, "tobeinactivated"), INACTIVE)
                    .updateMustNotContain((partialUpdate) -> docIdMatches(partialUpdate, "alreadyinactive"))
                    .updateMustNotContain((partialUpdate) -> docIdMatches(partialUpdate, "justindexed"))
                    .build()
                    .answer(inv);
        });

        when(mockTemplate.query(
                eq(CORE_NAME),
                argThat(query -> discoQueryMatches(discoUri, query)),
                eq(DiscoSolrDocument.class)))
        .thenReturn(queryResults);

        // Invoke post-index with the document that updates the existing document in the index
        try {
            underTest.postIndex(updateDoc);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        // verify the expectations
        verify(mockTemplate).query(
                eq(CORE_NAME),
                argThat(query -> discoQueryMatches(discoUri, query)),
                eq(DiscoSolrDocument.class));
        verify(mockTemplate).saveBeans(eq(CORE_NAME), anySet());
        verify(mockTemplate).commit(CORE_NAME);
    }

    /**
     * When a document representing an inactivation event is post-indexed, all documents in the index that refer to the
     * source of the inactivation event have their status set to INACTIVE
     */
    @Test
    public void testPostIndexInactivate() {
        String discoUri = "http://a/disco/to/be/inactivated";
        DiscoSolrDocument inactivateDoc = new DiscoSolrDocument.Builder()
                .docId("justindexed")
                .eventType(INACTIVATION.name())
                .discoEventDirection(SOURCE.name())
                .discoUri(discoUri)
                .build();

        DiscoSolrDocument existingToBeInactivated = new DiscoSolrDocument.Builder()
                .docId("tobeinactivated")
                .discoUri(discoUri)
                .discoStatus(ACTIVE.name())
                .build();

        DiscoSolrDocument existingAlreadyInactive = new DiscoSolrDocument.Builder()
                .docId("alreadyinactive")
                .discoUri(discoUri)
                .discoStatus(INACTIVE.name())
                .build();

        // on update, the index will be searched for documents for the disco being invalidated; return
        // the document to be invalidated when this search occurs.
        when(mockTemplate.query(
                eq(CORE_NAME),
                argThat(query -> discoQueryMatches(discoUri, query)),
                eq(DiscoSolrDocument.class)))
                .thenReturn(new SolrResultPage<>(asList(inactivateDoc, existingAlreadyInactive, existingToBeInactivated)));

        when(mockTemplate.saveBeans(eq(CORE_NAME), anySet())).thenAnswer(inv -> {
            UpdateAssertions.Builder builder = new UpdateAssertions.Builder();
            return builder
                    .updateMustContainHavingStatus(partialUpdate -> docIdMatches(partialUpdate, "tobeinactivated"), INACTIVE)
                    .updateMustNotContain((partialUpdate) -> docIdMatches(partialUpdate, "alreadyinactive"))
                    .updateMustNotContain((partialUpdate) -> docIdMatches(partialUpdate, "justindexed"))
                    .build()
                    .answer(inv);
        });

        // Invoke post-index with the document that updates the existing document in the index
        underTest.postIndex(inactivateDoc);

        // verify the expectations
        verify(mockTemplate).query(
                eq(CORE_NAME),
                argThat(query -> discoQueryMatches(discoUri, query)),
                eq(DiscoSolrDocument.class));
        verify(mockTemplate).saveBeans(eq(CORE_NAME), anySet());
        verify(mockTemplate).commit(CORE_NAME);
    }

    /**
     * When a document representing an tombstone event is post-indexed, all documents in the index that refer to the
     * source of the inactivation event have their status set to TOMBSTONED
     */
    @Test
    public void testIndexTombstone() {
        String discoUri = "http://a/disco/to/be/tombstoned";
        DiscoSolrDocument tombstoneDoc = new DiscoSolrDocument.Builder()
                .docId("justindexed")
                .eventType(TOMBSTONE.name())
                .discoEventDirection(SOURCE.name())
                .discoUri(discoUri)
                .build();

        DiscoSolrDocument existingToBeTombstoned_1 = new DiscoSolrDocument.Builder()
                .docId("tobetombstoned_1")
                .discoUri(discoUri)
                .discoStatus(ACTIVE.name())
                .build();

        DiscoSolrDocument existingToBeTombstoned_2 = new DiscoSolrDocument.Builder()
                .docId("tobetombstoned_2")
                .discoUri(discoUri)
                .discoStatus(INACTIVE.name())
                .build();

        // on update, the index will be searched for documents for the disco being invalidated; return
        // the document to be invalidated when this search occurs.
        when(mockTemplate.query(
                eq(CORE_NAME),
                argThat(query -> discoQueryMatches(discoUri, query)),
                eq(DiscoSolrDocument.class)))
                .thenReturn(new SolrResultPage<>(asList(tombstoneDoc, existingToBeTombstoned_1, existingToBeTombstoned_2)));

        when(mockTemplate.saveBeans(eq(CORE_NAME), anySet())).thenAnswer(inv -> {
            UpdateAssertions.Builder builder = new UpdateAssertions.Builder();
            return builder
                    .updateMustContainHavingStatus((partialUpdate) -> docIdMatches(partialUpdate, "tobetombstoned_1"), TOMBSTONED)
                    .updateMustContainHavingStatus((partialUpdate) -> docIdMatches(partialUpdate, "tobetombstoned_2"), TOMBSTONED)
                    .build()
                    .answer(inv);
        });

        // Invoke post-index with the document that updates the existing document in the index
        underTest.postIndex(tombstoneDoc);

        // verify the expectations
        verify(mockTemplate).query(
                eq(CORE_NAME),
                argThat(query -> discoQueryMatches(discoUri, query)),
                eq(DiscoSolrDocument.class));
        verify(mockTemplate).saveBeans(eq(CORE_NAME), anySet());
        verify(mockTemplate).commit(CORE_NAME);
    }

    /**
     * When a document representing an tombstone event is post-indexed, all documents in the index that refer to the
     * source of the inactivation event have their status set to DELETED
     */
    @Test
    public void testIndexDelete() throws Exception {
        String lineageUri = "http://progentior/lineage/id";
        String discoUri = "http://a/disco/to/be/deleted";
        DiscoSolrDocument tombstoneDoc = new DiscoSolrDocument.Builder()
                .docId("justindexed")
                .eventLineageUri(lineageUri)
                .eventType(DELETION.name())
                .discoEventDirection(SOURCE.name())
                .discoUri(discoUri)
                .build();

        DiscoSolrDocument existingToBeDeleted_1 = new DiscoSolrDocument.Builder()
                .docId("tobedeleted_1")
                .discoUri(discoUri)
                .discoStatus(ACTIVE.name())
                .eventLineageUri(lineageUri)
                .build();

        DiscoSolrDocument existingToBeDeleted_2 = new DiscoSolrDocument.Builder()
                .docId("tobedeleted_2")
                .discoUri(discoUri)
                .discoStatus(INACTIVE.name())
                .eventLineageUri(lineageUri)
                .build();

        // on update, the index will be searched for documents for the disco being invalidated; return
        // the document to be invalidated when this search occurs.
        when(mockTemplate.query(
                eq(CORE_NAME),
                argThat(query -> lineageQueryMatches(lineageUri, query)),
                eq(DiscoSolrDocument.class)))
                .thenReturn(new SolrResultPage<>(asList(tombstoneDoc, existingToBeDeleted_1, existingToBeDeleted_2)));

        when(mockTemplate.deleteByIds(eq(CORE_NAME), anyCollection())).thenAnswer(inv -> {
            UpdateAssertions.Builder builder = new UpdateAssertions.Builder();
            return builder
                    .deleteMustContain("justindexed")
                    .deleteMustContain("tobedeleted_1")
                    .deleteMustContain("tobedeleted_2")
                    .build()
                    .answer(inv);
        });

        // Invoke post-index with the document that updates the existing document in the index
        underTest.postIndex(tombstoneDoc);

        // verify the expectations
        verify(mockTemplate).query(
                eq(CORE_NAME),
                argThat(query -> lineageQueryMatches(lineageUri, query)),
                eq(DiscoSolrDocument.class));
        verify(mockTemplate).deleteByIds(eq(CORE_NAME), anyCollection());
        verify(mockTemplate).commit(CORE_NAME);
    }

    private static boolean docIdMatches(DiscoPartialUpdate partialUpdate, String docId) {
        boolean result = partialUpdate.getIdField().getValue().equals(docId);
        LOG.debug("Partial update for disco {} {} document ID {}",
                partialUpdate.getDiscoIri(), (result) ? "matched" : "did not match", docId);
        return result;
    }

    private static boolean discoQueryMatches(String discoUri, Query query) {
        return ((QueryStringHolder) query.getCriteria())
                .getQueryString()
                .equals(prepareDiscoUriQuery(discoUri));
    }

    private static boolean lineageQueryMatches(String lineageUri, Query query) {
        return ((QueryStringHolder) query.getCriteria())
                .getQueryString()
                .equals(prepareDiscoLineageUriQuery(lineageUri));
    }

}