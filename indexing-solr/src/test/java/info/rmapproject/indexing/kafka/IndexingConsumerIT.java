package info.rmapproject.indexing.kafka;

import static info.rmapproject.indexing.IndexUtils.EventDirection.TARGET;
import static info.rmapproject.indexing.kafka.ConsumerTestUtil.assertExceptionHolderEmpty;
import static info.rmapproject.indexing.kafka.ConsumerTestUtil.createSystemAgent;
import static info.rmapproject.indexing.kafka.ConsumerTestUtil.dumpTriplestore;
import static info.rmapproject.indexing.kafka.ConsumerTestUtil.newConsumerRunnable;
import static info.rmapproject.indexing.solr.TestUtils.getRmapObjects;
import static info.rmapproject.indexing.solr.TestUtils.getRmapResources;
import static java.util.Comparator.comparing;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import info.rmapproject.core.model.RMapObjectType;
import info.rmapproject.core.model.agent.RMapAgent;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.model.request.RequestEventDetails;
import info.rmapproject.core.rdfhandler.RDFHandler;
import info.rmapproject.core.rmapservice.RMapService;
import info.rmapproject.core.rmapservice.impl.openrdf.triplestore.SesameTriplestore;
import info.rmapproject.indexing.solr.AbstractSpringIndexingTest;
import info.rmapproject.indexing.solr.TestUtils;
import info.rmapproject.indexing.solr.model.DiscoSolrDocument;
import info.rmapproject.indexing.solr.repository.DiscoRepository;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration("classpath:/spring-rmapauth-context.xml")
public class IndexingConsumerIT extends AbstractSpringIndexingTest {

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
        Map<RMapObjectType, Set<TestUtils.RDFResource>> rmapObjects =
                populateTriplestore(triplestore, rdfHandler, rMapService, "/data/discos/rmd18mddcw");

        // Produce some events, so they're waiting for the consumer when it starts.
        LOG.debug("Producing events.");
        List<RMapEvent> events = getRmapObjects(
                rmapObjects, RMapObjectType.EVENT, rdfHandler, comparing(RMapEvent::getStartTime));
        events.forEach(event -> producer.send(topic, event));
        producer.flush();

        // Boot up the indexer, and consume the events
        LOG.debug("Starting indexer.");
        AtomicReference<Exception> exceptionHolder = new AtomicReference<>();
        // Boot up the first indexing consumer, and consume some events.
        Thread initialIndexerThread = new Thread(
                newConsumerRunnable(indexer, topic, exceptionHolder), "Initial Indexer");
        initialIndexerThread.start();

        Condition<Long> expectedDocCount = new Condition<>(() -> discoRepository.count(),
                "DiscoRepository contains expected document count.");
        assertTrue(expectedDocCount.verify((count) -> count == 5));

        // clean up
        indexer.getConsumer().wakeup();
        initialIndexerThread.join();

        assertExceptionHolderEmpty("Consumer threw an unexpected exception.", exceptionHolder);

        final Set<DiscoSolrDocument> inactive = discoRepository.findDiscoSolrDocumentsByDiscoStatus("INACTIVE");
        final Set<DiscoSolrDocument> active = discoRepository.findDiscoSolrDocumentsByDiscoStatus("ACTIVE");
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

    /*
     * TODO refactor into sharable util method
     */
    private static Map<RMapObjectType, Set<TestUtils.RDFResource>> populateTriplestore(SesameTriplestore triplestore,
                                                                                       RDFHandler rdfHandler,
                                                                                       RMapService rMapService,
                                                                                       String resourcePath)
            throws Exception {
        Map<RMapObjectType, Set<TestUtils.RDFResource>> rmapObjects = new HashMap<>();
        getRmapResources(resourcePath,rdfHandler, RDFFormat.NQUADS, rmapObjects);
        assertFalse(rmapObjects.isEmpty());

        RMapAgent systemAgent = createSystemAgent(rMapService);
        RequestEventDetails requestEventDetails = new RequestEventDetails(systemAgent.getId().getIri());

        List<RMapAgent> agents = getRmapObjects(rmapObjects, RMapObjectType.AGENT, rdfHandler);
        assertNotNull(agents);
        assertTrue(agents.size() > 0);
        LOG.debug("Creating {} agents", agents.size());
        agents.forEach(agent -> {
            try {
                rMapService.createAgent(agent, requestEventDetails);
            } catch (Exception e) {
                LOG.debug("Error creating agent {}: {}", agent.getId().getStringValue(), e.getMessage(), e);
            }
        });

        // Print out the triplestore contents to stderr
        System.err.println("Dump one:");
        dumpTriplestore(triplestore, new PrintStream(System.err, true));

        rmapObjects.values().stream().flatMap(Set::stream)
                .forEach(source -> {
                    try (InputStream in = source.getInputStream();
                         RepositoryConnection c = triplestore.getConnection();
                    ) {
                        assertTrue(c.isOpen());
                        c.add(in, "http://foo/bar", source.getRdfFormat());
                    } catch (IOException e) {
                        e.printStackTrace(System.err);
                        fail("Unexpected IOException");
                    }
                });

        System.err.println("Dump two:");
        dumpTriplestore(triplestore, new PrintStream(System.err, true));

        return rmapObjects;
    }

}
