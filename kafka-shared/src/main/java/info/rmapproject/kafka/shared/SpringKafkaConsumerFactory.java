package info.rmapproject.kafka.shared;

import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.kafka.core.ConsumerFactory;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class SpringKafkaConsumerFactory {

    private static ConsumerFactory factory;

    public static ConsumerFactory getFactory() {
        return factory;
    }

    public static void setFactory(ConsumerFactory factory) {
        SpringKafkaConsumerFactory.factory = factory;
    }

    public static Consumer newConsumer() {
        return factory.createConsumer();
    }

    public static Consumer newConsumer(String clientIdSuffix) {
        return factory.createConsumer(clientIdSuffix);
    }
}
