package info.rmapproject.kafka.shared;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A Kafka {@code ProducerFactory} which allows for Producer configuration property values to be resolved <em>just in
 * time</em> (i.e. the time at which a {@code Producer} is {@link #createKafkaProducer() created} by this factory).
 * <p>
 * Producer configuration properties supplied on {@link #JustInTimeConfiguredProducerFactory(Map, Serializer,
 * Serializer) construction} or {@link #addSource(PropertySource) added} {@link #setProperties(List) later} may contain
 * placeholder values, or values that may be considered {@link KafkaPropertyUtils#isNullValue(Object) null}, at the time
 * this factory is instantiated.  For example, the Producer configuration <em>requires</em> a {@code bootstrap.servers}
 * property, whose value may not be known or available when factory instances are created.  This is, in fact, what
 * happens when embedding Kafka in a test scenario.  The test lifecycle demands that a {@code ProducerFactory} be
 * instantiated, but the embedded Kafka broker connection properties are not known until after the embedded Kafka broker
 * has been started.
 * </p>
 * <p>
 * In cases where a Producer configuration value is not known a the time of factory construction, the value may be
 * {@code null} (or a null-equivalent value such as an empty string), or contain a placeholder, using the Spring
 * placeholder syntax (e.g. {@code ${placeholder.value}}).  Placeholders and null values will be resolved from the
 * Spring {@code Environment} when {@link #createKafkaProducer()} is invoked.
 * </p>
 * <p>
 * <em><strong>N.B.</strong></em>: This factory does <em><strong>not</strong></em> support transational producers.
 * </p>
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class JustInTimeConfiguredProducerFactory<K, V> extends DefaultKafkaProducerFactory<K, V>
        implements EnvironmentAware {

    private static final Logger LOG = LoggerFactory.getLogger(JustInTimeConfiguredProducerFactory.class);

    private static final boolean TRANSACTION_CAPABLE = false;

    /**
     * Serializer for Kafka keys
     */
    private final Serializer<K> keySerializer;

    /**
     * Serializer for Kafka values
     */
    private final Serializer<V> valueSerializer;

    private final FactoryState state;

    public JustInTimeConfiguredProducerFactory(Map<String, Object> configs, Serializer<K> keySerializer,
                                               Serializer<V> valueSerializer) {
        super(configs, keySerializer, valueSerializer);
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
        this.state = new FactoryState();

        state.sources.addFirst(new MapPropertySource("producer-construction-config", KafkaPropertyUtils.mapNullValues(configs)));
    }

    @Override
    public Map<String, Object> getConfigurationProperties() {
        return state.getConfigurationProperties();
    }

    public void setProperties(List<Map<String, Object>> props) {
        AtomicInteger count = new AtomicInteger();
        props.forEach(map -> {
            String name = this.getClass().getSimpleName() + "-source" + count.getAndIncrement();
            MapPropertySource ps = new MapPropertySource(name, map);
            addSource(ps);
        });
    }

    @Override
    protected Producer<K, V> createKafkaProducer() {
        LOG.debug("Creating new Kafka Producer ...");
        return new KafkaProducer<>(state.getConfigurationProperties(), this.keySerializer, this.valueSerializer);
    }

    @Override
    public boolean transactionCapable() {
        LOG.debug("This Kafka Producer [{}@{}] is transaction capable: {}", this.getClass().getSimpleName(),
                Integer.toHexString(System.identityHashCode(this)), TRANSACTION_CAPABLE);
        return TRANSACTION_CAPABLE;
    }

    @Override
    public void setEnvironment(Environment environment) {
        state.env = environment;
    }

    public void addSource(PropertySource source) {
        state.sources.addLast(source);
    }

    public void setPrefix(String prefix) {
        state.prefix = prefix;
    }

    public void setStrip(boolean strip) {
        state.strip = strip;
    }

}
