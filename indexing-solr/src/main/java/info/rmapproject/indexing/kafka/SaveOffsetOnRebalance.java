package info.rmapproject.indexing.kafka;

import info.rmapproject.indexing.IndexUtils;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class SaveOffsetOnRebalance<K, V> implements ConsumerAwareRebalanceListener<K, V> {

    /**
     * The default seek behavior if the requested offset for a topic and partition cannot be found.
     */
    public static final Seek DEFAULT_SEEK_BEHAVIOR = Seek.LATEST;

    /**
     * Logging.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SaveOffsetOnRebalance.class);

    /**
     * The Kafka consumer
     */
    private Consumer<K, V> consumer;

    /**
     * The interface used to look up offsets for a given topic and partition
     */
    private OffsetLookup offsetLookup;

    /**
     * Seek behavior if the requested offset for a topic and partition cannot be found.
     */
    private Seek seekBehavior = DEFAULT_SEEK_BEHAVIOR;

    /**
     * Constructs a new rebalancer which uses the supplied {@link OffsetLookup} for looking up offsets.  The
     * {@link Consumer} must be {@link #setConsumer(Consumer) set} after construction.
     *
     * @param offsetLookup responsible for looking up offsets for a topic/partition pair, must not be {@code null}
     * @throws IllegalArgumentException if {@code offsetLookup} is {@code null}
     */
    public SaveOffsetOnRebalance(OffsetLookup offsetLookup) {
        this.offsetLookup = IndexUtils.assertNotNull(offsetLookup, "OffsetLookup must not be null.");
    }

    /**
     * Constructs a new rebalancer which uses the supplied {@link OffsetLookup} for looking up offsets.
     *
     * @param offsetLookup responsible for looking up offsets for a topic/partition pair, must not be {@code null}
     * @param consumer the target of seek operations, must not be {@code null}
     * @throws IllegalArgumentException if {@code offsetLookup} or {@code consumer} is {@code null}
     */
    public SaveOffsetOnRebalance(OffsetLookup offsetLookup, Consumer<K, V> consumer) {
        this(offsetLookup);
        this.consumer = IndexUtils.assertNotNull(consumer, "Consumer must not be null.");
    }

    /**
     * Sets the {@code Consumer}, which is the target of seek operations.
     *
     * @param consumer the {@code Consumer}, must not be {@code null}
     * @throws IllegalArgumentException if {@code consumer} is {@code null}
     */
    @Override
    public void setConsumer(Consumer<K, V> consumer) {
        this.consumer = IndexUtils.assertNotNull(consumer, "Consumer must not be null.");
    }

    /**
     * The seek behavior when an offset cannot be found for a topic/partition pair.  When an offset cannot be found,
     * the {@link #setConsumer(Consumer) consumer} will be directed to seek to either the {@link Seek#EARLIEST earliest}
     * or {@link Seek#LATEST latest} offset.
     *
     * @return directs the {@code Consumer} to seek to the earliest or latest offset when the exact offset of the
     *         topic/partition cannot be determined
     */
    public Seek getSeekBehavior() {
        return seekBehavior;
    }

    /**
     * The seek behavior when an offset cannot be found for a topic/partition pair.  When an offset cannot be found,
     * the {@link #setConsumer(Consumer) Consumer} will be directed to seek to either the {@link Seek#EARLIEST earliest}
     * or {@link Seek#LATEST latest} offset.
     *
     * @param seekBehavior directs the {@code Consumer} to seek to the earliest or latest offset when the exact offset
     *                     of the topic/partition cannot be determined
     */
    @Override
    public void setSeekBehavior(Seek seekBehavior) {
        this.seekBehavior = IndexUtils.assertNotNull(seekBehavior, "Seek behavior must not be null.");
    }

    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
        LOG.debug("Received {} event", "onPartitionsRevoked");

        Map<TopicPartition, OffsetAndMetadata> commits = partitions
                .stream()
                .collect(Collectors.toMap(
                        Function.identity(), tp -> new OffsetAndMetadata(consumer.position(tp))));

        KafkaUtils.commitOffsets(consumer, commits, false);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation notes: attempts to look up the offset for the supplied {@code TopicPartition}s using the {@link
     * OffsetLookup} supplied on construction.  If the {@code OffsetLookup} cannot determine the last committed offset,
     * this implementation will direct the {@link #setConsumer(Consumer) Consumer} to seek to the earliest or latest
     * offset, based on the {@link #getSeekBehavior() seek behavior}.
     * </p>
     * @param partitions the topic partition pairs being assigned to the {@code Consumer}
     */
    @Override
    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
        LOG.debug("Received {} event", "onPartitionsAssigned");

        partitions.forEach(tp -> {
            // determine latest offset
            long off = offsetLookup.lookupOffset(tp.topic(), tp.partition(), Seek.LATEST);

            if (off > -1) {
                // seek to offset + 1 so we don't re-read events on the next poll()
                off += 1;
                consumer.seek(tp, off);
            } else {
                LOG.debug("OffsetLookup could not determine offset, seeking to the {} offset for topic/partition {}/{}",
                        (seekBehavior == Seek.LATEST) ? "latest" : "earliest", tp.topic(), tp.partition());

                Set<TopicPartition> tpCollection = Collections.singleton(tp);
                switch (seekBehavior) {
                    case LATEST:
                    {
                        consumer.seekToEnd(tpCollection);
                        break;
                    }
                    case EARLIEST:
                    {
                        consumer.seekToBeginning(tpCollection);
                        break;
                    }
                    default:
                    {
                        throw new IllegalArgumentException("Unknown seek behavior value " + seekBehavior);
                    }
                }
            }

            long pos = consumer.position(tp);
            LOG.debug("Requested offset {}, performed seek to actual offset {} for topic/partition {}/{}",
                    off, pos, tp.topic(), tp.partition());
        });
    }

}
