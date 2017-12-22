package info.rmapproject.indexing.kafka;

import static info.rmapproject.indexing.IndexUtils.EventDirection.TARGET;
import static info.rmapproject.indexing.kafka.ConsumerTestUtil.assertExceptionHolderEmpty;
import static info.rmapproject.indexing.solr.TestUtils.getRmapObjects;
import static info.rmapproject.indexing.solr.TestUtils.getRmapResources;
import static java.util.Comparator.comparing;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;

import info.rmapproject.core.model.RMapObjectType;
import info.rmapproject.core.model.agent.RMapAgent;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.model.request.RequestEventDetails;
import info.rmapproject.core.rmapservice.RMapService;
import info.rmapproject.core.rmapservice.impl.openrdf.triplestore.SesameTriplestore;
import info.rmapproject.indexing.solr.AbstractSpringIndexingTest;
import info.rmapproject.indexing.solr.TestUtils;
import info.rmapproject.indexing.solr.model.DiscoSolrDocument;
import info.rmapproject.indexing.solr.repository.DiscoRepository;
import info.rmapproject.kafka.shared.SpringKafkaConsumerFactory;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class SaveOffsetOnRebalanceIT extends AbstractSpringIndexingTest {

    @Autowired
    private IndexingConsumer indexer;

    @Autowired
    private KafkaTemplate<String, RMapEvent> producer;

    @Autowired
    private DiscoRepository discoRepository;

    @Autowired
    private RMapService rMapService;

    @Autowired
    private SesameTriplestore triplestore;

    @Value("${rmapcore.producer.topic}")
    private String topic;

    /**
     * Note: this is *not* the same Consumer instance referenced by the {@link #indexer IndexingConsumer}.  Be sure to
     * use {@link IndexingConsumer#getConsumer()} if you want that instance.
     */
    @Autowired
    private Consumer consumer;

    /**
     * The number of documents in solr at the beginning of each test
     */
    private long existingDocumentCount;

    /**
     * Request details used by this test when creating Agents in RMap
     */
    private RequestEventDetails systemAgentEventDetails;

    /**
     * Keeps track of whether or not the triplestore has been populate with test content by {@link #setUp()}.
     */
    private boolean triplestoreInitialized = false;

    /**
     * A list of test events ordered chronologically, with the oldest event at the head of the list.  The events in
     * this list, and their source and target objects, are present in the triplestore if the
     * {@link #triplestoreInitialized} flag is {@code true}.
     */
    private List<RMapEvent> events;

    /**
     * Does a number of things:
     * <ol>
     *     <li>Records the number of documents in the solr index at the beginning of each test</li>
     *     <li>Initializes the triplestore with test content if the {@link #triplestoreInitialized} flag is {@code false}</li>
     * </ol>
     * Initializing the triplestore entails:
     * <ol>
     *     <li>{@link ConsumerTestUtil#createSystemAgent(RMapService) Creating} a system {@code RMapAgent}</li>
     *     <li>Using the system agent to create additional {@code RMapAgent}s found in test content</li>
     *     <li>Streaming the triples found in test content directly to the triplestore</li>
     * </ol>
     * This insures that the RMap domain objects used by this integration test have representations in the triplestore.
     * Using the {@link #triplestoreInitialized} flag allows the expensive operations surrounding triplestore
     * initialization to happen only once for this test class.
     */
    @Override
    @Before
    public void setUp() throws Exception {
        existingDocumentCount = discoRepository.count();

        if (!triplestoreInitialized) {
            RMapAgent systemAgent = ConsumerTestUtil.createSystemAgent(rMapService);
            systemAgentEventDetails = new RequestEventDetails(systemAgent.getId().getIri());

            // Get some Rmap objects from the filesystem, and put them in the triplestore
            Map<RMapObjectType, Set<TestUtils.RDFResource>> rmapObjects = new HashMap<>();
            getRmapResources("/data/discos/rmd18mddcw", rdfHandler, RDFFormat.NQUADS, rmapObjects);
            assertFalse(rmapObjects.isEmpty());

            // Create necessary agents
            createAgents(rmapObjects);

            addTriplesFromResource("http://foo/bar", triplestore, rmapObjects);
            triplestoreInitialized = true;

            events = getRmapObjects(rmapObjects, RMapObjectType.EVENT, rdfHandler, comparing(RMapEvent::getStartTime));
        }
    }

    /**
     * Arguably a consumer test.  Insures that the rebalancer methods are invoked when a consumer joins.
     *
     * @throws InterruptedException if concurrent operations are interrupted
     */
    @Test
    @SuppressWarnings("unchecked")
    @Ignore("TODO: pending using two partitions in the test broker")
    public void testPartitionsRevokedAndAssignedInvokedOnConsumerJoin() throws InterruptedException {
        CountDownLatch initialLatch2 = new CountDownLatch(2);
        CountDownLatch initialLatch4 = new CountDownLatch(4);

        indexer.setRebalanceListener(new ConsumerAwareRebalanceListener<String, RMapEvent>() {
            @Override
            public void setConsumer(Consumer<String, RMapEvent> consumer) {
                // no-op
            }

            @Override
            public void setSeekBehavior(Seek seekBehavior) {
                // no-op
            }

            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                LOG.debug("Initial Consumer: Partitions Revoked {}", KafkaUtils.topicPartitionsAsString(partitions));
                initialLatch2.countDown();
                initialLatch4.countDown();
            }

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                LOG.debug("Initial Consumer: Partitions Assigned {}", KafkaUtils.topicPartitionsAsString(partitions));
                initialLatch2.countDown();
                initialLatch4.countDown();
            }
        });

        AtomicReference<Exception> exceptionHolder = new AtomicReference<>();
        Thread t = new Thread(ConsumerTestUtil.newConsumerRunnable(indexer, topic, exceptionHolder), "testPartitionsRevokedAndAssignedOnConsumerJoin-consumer");

        t.start();

        // rebalancer should be called when the consumer starts.
        assertTrue(initialLatch2.await(60000, TimeUnit.MILLISECONDS));

        CountDownLatch secondaryLatch2 = new CountDownLatch(2);
        @SuppressWarnings("rawtypes")
        Consumer secondaryConsumer = SpringKafkaConsumerFactory.newConsumer("-02");
        secondaryConsumer.subscribe(Collections.singleton(topic), new ConsumerAwareRebalanceListener<String, RMapEvent>() {
            @Override
            public void setConsumer(Consumer<String, RMapEvent> consumer) {
                // no-op
            }

            @Override
            public void setSeekBehavior(Seek seekBehavior) {
                // no-op
            }

            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                LOG.debug("Secondary Consumer: Partitions Revoked {}", KafkaUtils.topicPartitionsAsString(partitions));
                secondaryLatch2.countDown();
            }

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                LOG.debug("Secondary Consumer: Partitions Assigned {}", KafkaUtils.topicPartitionsAsString(partitions));
                secondaryLatch2.countDown();
            }
        });

        // Fire up a second consumer, and invoke poll so it gets its partitions assigned.  the rebalancer should be
        // invoked for both consumers
        secondaryConsumer.poll(0);
        t.interrupt();  // short-circuit any polling in the initial consumer, speed things up.

        // the initial rebalancer should have its methods invoked a total of four times
        assertTrue(initialLatch4.await(60000, TimeUnit.MILLISECONDS));

        // the second rebalancer should have its methods invoked a total of two times
        assertTrue(secondaryLatch2.await(60000, TimeUnit.MILLISECONDS));

        // cleanup.  wakeup causes the initial indexer close its consumer and exit
        LOG.debug("Waking up initial consumer.");
        indexer.getConsumer().wakeup();
        LOG.debug("Thread joining.");
        t.join();
        LOG.debug("Closing secondary consumer.");
        secondaryConsumer.close();
        assertExceptionHolderEmpty(exceptionHolder);
    }

    /**
     * Arguably a consumer test.  Insures that the rebalancer methods are invoked when a consumer joins.
     *
     * @throws InterruptedException if concurrent operations are interrupted
     */
    @Test
    @SuppressWarnings({"unchecked", "rawtypes", "serial"})
    @Ignore("TODO: pending use of two partitions in test broker")
    public void testPartitionsRevokedAndAssignedInvokedOnStart() throws InterruptedException {
        ConsumerAwareRebalanceListener underTest = mock(ConsumerAwareRebalanceListener.class);
        indexer.setRebalanceListener(underTest);

        AtomicReference<Exception> exceptionHolder = new AtomicReference<>();
        Thread t = new Thread(ConsumerTestUtil.newConsumerRunnable(indexer, topic, exceptionHolder), "testPartitionsRevokedAndAssignedOnStart-consumer");
        t.start();

        // allow thread to run a bit
        Thread.sleep(5000);

        LOG.debug("Waking up consumer.");
        indexer.getConsumer().wakeup();

        LOG.debug("Thread joining.");
        t.join();

        ConsumerTestUtil.assertExceptionHolderEmpty(exceptionHolder);

        verify(underTest).onPartitionsRevoked(Collections.emptySet());
        verify(underTest).onPartitionsAssigned(new HashSet() {
            {
                add(new TopicPartition(topic, 0));
                add(new TopicPartition(topic, 1));
            }
        });


    }

    @Test
    public void testRebalance() throws Exception {
        String expectedDiscoUri = "rmap:rmd18mddcw";
        String expectedEventUri = "rmap:rmd18mdddd";
        String expectedAgentUri = "rmap:rmd18m7mj4";
        String expectedLineageUri = "rmap:rmd18m7mr7";

        final Set<DiscoSolrDocument> existingInactiveDocuments = discoRepository
                .findDiscoSolrDocumentsByEventLineageProgenitorUriAndDiscoStatus(expectedLineageUri, "INACTIVE");
        final Set<DiscoSolrDocument> existingActiveDocuments = discoRepository
                .findDiscoSolrDocumentsByEventLineageProgenitorUriAndDiscoStatus(expectedLineageUri, "ACTIVE");

        // Produce some events
        LOG.debug("Producing events.");
        events.forEach(event -> {
                    assertEquals("Unexpected lineage progenitor URI", expectedLineageUri, event.getLineageProgenitor().getStringValue());
                    producer.send(topic, event);
                }
        );
        producer.flush();

        // Boot up the indexer in its own thread, and consume some events.
        LOG.debug("Starting indexer.");
        AtomicReference<Exception> exceptionHolder = new AtomicReference<>();
        Thread initialIndexerThread = new Thread(ConsumerTestUtil.newConsumerRunnable(indexer, topic, exceptionHolder), "Initial Indexer");
        initialIndexerThread.start();

        // sleep to let the indexer do its job
        Thread.sleep(30000);

        // clean up indexer thread
        indexer.getConsumer().wakeup();
        initialIndexerThread.join();

        assertExceptionHolderEmpty("Consumer threw an unexpected exception.", exceptionHolder);

        final Set<DiscoSolrDocument> inactiveDocuments = discoRepository
                .findDiscoSolrDocumentsByEventLineageProgenitorUriAndDiscoStatus(expectedLineageUri, "INACTIVE")
                .stream()
                .filter(doc -> !existingInactiveDocuments.contains(doc))
                .collect(Collectors.toSet());

        final Set<DiscoSolrDocument> activeDocuments = discoRepository
                .findDiscoSolrDocumentsByEventLineageProgenitorUriAndDiscoStatus(expectedLineageUri, "ACTIVE")
                .stream()
                .filter(doc -> !existingActiveDocuments.contains(doc))
                .collect(Collectors.toSet());

        // We added 4 inactive documents, and 1 active document
        assertEquals(4, inactiveDocuments.size());
        assertEquals(1, activeDocuments.size());

        final DiscoSolrDocument activeDocument = activeDocuments.iterator().next();
        assertEquals(expectedDiscoUri, activeDocument.getDiscoUri());
        assertEquals(TARGET.name(), activeDocument.getDiscoEventDirection());
        assertEquals(expectedDiscoUri, activeDocument.getEventTargetObjectUris().get(0));
        assertEquals(expectedLineageUri, activeDocument.getEventLineageProgenitorUri());

        assertEquals(expectedEventUri, activeDocument.getEventUri());
        assertEquals(expectedAgentUri, activeDocument.getAgentUri());
        assertTrue(activeDocument.getKafkaOffset() > -1);
        assertTrue(activeDocument.getKafkaPartition() > -1);
        assertNotNull(activeDocument.getKafkaTopic());

        inactiveDocuments.forEach(inactiveDoc -> {
            assertNotEquals(expectedDiscoUri, inactiveDoc.getDiscoUri());
            assertEquals("INACTIVE", inactiveDoc.getDiscoStatus());
            assertEquals(expectedLineageUri, inactiveDoc.getEventLineageProgenitorUri());
        });

    }

    /**
     * Insures that a consumer can seek to the (last-written-offset + 1) of a Kafka partition without throwing an
     * exception.  Insures that polling after seeking to (last-written-offset + 1) does not re-read any events, and
     * does not throw any exceptions.
     */
    @Test
    public void testSeekOffsetPlusOne() throws InterruptedException, ExecutionException, TimeoutException {

        // insure that consumer.poll() does not re-read the last event in the partition when a rebalance occurs
        // (i.e. the rebalancer seeks to <last-saved-offset> + 1) and that no exceptions are thrown by seeking
        // to <last-saved-offset> + 1.

        RMapEvent event = mock(RMapEvent.class, withSettings().extraInterfaces(Serializable.class));
        String testTopic = SaveOffsetOnRebalance.class.getSimpleName() + "-testSeekOffsetPlusOne";
        RecordMetadata record = producer.send(testTopic, event)
                .get(5000, TimeUnit.MILLISECONDS)
                .getRecordMetadata();

        // Wrote the record to offset 0
        assertEquals(0, record.offset());
        producer.flush();

        // Assert that we are able to seek to the end of the partition, plus 1 without throwing an exception.
        TopicPartition tp = new TopicPartition(testTopic, record.partition());
        consumer.assign(Collections.singleton(tp));
        consumer.seek(tp, record.offset() + 1);
        assertEquals(1, consumer.position(tp));

        // Assert that seeking to the beginning is offset 0
        consumer.seekToBeginning(Collections.singleton(tp));
        assertEquals(0, consumer.position(tp));

        // Assert that seeking to the end is the last written offset plus 1
        consumer.seekToEnd(Collections.singleton(tp));
        assertEquals(record.offset() + 1, consumer.position(tp));

        // Assert that poll() does not re-read an event or throw unwanted exceptions
        assertTrue(consumer.poll(1000L).isEmpty());
    }

    /**
     * Streams the triples found in each {@code RDFResource} and deposits them into the triplestore.
     *
     * @param baseURI
     * @param triplestore
     * @param rmapObjects
     */
    private static void addTriplesFromResource(String baseURI, SesameTriplestore triplestore, Map<RMapObjectType, Set<TestUtils.RDFResource>> rmapObjects) {
        rmapObjects.values().stream().flatMap(Set::stream)
                .forEach(source -> {
                    try (InputStream in = source.getInputStream();
                         RepositoryConnection c = triplestore.getConnection();
                    ) {
                        assertTrue(c.isOpen());
                        c.add(in, baseURI, source.getRdfFormat());
                    } catch (IOException e) {
                        e.printStackTrace(System.err);
                        fail("Unexpected IOException");
                    }
                });
    }

    /**
     * Creates additional agents found in {@code rmapObjects} using the request details of the
     * {@link #systemAgentEventDetails system agent}.
     *
     * @param rmapObjects map of test objects that may contain agents
     */
    private void createAgents(Map<RMapObjectType, Set<TestUtils.RDFResource>> rmapObjects) {
        List<RMapAgent> agents = getRmapObjects(rmapObjects, RMapObjectType.AGENT, rdfHandler);
        assertNotNull(agents);
        assertTrue(agents.size() > 0);
        LOG.debug("Creating {} agents", agents.size());
        agents.forEach(agent -> {
            try {
                rMapService.createAgent(agent, systemAgentEventDetails);
            } catch (Exception e) {
                LOG.debug("Error creating agent {}: {}", agent.getId().getStringValue(), e.getMessage(), e);
            }
        });
    }

    /**
     * Removes all documents from the index that reference the supplied {@code lineageUri}.
     *
     * @param lineageUri the lineage uri
     */
    private void deleteLineageFromIndex(String lineageUri) {
        // Remove documents from the index that may be created by this test invocation
        Set<DiscoSolrDocument> existingDocs = discoRepository.findDiscoSolrDocumentsByEventLineageProgenitorUri(lineageUri);
        if (!existingDocs.isEmpty()) {
            discoRepository.deleteAll(existingDocs);
        }
        assertEquals(0, discoRepository.findDiscoSolrDocumentsByEventLineageProgenitorUri(lineageUri).size());
    }
}