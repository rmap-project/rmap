package info.rmapproject.kafka.shared;

import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
@Configuration
public class SpringKafkaProducerConfiguration {

    @Bean
    public <T> ProducerFactory producerFactory() {
        return new JustInTimeConfiguredProducerFactory(producerConfig(), stringSerializer(), jvmSerializer());
    }

    @Bean
    public Serializer<String> stringSerializer() {
        return new StringSerializer();
    }

    @Bean
    public <T> GenericJvmObjectSerializer<T> jvmSerializer() {
        return new GenericJvmObjectSerializer<>();
    }

    @Bean
    public Map<String, Object> producerConfig() {
        return new HashMap<String, Object>() {{
            put("client.id", "rmap-event-producer");
            put("enable.idempotence", "true");
            put("bootstrap.servers", System.getProperty("spring.embedded.kafka.brokers"));
        }};
    }

}
