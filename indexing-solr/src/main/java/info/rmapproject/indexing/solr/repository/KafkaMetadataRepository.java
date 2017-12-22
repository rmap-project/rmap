package info.rmapproject.indexing.solr.repository;

import info.rmapproject.indexing.solr.model.KafkaMetadata;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * Implemented by all repositories that contain Solr documents extending {@link KafkaMetadata}
 *
 * @param <T> the solr document type which contains Kafka metadata
 */
public interface KafkaMetadataRepository<T extends KafkaMetadata> {

    /**
     * Sorts documents by the {@link KafkaMetadata#KAFKA_OFFSET kafka_offset} field in descending order
     * (latest offset at the head of the results).
     */
    Sort SORT_DESC_BY_KAFKA_OFFSET = new Sort(Sort.Direction.DESC, KafkaMetadata.KAFKA_OFFSET);
    /**
     * Sorts documents by the {@link KafkaMetadata#KAFKA_OFFSET kafka_offset} field, in ascending order
     * (earliest offset at the head of the results).
     */
    Sort SORT_ASC_BY_KAFKA_OFFSET = new Sort(Sort.Direction.ASC, KafkaMetadata.KAFKA_OFFSET);

    /**
     * Searches a Solr repository for the document that contains the earliest or latest (according to the {@code sort}
     * parameter) Kafka offset for the provided topic and partition.
     * <p>
     * To find the latest offset, be sure to sort <em>descending</em> on the
     * </p>
     *
     * @param topic the Kafka topic
     * @param partition the Kafka partition
     * @param sort the sort order of the returned {@code List}
     * @return a List of Solr documents containing Kafka metadata for the supplied {@code topic} and {@code partition},
     *         ordered by {@code sort}
     */
    List<T> findTopDiscoSolrDocumentByKafkaTopicAndKafkaPartition(String topic, int partition, Sort sort);

}
