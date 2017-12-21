package info.rmapproject.indexing.kafka;

import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.indexing.IndexingInterruptedException;
import info.rmapproject.indexing.IndexingTimeoutException;
import info.rmapproject.indexing.solr.model.DiscoSolrDocument;
import info.rmapproject.indexing.solr.model.KafkaMetadata;
import info.rmapproject.indexing.solr.repository.IndexDTO;

import java.util.function.Consumer;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
@FunctionalInterface
public interface IndexingRetryHandler {

    void retry(RMapEvent event, KafkaMetadata metadata,  Consumer<DiscoSolrDocument> documentDecorator)
            throws IndexingTimeoutException, IndexingInterruptedException;

}
