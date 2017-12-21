package info.rmapproject.indexing.solr.model;

import info.rmapproject.indexing.kafka.IndexingConsumer;
import info.rmapproject.indexing.kafka.Seek;
import org.apache.kafka.common.TopicPartition;

/**
 * Solr documents that carry Kafka metadata will implement this interface.  Kafka metadata includes the topic,
 * partition, and offset that originated - or contributed to - information in the Solr document.  Storing Kafka metadata
 * in Solr allows for fulfilling exactly-once message semantics, avoiding the re-indexing of of Kafka records that
 * already exist in Solr.
 *
 * @see org.apache.kafka.clients.consumer.Consumer#seek(TopicPartition, long)
 * @see IndexingConsumer#consume(String, Seek)
 * @see info.rmapproject.indexing.kafka.OffsetLookup#lookupOffset(String, int, Seek)
 * @see info.rmapproject.indexing.solr.repository.KafkaMetadataRepository
 */
public interface KafkaMetadata {

    /**
     * Solr field name for storing the Kafka topic
     */
    String KAFKA_TOPIC = "kafka_topic";

    /**
     * Solr field name for storing the Kafka partition
     */
    String KAFKA_PARTITION = "kafka_partition";

    /**
     * Solr field name for storing the Kafka offset
     */
    String KAFKA_OFFSET = "kafka_offset";

    /**
     * The Kafka offset of the topic and partition that contributed in some way to this document.
     *
     * @return the offset
     */
    long getKafkaOffset();

    /**
     * The Kafka partition of the topic that contributed in some way to this document.
     *
     * @return the partition
     */
    int getKafkaPartition();

    /**
     * The Kafka topic that contributed in some way to this document.
     *
     * @return the topic
     */
    String getKafkaTopic();

}
