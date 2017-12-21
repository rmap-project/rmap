package info.rmapproject.indexing.kafka;

/**
 * Determine the latest or earliest offset for a given topic and partition.
 */
@FunctionalInterface
public interface OffsetLookup {

    /**
     * Look up the latest or earliest offset for the supplied topic and partition.  If the offset cannot be determined,
     * {@code -1} should be returned.
     * <p>
     * Clients wishing to (re-)process the entire Kafka log for a particular topic/partition will want to seek to the
     * earliest offset, clients wishing to process only new events will want to seek to the latest offset.
     * </p>
     *
     * @param topic the topic
     * @param partition the partition
     * @param seek whether to look up the earliest offset or the latest offset
     * @return the offset or -1 if it cannot be determined
     */
    long lookupOffset(String topic, int partition, Seek seek);

}
