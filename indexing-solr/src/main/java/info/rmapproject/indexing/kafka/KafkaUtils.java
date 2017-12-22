package info.rmapproject.indexing.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.OffsetCommitCallback;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

class KafkaUtils {

    private static final Logger LOG = LoggerFactory.getLogger(IndexingConsumer.class);

    static void commitOffsets(Consumer<?, ?> consumer, Map<TopicPartition, OffsetAndMetadata> offsetsToCommit,
                              boolean async) {

        if (offsetsToCommit == null || offsetsToCommit.isEmpty()) {
            return;
        }

        OffsetCommitCallback callback = (offsets, exception) -> {
            if (exception != null) {
                LOG.warn("Unable to commit offsets for {} TopicPartition(s) {}: {}",
                        offsets.size(),
                        offsetsAsString(offsets),
                        exception.getMessage(),
                        exception);
            } else {
                LOG.debug("Successfully committed offset(s) for {} TopicPartition(s): {}",
                        offsets.size(), offsetsAsString(offsets));
            }
        };

        if (async) {
            consumer.commitAsync(offsetsToCommit, callback);
        } else {
            consumer.commitSync(offsetsToCommit);
        }
    }

    static String offsetsAsString(Map<TopicPartition, OffsetAndMetadata> offsets) {
        return offsets
                .entrySet()
                .stream()
                .map((entry) -> entry.getKey().toString() + ": " + String.valueOf(entry.getValue().offset()))
                .collect(Collectors.joining(", "));
    }

    static String topicPartitionsAsString(Collection<TopicPartition> tps) {
        return tps
                .stream()
                .map(TopicPartition::toString)
                .collect(Collectors.joining(","));
    }
}
