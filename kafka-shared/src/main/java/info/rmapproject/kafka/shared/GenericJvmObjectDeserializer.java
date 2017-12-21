package info.rmapproject.kafka.shared;

import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;

/**
 * A Kafka deserializer which uses a Java {@link ObjectInputStream} to deserialize a byte stream to a Java object.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class GenericJvmObjectDeserializer<T> implements Deserializer<T> {

    private static final Logger LOG = LoggerFactory.getLogger(GenericJvmObjectDeserializer.class);

    /**
     * {@inheritDoc}
     * <p>
     * Implementation notes: this is a no-op, as this implementation does not require any configuration
     * </p>
     * @param configs {@inheritDoc}
     * @param isKey {@inheritDoc}
     */
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // no-op
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation notes: this method uses the Java {@link ObjectInputStream} to consume the supplied {@code bytes}
     * and deserialize them to a Java object.
     * </p>
     * @param topic {@inheritDoc}
     * @param bytes {@inheritDoc}
     * @return {@inheritDoc}
     * @throws RuntimeException if the byte stream cannot be deserialized
     */
    @Override
    @SuppressWarnings("unchecked")
    public T deserialize(String topic, byte[] bytes) {

        if (bytes == null || bytes.length == 0) {
            LOG.debug("Supplied bytes for topic [{}] were [{}], returning null",
                    (bytes == null) ? "null" : "zero length");
            return null;
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
                return (T) ois.readObject();
        } catch (ClassNotFoundException|IOException e) {
            throw new RuntimeException("Error deserializing a byte stream to a Java object: " + e.getMessage(), e);
        }

    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation notes: this is a no-op, as this implementation does not manage any resources.
     * </p>
     */
    @Override
    public void close() {
        // no-op
    }

}
