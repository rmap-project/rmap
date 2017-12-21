package info.rmapproject.indexing.kafka;

import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.rmapservice.RMapService;
import info.rmapproject.indexing.IndexUtils;
import info.rmapproject.indexing.IndexingInterruptedException;
import info.rmapproject.indexing.IndexingTimeoutException;
import info.rmapproject.indexing.solr.model.DiscoSolrDocument;
import info.rmapproject.indexing.solr.model.KafkaMetadata;
import info.rmapproject.indexing.solr.repository.EventTupleIndexingRepository;
import info.rmapproject.indexing.solr.repository.IndexDTOMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.InterruptException;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static info.rmapproject.indexing.IndexUtils.ise;
import static info.rmapproject.indexing.kafka.KafkaUtils.commitOffsets;
import static java.util.Collections.singleton;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class IndexingConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(IndexingConsumer.class);

    private static final ConsumerRecords<String, RMapEvent> EMPTY_RECORDS =
            new ConsumerRecords<>(Collections.emptyMap());

    @Autowired
    private RMapService rmapService;

    private EventTupleIndexingRepository<DiscoSolrDocument> indexer;

    private Consumer<String, RMapEvent> consumer;

    private int pollTimeoutMs;

    private Thread shutdownHook;

    private ConsumerAwareRebalanceListener<String, RMapEvent> rebalanceListener;

    private IndexingRetryHandler retryHandler;

    private OffsetLookup offsetLookup;

    private IndexDTOMapper dtoMapper;

    void consumeLatest(String topic) throws UnknownOffsetException {
        consume(topic, Seek.LATEST);
    }

    void consumeEarliest(String topic) throws UnknownOffsetException {
        consume(topic, Seek.EARLIEST);
    }

    void consume(String topic, Seek seek) throws UnknownOffsetException {
        IndexUtils.assertNotNull(consumer, ise("Consumer must not be null."));
        IndexUtils.assertNotNullOrEmpty(topic, "Topic must not be null or empty");
        IndexUtils.assertNotNull(seek, ise("Seek must not be null."));

        rebalanceListener.setConsumer(consumer);
        rebalanceListener.setSeekBehavior(seek);

        consumer.subscribe(singleton(topic), rebalanceListener);
        consumer.poll(0); // join consumer group, get partitions, seek to correct offset

        Map<TopicPartition, OffsetAndMetadata> offsetsToCommit = new HashMap<>(1);

        while (true) {

            offsetsToCommit.clear();
            ConsumerRecords<String, RMapEvent> records = null;

            try {
                LOG.trace("Entering poll for {} ms", pollTimeoutMs);
                records = consumer.poll(pollTimeoutMs);
            } catch (WakeupException e) {
                LOG.info("WakeupException encountered, closing consumer.");
                consumer.close();
                break;
            } catch (InterruptException e) {
                LOG.info("InterruptException encountered, exiting consumer.poll({}) early.", pollTimeoutMs);
                Thread.interrupted();
                // guard against null records
                records = EMPTY_RECORDS;
            }

            LOG.trace("Processing {} records", records.count());
            records.forEach(record -> {
                RMapEvent event = record.value();

                if (event.getEventTargetType() != null &&
                        !event.getEventTargetType().equals(RMapEventTargetType.DISCO)) {
                    LOG.debug("Skipping event {} because it does not target a DISCO (was {} instead)",
                            event, event.getEventTargetType());
                    return;
                }

                String recordTopic = record.topic();
                long recordOffset = record.offset();
                int recordPartition = record.partition();

                LOG.trace("Processing record {}/{}/{} for event: {}", recordTopic, recordPartition, recordOffset, event);

                try {
                    indexEvent(recordTopic, recordPartition, recordOffset, event);
                    offsetsToCommit.put(new TopicPartition(recordTopic, recordPartition),
                            new OffsetAndMetadata(recordOffset));
                } catch (Exception e) {
                    LOG.warn("Unable to index event {}: {}", event, e.getMessage(), e);
                }
            });

            if (!offsetsToCommit.isEmpty()) {
                LOG.trace("Committing offset(s) for {} TopicPartition(s): {}", offsetsToCommit.size(),
                        KafkaUtils.offsetsAsString(offsetsToCommit));
                commitOffsets(consumer, offsetsToCommit, true);
            }

        }
    }

    private void indexEvent(String recordTopic, int recordPartition, long recordOffset, RMapEvent event) throws IndexingTimeoutException, IndexingInterruptedException {
        KafkaMetadata md = new KafkaMetadata() {
            @Override
            public long getKafkaOffset() {
                return recordOffset;
            }

            @Override
            public int getKafkaPartition() {
                return recordPartition;
            }

            @Override
            public String getKafkaTopic() {
                return recordTopic;
            }
        };

        retryHandler.retry(event, md, (doc) -> {
            doc.setKafkaOffset(recordOffset);
            doc.setKafkaPartition(recordPartition);
            doc.setKafkaTopic(recordTopic);
        });
    }


    public RMapService getRmapService() {
        return rmapService;
    }

    public void setRmapService(RMapService rmapService) {
        this.rmapService = rmapService;
    }

    public EventTupleIndexingRepository<DiscoSolrDocument> getIndexer() {
        return indexer;
    }

    public void setIndexer(EventTupleIndexingRepository<DiscoSolrDocument> indexer) {
        this.indexer = indexer;
    }

    public IndexDTOMapper getDtoMapper() {
        return dtoMapper;
    }

    public void setDtoMapper(IndexDTOMapper dtoMapper) {
        this.dtoMapper = dtoMapper;
    }

    public Consumer<String, RMapEvent> getConsumer() {
        return consumer;
    }

    public void setConsumer(Consumer<String, RMapEvent> consumer) {
        this.consumer = consumer;
    }

    public int getPollTimeoutMs() {
        return pollTimeoutMs;
    }

    public void setPollTimeoutMs(int pollTimeoutMs) {
        this.pollTimeoutMs = pollTimeoutMs;
    }

    public Thread getShutdownHook() {
        return shutdownHook;
    }

    public void setShutdownHook(Thread shutdownHook) {
        this.shutdownHook = shutdownHook;
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    public ConsumerRebalanceListener getRebalanceListener() {
        return rebalanceListener;
    }

    public void setRebalanceListener(ConsumerAwareRebalanceListener<String, RMapEvent> rebalanceListener) {
        this.rebalanceListener = rebalanceListener;
    }

    public IndexingRetryHandler getRetryHandler() {
        return retryHandler;
    }

    public void setRetryHandler(IndexingRetryHandler retryHandler) {
        this.retryHandler = retryHandler;
    }

    public OffsetLookup getOffsetLookup() {
        return offsetLookup;
    }

    public void setOffsetLookup(OffsetLookup offsetLookup) {
        this.offsetLookup = offsetLookup;
    }
}
