package info.rmapproject.indexing.kafka;

import static info.rmapproject.indexing.IndexUtils.EventDirection.TARGET;
import static info.rmapproject.indexing.TestUtils.getRmapObjects;
import static info.rmapproject.indexing.TestUtils.populateTriplestore;
import static info.rmapproject.indexing.kafka.ConsumerTestUtil.assertExceptionHolderEmpty;
import static info.rmapproject.indexing.kafka.ConsumerTestUtil.newConsumerRunnable;
import static java.util.Comparator.comparing;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;

import info.rmapproject.indexing.TestUtils.RDFResource;
import info.rmapproject.core.model.RMapObjectType;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.rmapservice.RMapService;
import info.rmapproject.core.rmapservice.impl.rdf4j.triplestore.Rdf4jTriplestore;

import info.rmapproject.indexing.solr.model.DiscoSolrDocument;
import info.rmapproject.indexing.solr.repository.DiscoRepository;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class IndexingConsumerIT extends BaseKafkaIT {

    @Autowired
    private IndexingConsumer indexer;

    @Autowired
    private KafkaTemplate<String, RMapEvent> producer;

    @Autowired
    private DiscoRepository discoRepository;

    @Autowired
    private RMapService rMapService;

    @Autowired
    private Rdf4jTriplestore triplestore;

    @Value("${rmapcore.producer.topic}")
    private String topic;

    /**
     * Simple IT that consumes 5 events, indexes them, and makes a few assertions about the resulting documents.
     *
     * @throws Exception if the triplestore cannot be populated
     */
    @Test
    public void simpleIT() throws Exception {
        // Clear out the index
        discoRepository.deleteAll();
        assertEquals(0, discoRepository.count());

        // Get some RMap objects from the filesystem, and put them in the triplestore
        Map<RMapObjectType, Set<RDFResource>> rmapObjects =
                populateTriplestore(triplestore, rdfHandler, rMapService, "/data/discos/rmd18mddcw");

        // Produce some events, so they're waiting for the consumer when it starts.
        List<RMapEvent> events = getRmapObjects(
                rmapObjects, RMapObjectType.EVENT, rdfHandler, comparing(RMapEvent::getStartTime));
        LOG.debug("Sending {} RMap events to Kafka", events.size());
        events.forEach(event -> producer.send(topic, event));
        producer.flush();

        // Boot up the indexer, and consume the events
        AtomicReference<Exception> exceptionHolder = new AtomicReference<>();
        // Boot up the first indexing consumer, and consume some events.  Any exceptions thrown by the thread are
        // caught in the `exceptionHolder`
        Thread initialIndexerThread = new Thread(
                newConsumerRunnable(indexer, topic, exceptionHolder), "Initial Indexer");
        LOG.debug("Consuming RMap events from Kafka, and indexing them from thread '{}'", initialIndexerThread.getName());
        initialIndexerThread.start();

        int expectedDocumentCount = 5;
        Condition<Long> docCountCondition = new Condition<>(() -> discoRepository.count(),
                "DiscoRepository contains " + expectedDocumentCount + " documents");
        LOG.debug("Verifying index contains {} new documents", expectedDocumentCount);
        assertTrue(docCountCondition.verify((count) -> count == expectedDocumentCount));

        // clean up
        indexer.getConsumer().wakeup();
        initialIndexerThread.join();

        assertExceptionHolderEmpty("Consumer threw an unexpected exception.", exceptionHolder);

        final Set<DiscoSolrDocument> inactive = discoRepository.findDiscoSolrDocumentsByDiscoStatus("INACTIVE");
        final Set<DiscoSolrDocument> active = discoRepository.findDiscoSolrDocumentsByDiscoStatus("ACTIVE");
        assertEquals(expectedDocumentCount, inactive.size() + active.size());
        assertEquals(4, inactive.size());
        assertEquals(1, active.size());

        final DiscoSolrDocument activeDocument = active.iterator().next();
        assertEquals("rmap:rmd18mddcw", activeDocument.getDiscoUri());
        assertEquals(TARGET.name(), activeDocument.getDiscoEventDirection());
        assertEquals("rmap:rmd18mddcw", activeDocument.getEventTargetObjectUris().get(0));
        assertEquals("rmap:rmd18mdddd", activeDocument.getEventUri());
        assertEquals("rmap:rmd18m7mj4", activeDocument.getAgentUri());
        assertTrue(activeDocument.getKafkaOffset() > -1);
        assertTrue(activeDocument.getKafkaPartition() > -1);
        assertNotNull(activeDocument.getKafkaTopic());
    }

}
