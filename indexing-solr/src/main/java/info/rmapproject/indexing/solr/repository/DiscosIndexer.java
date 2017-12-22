package info.rmapproject.indexing.solr.repository;

import info.rmapproject.core.model.RMapStatus;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.indexing.IndexUtils;
import info.rmapproject.indexing.solr.model.DiscoSolrDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.repository.SolrCrudRepository;

import java.util.function.Function;

import static info.rmapproject.core.model.RMapStatus.ACTIVE;
import static info.rmapproject.indexing.solr.model.DiscoSolrDocument.CORE_NAME;

/**
 * Performs  pre- and post-indexing tasks for the "discos" Solr core, including updating the state of existing documents
 * in the index based on newly indexed documents.
 */
public class DiscosIndexer extends AbstractEventTupleIndexer<DiscoSolrDocument, Long> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private DiscosSolrOperations discoOperations;

    /**
     * {@inheritDoc}
     *
     * @param documentMapper {@inheritDoc}
     * @param repository {@inheritDoc}
     * @param template {@inheritDoc}
     */
    public DiscosIndexer(Function<EventDiscoTuple, DiscoSolrDocument> documentMapper,
                         SolrCrudRepository<DiscoSolrDocument, Long> repository, SolrTemplate template) {
        super(documentMapper, repository, template);

        // There *is* a Spring Bean definition for DiscoSolrOperations that *ought* to be injected by this constructor.
        // However, DiscoSolrOperations test coverage is nill.  Testing of DiscoSolrOperations is indirectly
        // accomplished by the DiscosIndexerTest, whereby expectations on the SolrTemplate are verified.
        // If DiscoSolrOperations is injected into this constructor, then the DiscoIndexerTest would need to be
        // re-written with expectations against DiscoSolrOperations, not the SolrTemplate.  And DiscoSolrOperations
        // would need its own test.  All good things, but due to lack of time, leaving this as a TODO.
        this.discoOperations = new DiscosSolrOperations(template, CORE_NAME);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation notes: this is a no-op.
     * </p>
     *
     * @param tuple {@inheritDoc}
     */
    @Override
    public void preIndex(EventDiscoTuple tuple) {
        // no-op
    }

    /**
     * {@inheritDoc}
     * <p>
     * Updates the {@link DiscoSolrDocument#DISCO_STATUS disco_status} of <em>existing</em> documents in the index,
     * based on the {@link DiscoSolrDocument#getEventType() event type} of the provided document.
     * </p>
     * <p>
     * For example, if the provided document is a {@link RMapEventType#CREATION CREATION} event, then nothing needs
     * to be done to the index.  If the provided document is a {@link RMapEventType#UPDATE} event, then existing
     * documents in the index that refer to disco being updated need to be inactivated.
     * </p>
     * <p>
     * Implementation note: post-indexing operations are limited to discos that were the source of an event
     * </p>
     *
     * @param doc the Solr document representing the {@link EventDiscoTuple} that was recently indexed
     */
    @Override
    public void postIndex(DiscoSolrDocument doc) {

        // Right now, we only perform post-indexing operations on the discos that were the source of an event
        try {
            if (IndexUtils.EventDirection.valueOf(doc.getDiscoEventDirection()) == IndexUtils.EventDirection.TARGET) {
                log.trace("Not operating on document {} for disco {} with EventDirection {}",
                        doc.getDocId(), doc.getDiscoUri(), doc.getDiscoEventDirection());
                return;
            }
        } catch (IllegalArgumentException e) {
            log.warn("Unknown event direction {} found in Solr document {} for event {} (a {} event)",
                    doc.getDiscoEventDirection(), doc.getDocId(), doc.getEventUri(), doc.getEventType());
            return;
        }

        /*
              Event            Source                    Target
              =====            ======                    ======
              CREATION         n/a                       created disco, or created agent
              UPDATE           disco being updated       updated disco
              DELETE           disco being deleted       n/a
              REPLACE          agent being updated       updated agent
              TOMBSTONE        disco being tombed        n/a
              DERIVATION       disco being derived from  the derived disco
              INACTIVATION     disco being inactivated   n/a
         */

        RMapEventType eventType = RMapEventType.valueOf(doc.getEventType().toUpperCase());

        switch (eventType) {
            case CREATION:
                // no-op, nothing needs to be done to the discos index
            case DERIVATION:
                // discos in the previous lineage do not require updates
                break;

            case UPDATE:
            case INACTIVATION:
                // FIXME: INACTIVATE every ACTIVE document in the newly indexed document's lineage
                discoOperations.updateStatus(doc.getDiscoUri(), RMapStatus.INACTIVE, (candidate) ->
                        !(candidate.getDocId().equals(doc.getDocId()))
                                && candidate.getDiscoStatus().equals(ACTIVE.name()));
                break;

            case DELETION:
                discoOperations.deleteDocumentsForLineage(doc.getEventLineageProgenitorUri());
                break;

            case TOMBSTONE:
                // FIXME: TOMBSTONE all (ACTIVE, INACTIVE) documents in the newly indexed document's lineage
                discoOperations.updateStatus(doc.getDiscoUri(), RMapStatus.TOMBSTONED, null);
                break;

            default:
                throw new RuntimeException("Unknown event type: " + doc.getEventType());

        }
    }

}

