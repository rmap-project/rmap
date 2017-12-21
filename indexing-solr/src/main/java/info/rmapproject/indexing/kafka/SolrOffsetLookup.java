package info.rmapproject.indexing.kafka;

import info.rmapproject.indexing.solr.model.KafkaMetadata;
import info.rmapproject.indexing.solr.repository.KafkaMetadataRepository;

import java.util.List;

import static info.rmapproject.indexing.IndexUtils.assertNotNull;
import static info.rmapproject.indexing.solr.repository.KafkaMetadataRepository.SORT_ASC_BY_KAFKA_OFFSET;
import static info.rmapproject.indexing.solr.repository.KafkaMetadataRepository.SORT_DESC_BY_KAFKA_OFFSET;

/**
 * Determines the earliest or latest offset for a topic and partition by consulting a Solr repository or core.
 * <p>
 * Kafka-related metadata is stored with Solr documents that implement {@link KafkaMetadata}.  This implementation is
 * able to look up Kafka offsets for a given topic and partition by querying a Solr repository containing such
 * documents.
 * </p>
 *
 * @param <T> the solr document type that carries Kafka-related metadata
 */
public class SolrOffsetLookup<T extends KafkaMetadata> implements OffsetLookup {

    private KafkaMetadataRepository<T> repository;

    /**
     * Support offset lookups for any topic in the supplied Map.  The expectation is that Kafka metadata for a topic
     * is kept in exactly one Solr repository.
     *
     * @param repository the Solr repository or core that carries KafkaMetadata
     */
    public SolrOffsetLookup(KafkaMetadataRepository<T> repository) {
        this.repository = assertNotNull(repository, "Repository not be null.");
    }

    @Override
    public long lookupOffset(String topic, int partition, Seek seek) {
        List<T> results = repository.
                findTopDiscoSolrDocumentByKafkaTopicAndKafkaPartition(topic, partition,
                        (seek == Seek.LATEST) ? SORT_DESC_BY_KAFKA_OFFSET : SORT_ASC_BY_KAFKA_OFFSET);

        if (results == null || results.isEmpty()) {
            return -1;
        }

        return results.get(0).getKafkaOffset();
    }
}
