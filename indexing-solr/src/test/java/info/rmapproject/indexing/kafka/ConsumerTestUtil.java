package info.rmapproject.indexing.kafka;

import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;

public class ConsumerTestUtil {

    /**
     * Asserts the supplied {@code exceptionHolder} is empty, otherwise {@link Assert#fail(String) fail} the test,
     * emitting the stacktrace in the failure message.
     *
     * @param exceptionHolder an {@code AtomicReference} which may hold an {@code Exception}
     */
    static void assertExceptionHolderEmpty(AtomicReference<Exception> exceptionHolder) {
        assertExceptionHolderEmpty("Encountered an unexpected exception", exceptionHolder);
    }

    /**
     * Asserts the supplied {@code exceptionHolder} is empty, otherwise {@link Assert#fail(String) fail} the test,
     * emitting the stacktrace in the failure message.
     *
     * @param message optional message to emit with a failed assertion
     * @param exceptionHolder an {@code AtomicReference} which may hold an {@code Exception}
     */
    static void assertExceptionHolderEmpty(String message, AtomicReference<Exception> exceptionHolder) {
        if (exceptionHolder.get() == null) {
            return;
        }

        ByteArrayOutputStream trace = new ByteArrayOutputStream();
        exceptionHolder.get().printStackTrace(new PrintStream(trace, true));
        if (message == null || message.trim().length() == 0) {
            fail("Consumer threw an unexpected exception: \n" + trace);
        } else {
            fail(message + "(Stacktrace: \n" + trace + "\n)");
        }
    }

    /**
     * Returns a {@link Runnable} that starts an indexing process upon invoking {@link Runnable#run()}.
     * <p>
     * The {@code Runnable} starts the supplied {@code indexer} by {@link IndexingConsumer#consumeEarliest(String)
     * consuming from the earliest offset} in the {@code topic}.  Any exceptions thrown by the {@code indexer} are
     * caught and stored in the supplied {@code exceptionHolder}.
     *
     * @param indexer the indexer start consuming RMap events from Kafka
     * @param topic the Kafka topic to consume from
     * @param exceptionHolder catches any exceptions thrown by the indexer
     * @return a {@code Runnable} that will start consuming RMap events when started
     */
    static Runnable newConsumerRunnable(IndexingConsumer indexer, String topic,
                                        AtomicReference<Exception> exceptionHolder) {
        return () -> {
            try {
                indexer.consumeEarliest(topic);
            } catch (Exception e) {
                exceptionHolder.set(e);
            }
        };
    }

    /**
     * Returns a {@link Runnable} that starts an indexing process upon invoking {@link Runnable#run()}.
     * <p>
     * The {@code Runnable} starts the supplied {@code indexer} by {@link IndexingConsumer#consume(String, Seek)
     * consuming} from the {@code topic}, starting from the beginning or end as directed by {@code seekBehavior}.  Any
     * exceptions thrown by the {@code indexer} are caught and stored in the supplied {@code exceptionHolder}.
     *
     * @param indexer the indexer start consuming RMap events from Kafka
     * @param topic the Kafka topic to consume from
     * @param seekBehavior whether to seek to the beginning or end of the Kafka topic
     * @param exceptionHolder catches any exceptions thrown by the indexer
     * @return a {@code Runnable} that will start consuming RMap events when started
     */
    static Runnable newConsumerRunnable(IndexingConsumer indexer, String topic, Seek seekBehavior,
                                        AtomicReference<Exception> exceptionHolder) {
        return () -> {
            try {
                indexer.consume(topic, seekBehavior);
            } catch (Exception e) {
                exceptionHolder.set(e);
            }
        };
    }
}
