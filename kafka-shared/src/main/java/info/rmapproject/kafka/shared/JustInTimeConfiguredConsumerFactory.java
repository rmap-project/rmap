package info.rmapproject.kafka.shared;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class JustInTimeConfiguredConsumerFactory<K, V> extends DefaultKafkaConsumerFactory<K, V>
    implements EnvironmentAware {

    private static final Logger LOG = LoggerFactory.getLogger(JustInTimeConfiguredConsumerFactory.class);

    /**
     * Deserializer for Kafka keys
     */
    private final Deserializer<K> keyDeserializer;

    /**
     * Deserializer for Kafka values
     */
    private final Deserializer<V> valueDeserializer;

    private final FactoryState state;

    public JustInTimeConfiguredConsumerFactory(Map<String, Object> configs, Deserializer<K> keyDeserializer, Deserializer<V> valueDeserializer) {
        super(configs, keyDeserializer, valueDeserializer);
        this.keyDeserializer = keyDeserializer;
        this.valueDeserializer = valueDeserializer;
        this.state = new FactoryState();

        state.sources.addFirst(new MapPropertySource("consumer-construction-config", KafkaPropertyUtils.mapNullValues(configs)));
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
    protected KafkaConsumer<K, V> createKafkaConsumer() {
        LOG.debug("Creating new Kafka Consumer ...");
        return new KafkaConsumer<>(state.getConfigurationProperties(), this.keyDeserializer, this.valueDeserializer);
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
