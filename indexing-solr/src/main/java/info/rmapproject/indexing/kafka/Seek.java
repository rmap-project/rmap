package info.rmapproject.indexing.kafka;

/**
 * Represents seek behavior for a {@link org.apache.kafka.clients.consumer.Consumer}.
 *
 * @see OffsetLookup#lookupOffset(String, int, Seek)
 * @see SolrOffsetLookup#lookupOffset(String, int, Seek)
 * @see SaveOffsetOnRebalance#seekBehavior
 */
public enum Seek {

    /**
     * Seek to the earliest available offset.
     */
    EARLIEST,

    /**
     * Seek to the latest available offset.
     */
    LATEST
}
