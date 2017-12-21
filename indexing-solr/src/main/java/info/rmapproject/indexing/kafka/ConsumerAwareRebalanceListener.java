package info.rmapproject.indexing.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;

/**
 * Kafka re-balancer that allows the {@link Consumer} to be injected.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 * @see <a href="https://kafka.apache.org/documentation/#impl_consumerrebalance">Kafka documentation</a>
 */
public interface ConsumerAwareRebalanceListener<K, V> extends ConsumerRebalanceListener {

    /**
     * The {@link Consumer} to be updated when rebalancing occurs.
     *
     * @param consumer the consumer
     */
    void setConsumer(Consumer<K, V> consumer);

    /**
     * What the rebalancer should do when it cannot determine the current offset of the consumer
     *
     * @param seekBehavior the seek behavior
     */
    void setSeekBehavior(Seek seekBehavior);

}
