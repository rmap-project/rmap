package info.rmapproject.kafka.shared;

import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;

/**
 * A Kafka serializer which uses a Java {@link ObjectOutputStream} to serialize a Java object to a byte stream.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class GenericJvmObjectSerializer<T> implements Serializer<T> {

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
     * Implementation notes: this method uses the Java {@link ObjectOutputStream} to produce a byte stream by
     * serializing the supplied Java object {@code data}.
     * </p>
     * @param topic {@inheritDoc}
     * @param data {@inheritDoc}
     * @return {@inheritDoc}
     * @throws RuntimeException if the Java object cannot be serialized
     */
    @Override
    public byte[] serialize(String topic, T data) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(data);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error serializing an instance of " + data.getClass().getName() + ": " +
                    e.getMessage(), e);
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
