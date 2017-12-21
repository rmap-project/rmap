package info.rmapproject.indexing.solr.repository;

import static info.rmapproject.core.model.RMapStatus.ACTIVE;
import static info.rmapproject.core.model.RMapStatus.INACTIVE;
import static info.rmapproject.core.model.RMapStatus.TOMBSTONED;
import static info.rmapproject.indexing.solr.TestUtils.prepareIndexableDtos;
import static info.rmapproject.indexing.solr.repository.MappingUtils.tripleToRDF;
import static java.lang.Long.parseLong;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.util.JavaBinCodec;
import org.apache.solr.common.util.NamedList;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Sort;
import org.springframework.data.solr.core.DefaultQueryParser;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.PartialUpdate;
import org.springframework.data.solr.core.query.Query;
import org.springframework.lang.Nullable;

import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapObjectType;
import info.rmapproject.core.model.agent.RMapAgent;
import info.rmapproject.core.model.disco.RMapDiSCO;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.model.event.RMapEventDerivation;
import info.rmapproject.core.model.event.RMapEventUpdate;
import info.rmapproject.core.model.event.RMapEventWithNewObjects;
import info.rmapproject.core.rdfhandler.RDFHandler;
import info.rmapproject.core.rdfhandler.RDFType;
import info.rmapproject.indexing.IndexUtils;
import info.rmapproject.indexing.solr.AbstractSpringIndexingTest;
import info.rmapproject.indexing.solr.TestUtils;
import info.rmapproject.indexing.solr.model.DiscoSolrDocument;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class SimpleSolrIT extends AbstractSpringIndexingTest {

    @Autowired
    private RDFHandler rdfHandler;

    @Autowired
    private DiscoRepository discoRepository;

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private DiscosIndexer discosIndexer;

    @Autowired
    private IndexDTOMapper mapper;

    /**
     * Tests the {@link DiscosIndexer#index(Stream)} method by supplying three {@code IndexDTO}s for indexing from
     * the {@code /data/discos/rmd18mddcw} directory:
     * <ul>
     *     <li>a creation event</li>
     *     <li>followed by an update event</li>
     *     <li>followed by another update event</li>
     * </ul>
     * This test insures that the {@link DiscoSolrDocument}s in the index have the correct
     * {@link DiscoSolrDocument#DISCO_STATUS} after the three events have been processed.
     *
     * @see <a href="src/test/resources/data/discos/rmd18mddcw/README.md">README.MD</a>
     */
    @Test
    public void testIndexingDiscoStatusCreateAndUpdateAndUpdate() {
        LOG.debug("Deleting everything in the index.");
        discoRepository.deleteAll();
        assertEquals(0, discoRepository.count());

        Consumer<Map<RMapObjectType, Set<TestUtils.RDFResource>>> assertions = (resources) -> {
            List<RMapDiSCO> discos = TestUtils.getRmapObjects(resources, RMapObjectType.DISCO, rdfHandler);
            assertEquals(3, discos.size());

            List<RMapEvent> events = TestUtils.getRmapObjects(resources, RMapObjectType.EVENT, rdfHandler);
            assertEquals(3, events.size());

            List<RMapAgent> agents = TestUtils.getRmapObjects(resources, RMapObjectType.AGENT, rdfHandler);
            assertEquals(1, agents.size());
        };

        LOG.debug("Preparing indexable objects.");
        Stream<IndexDTO> dtos = prepareIndexableDtos(rdfHandler,"/data/discos/rmd18mddcw", assertions);

        dtos.peek(dto -> LOG.debug("Indexing {}", dto)).forEach(dto -> discosIndexer.index(mapper.apply(dto)));

        // 5 documents should have been added
        // - one document per DiSCO, Event tuple
        assertEquals(5, discoRepository.count());

        // 1 document should be active
        Set<DiscoSolrDocument> docs = discoRepository.findDiscoSolrDocumentsByDiscoStatus(ACTIVE.toString());
        assertEquals(1, docs.size());

        // assert it is the uri we expect
        DiscoSolrDocument active = docs.iterator().next();
        assertTrue(active.getDiscoUri().endsWith("rmd18mddcw"));

        // the other four should be inactive
        docs = discoRepository.findDiscoSolrDocumentsByDiscoStatus(INACTIVE.toString());
        assertEquals(4, docs.size());

        // assert they have the uris we expect
        assertEquals(2, docs.stream().filter(doc -> doc.getDiscoUri().endsWith("rmd18mdd8b")).count());
        assertEquals(2, docs.stream().filter(doc -> doc.getDiscoUri().endsWith("rmd18m7mr7")).count());
    }

    /**
     * Documents what happens when the same DTOs are indexed twice, in order.
     */
    @Test
    public void testIndexDuplicateDTOs() {
          discoRepository.deleteAll();
        assertEquals(0, discoRepository.count());

        // index the same DTOs twice.
        prepareIndexableDtos(rdfHandler,"/data/discos/rmd18mddcw", null)
                .forEach(dto -> discosIndexer.index(mapper.apply(dto)));

        prepareIndexableDtos(rdfHandler,"/data/discos/rmd18mddcw", null)
                .forEach(dto -> discosIndexer.index(mapper.apply(dto)));

        // 10 documents should have been added
        // - one document per DiSCO, Event tuple
        assertEquals(10, discoRepository.count());

        // 2 documents should be active - should be the same, so there'd normally just be one object in the set, except
        // they differ by their last updated time
        Set<DiscoSolrDocument> docs = discoRepository.findDiscoSolrDocumentsByDiscoStatus(ACTIVE.toString());
        assertEquals(2, docs.size());

        // assert it is the uri we expect
        assertEquals(2, docs.stream().filter(doc -> doc.getDiscoUri().endsWith("rmd18mddcw")).count());

        // the other four should be inactive
        docs = discoRepository.findDiscoSolrDocumentsByDiscoStatus(INACTIVE.toString());
        assertEquals(8, docs.size());

        // assert they have the uris we expect
        assertEquals(4, docs.stream().filter(doc -> doc.getDiscoUri().endsWith("rmd18mdd8b")).count());
        assertEquals(4, docs.stream().filter(doc -> doc.getDiscoUri().endsWith("rmd18m7mr7")).count());
    }

    /**
     * Tests the {@link DiscosIndexer#index(Stream)} method by supplying three {@code IndexDTO}s for indexing from
     * the {@code /data/discos/rmd18mddcw} directory:
     * <ul>
     *     <li>a creation event</li>
     *     <li>followed by an update event</li>
     *     <li>followed by another update event</li>
     *     <li>followed by a tombstone</li>
     * </ul>
     * This test insures that the {@link DiscoSolrDocument}s in the index have the correct
     * {@link DiscoSolrDocument#DISCO_STATUS} after the four events have been processed.
     *
     * @see <a href="src/test/resources/data/discos/rmd18mddcw/README.md">README.MD</a>
     */
    @Test
    public void testIndexingDiscoStatusCreateAndUpdateAndUpdateAndTombstone() {
        LOG.debug("Deleting everything in the index.");
        discoRepository.deleteAll();
        assertEquals(0, discoRepository.count());

        Consumer<Map<RMapObjectType, Set<TestUtils.RDFResource>>> assertions = (resources) -> {
            List<RMapDiSCO> discos = TestUtils.getRmapObjects(resources, RMapObjectType.DISCO, rdfHandler);
            assertEquals(3, discos.size());

            List<RMapEvent> events = TestUtils.getRmapObjects(resources, RMapObjectType.EVENT, rdfHandler);
            assertEquals(4, events.size());

            List<RMapAgent> agents = TestUtils.getRmapObjects(resources, RMapObjectType.AGENT, rdfHandler);
            assertEquals(1, agents.size());
        };

        LOG.debug("Preparing indexable objects.");
        Stream<IndexDTO> dtos = prepareIndexableDtos(rdfHandler,"/data/discos/rmd18mddcw-with-tombstone", assertions);

        dtos.peek(dto -> LOG.debug("Indexing {}", dto)).forEach(dto -> discosIndexer.index(mapper.apply(dto)));

        // 6 documents should have been added
        // - one document per DiSCO, Event tuple
        assertEquals(6, discoRepository.count());

        // No documents should be active
        Set<DiscoSolrDocument> docs = discoRepository.findDiscoSolrDocumentsByDiscoStatus(ACTIVE.toString());
        assertEquals(0, docs.size());

        // 4 documents should be inactive
        docs = discoRepository.findDiscoSolrDocumentsByDiscoStatus(INACTIVE.toString());
        assertEquals(4, docs.size());

        // assert they have the uris we expect
        assertEquals(2, docs.stream().filter(doc -> doc.getDiscoUri().endsWith("rmd18mdd8b")).count());
        assertEquals(2, docs.stream().filter(doc -> doc.getDiscoUri().endsWith("rmd18m7mr7")).count());

        // 2 documents should be tombstoned
        docs = discoRepository.findDiscoSolrDocumentsByDiscoStatus(TOMBSTONED.toString());

        // assert it is the uri we expect
        assertEquals(2, docs.stream().filter(doc -> doc.getDiscoUri().endsWith("rmd18mddcw")).count());
    }
    
    /**
     * Fails: can't specify a core name to ping
     */
    @Test
    @Ignore("Fails: can't specify a core name to ping")
    public void testPing() throws Exception {
        SolrPingResponse res = solrTemplate.ping();
        assertNotNull(res);
        assertTrue(res.getElapsedTime() > 0);
        assertEquals(0, res.getStatus());
    }

    /**
     * Write a document using the SolrTemplate
     */
    @Test
    @SuppressWarnings("unchecked")
    public void simpleWrite() throws Exception {
        DiscoSolrDocument doc = discoDocument("1", "simpleWriteWithTemplate");

        // NOT TRUE WITH spring-data-solr 3: Don't need to use the saveBean(core, doc) method, because the document has the core as an annotation
        UpdateResponse res = solrTemplate.saveBean("discos", doc);
        assertNotNull(res);

        solrTemplate.commit("discos");
    }

    /**
     * Write a document using domain-specific DiscoRepository
     *
     * Fails on commit() with no core specified
     */
    @Test
    public void simpleWriteWithRepo() throws Exception {
        DiscoSolrDocument doc = discoDocument("10", "simpleWriteWithRepo");

        DiscoSolrDocument saved = discoRepository.save(doc);
        assertNotNull(saved);
    }

    @Test
    public void simpleCountAndDelete() throws Exception {
        DiscoSolrDocument doc = discoDocument("20", "simpleCountAndDelete");

        DiscoSolrDocument saved = discoRepository.save(doc);
        assertNotNull(saved);

        assertTrue(discoRepository.count() > 0);

        discoRepository.deleteAll();

        assertEquals(0, discoRepository.count());
    }

    @Test
    public void testSimpleFindUsingDocumentId() throws Exception {
        List<Long> ids = Arrays.asList(100L, 101L, 102L);
        ids.stream().map(String::valueOf).map(id -> discoDocument(id, "testSimpleSearchUsingDocumentId"))
                .forEach(doc -> discoRepository.save(doc));

        Set<String> found = StreamSupport.stream(
                discoRepository.findAllById(ids).spliterator(), false)
                .map(DiscoSolrDocument::getDocId)
                .collect(Collectors.toSet());

        ids.forEach(expectedId -> assertTrue(found.stream().map(Long::parseLong).anyMatch(expectedId::equals)));
    }

    @Test
    public void testSimpleFindUsingCriteria() throws Exception {
        discoRepository.deleteAll();
        registerUriConverter(solrTemplate);
        List<Long> ids = Arrays.asList(200L, 201L, 202L);
        ids.stream().map(String::valueOf).map(id -> discoDocument(id, "testSimpleFindUsingCriteria"))
                .forEach(doc -> discoRepository.save(doc));

        Set<DiscoSolrDocument> found = discoRepository
                .findDiscoSolrDocumentsByDiscoAggregatedResourceUris(URI.create("http://doi.org/10.1109/disco.test"));

        assertNotNull(found);

        Set<DiscoSolrDocument> filtered = found.stream()
                .filter(doc -> (parseLong(doc.getDocId()) > 199 && parseLong(doc.getDocId()) < 300))
                .collect(Collectors.toSet());

        assertEquals(3, filtered.size());
        assertTrue(filtered.stream().allMatch(doc -> parseLong(doc.getDocId()) >= 200 && parseLong(doc.getDocId()) < 203));
    }

    @Test
    @SuppressWarnings({"unchecked", "serial"})
    public void testCaseInsensitiveUriSearch() throws Exception {
        registerUriConverter(solrTemplate);
        discoRepository.deleteAll();

        // Store a disco document that has a lower-case resource url and try to find it with an upper case URL

        DiscoSolrDocument doc = discoDocument("300", "testCaseInsensitiveUriSearch");
        assertEquals("http://doi.org/10.1109/disco.test",
                doc.getDiscoAggregatedResourceUris().iterator().next());
        discoRepository.save(doc);

        Set<DiscoSolrDocument> found = discoRepository
                .findDiscoSolrDocumentsByDiscoAggregatedResourceUris(URI.create("http://DOI.ORG/10.1109/disco.test"));

        assertNotNull(found);

        assertEquals(1, found.size());
        assertEquals("300", found.iterator().next().getDocId());
        assertEquals("http://doi.org/10.1109/disco.test",
                found.iterator().next().getDiscoAggregatedResourceUris().iterator().next());

        discoRepository.deleteAll();

        // Store a disco document that has an upper-case resource url and try to find it with a lower case URL

        doc = new DiscoSolrDocument();
        doc.setDocId("301");
        doc.setDiscoAggregatedResourceUris(new ArrayList<String>() {
            {
                add("http://DOI.ORG/10.1109/disco.test");
            }
        });
        discoRepository.save(doc);



        found = discoRepository
                .findDiscoSolrDocumentsByDiscoAggregatedResourceUris(URI.create("http://doi.org/10.1109/disco.test"));

        assertNotNull(found);

        assertEquals(1, found.size());
        assertEquals("301", found.iterator().next().getDocId());
        assertEquals("http://DOI.ORG/10.1109/disco.test",
                found.iterator().next().getDiscoAggregatedResourceUris().iterator().next());

    }

    @Test
    public void testWildcardUriSearch() throws Exception {
        discoRepository.deleteAll();

        // Store a disco document that has a field containing a URL, and see if we can retrieve that document using
        // a wildcard search

        DiscoSolrDocument doc = discoDocument("400", "testWildcardUriSearch");
        discoRepository.save(doc);

        Set<DiscoSolrDocument> found = discoRepository.findDiscoSolrDocumentsByDiscoAggregatedResourceUrisContains("http://doi.org/10.1109/");
        assertEquals(1, found.size());

        found = discoRepository.findDiscoSolrDocumentsByDiscoAggregatedResourceUrisContains("10.1109");
        assertEquals(1, found.size());
    }

    @Test
    public void writeAndUpdateDiscoDocUsingPartialUpdate() throws Exception {
        String solrCore = "discos";
        long expectedDocId = 200L;
        DiscoSolrDocument doc = new DiscoSolrDocument.Builder()
                .docId(String.valueOf(expectedDocId))
                .discoUri("http://doi.org/10.1109/disco/2")
                .discoStatus("ACTIVE")
                .build();

        UpdateResponse res = solrTemplate.saveBean(solrCore, doc);
        assertNotNull(res);
        assertEquals(0, res.getStatus());
        solrTemplate.commit(solrCore);


        assertEquals(doc, solrTemplate.getById(solrCore, expectedDocId, DiscoSolrDocument.class)
                .orElseThrow(() -> new RuntimeException(
                        "Expected to find a DiscoSolrDocument with ID " + doc.getDocId() + " in the index.")));

        // Update using Solr Atomic Updates (requires <updateLog/>)
        PartialUpdate update = new PartialUpdate(DiscoSolrDocument.DOC_ID, doc.getDocId());
        update.setValueOfField(DiscoSolrDocument.DISCO_STATUS, "INACTIVE");
        res = solrTemplate.saveBean(solrCore, update);
        assertNotNull(res);
        assertEquals(0, res.getStatus());
        solrTemplate.commit(solrCore);

        assertEquals("INACTIVE", solrTemplate.getById(solrCore, expectedDocId, DiscoSolrDocument.class)
                .orElseThrow(() -> new RuntimeException(
                        "Expected to find a DiscoSolrDocument with ID " + doc.getDocId() + " in the index."))
                .getDiscoStatus());
    }

    @Test
    public void indexDiscoRdf() throws Exception {
        discoRepository.deleteAll();
        assertEquals(0, discoRepository.count());

        AtomicInteger idCounter = new AtomicInteger(1);

        Map<RMapObjectType, Set<TestUtils.RDFResource>> rmapObjects = new HashMap<>();
        TestUtils.getRmapResources("/data/discos/rmd18mddcw", rdfHandler, RDFFormat.NQUADS, rmapObjects);

        List<RMapDiSCO> discos = TestUtils.getRmapObjects(rmapObjects, RMapObjectType.DISCO, rdfHandler);
        assertEquals(3, discos.size());

        List<RMapEvent> events = TestUtils.getRmapObjects(rmapObjects, RMapObjectType.EVENT, rdfHandler);
        assertEquals(3, events.size());

        List<RMapAgent> agents = TestUtils.getRmapObjects(rmapObjects, RMapObjectType.AGENT, rdfHandler);
        assertEquals(1, agents.size());

        events.stream()
                .flatMap(event -> {

                    final RMapIri agentIri = event.getAssociatedAgent();
                    final RMapAgent agent = agents.stream()
                            .filter(a -> a.getId().getStringValue().equals(agentIri.getStringValue()))
                            .findAny()
                            .orElseThrow(() ->
                                    new RuntimeException("Missing agent '" + agentIri + "' of event " + event));

                    RMapIri source;
                    RMapIri target;

                    switch (event.getEventType()) {
                        case CREATION:
                            source = null;
                            target = ((RMapEventWithNewObjects) event).getCreatedObjectIds().get(0);
                            break;
                        case UPDATE:
                            source = ((RMapEventUpdate) event).getInactivatedObjectId();
                            target = ((RMapEventUpdate) event).getDerivedObjectId();
                            break;
                        case DERIVATION:
                            source = ((RMapEventDerivation) event).getSourceObjectId();
                            target = ((RMapEventDerivation) event).getDerivedObjectId();
                            break;
                        default:
                            throw new RuntimeException("Unhandled event type " + event);
                    }

                    EventDiscoTuple forSource = null;
                    // The source IRI will be null in the case of a creation event
                    if (source != null) {
                        forSource = new EventDiscoTuple();

                        forSource.eventSource = source;
                        forSource.eventTarget = target;
                        forSource.event = event;
                        forSource.agent = agent;
                        forSource.disco = discos.stream()
                                .filter(d -> d.getId().getStringValue().equals(source.getStringValue()))
                                .findAny()
                                .orElseThrow(() ->
                                        new RuntimeException("Missing source '" + source + "' of event " + event));
                    }

                    // The target IRI should never be null
                    EventDiscoTuple forTarget = new EventDiscoTuple();

                    forTarget.eventSource = source;
                    forTarget.eventTarget = target;
                    forTarget.event = event;
                    forTarget.agent = agent;
                    forTarget.disco = discos.stream()
                            .filter(d -> d.getId().getStringValue().equals(target.getStringValue()))
                            .findAny()
                            .orElseThrow(() ->
                                    new RuntimeException("Missing target '" + target + "' of event " + event));


                    return Stream.of(Optional.ofNullable(forSource), Optional.of(forTarget));
                })

                .filter(Optional::isPresent)

                .map(Optional::get)

                .map(toIndex -> {

                    DiscoSolrDocument doc = new DiscoSolrDocument();

                    doc.setDocId(String.valueOf(idCounter.getAndIncrement()));

                	List<String> stmts = new ArrayList<String>();
                	toIndex.disco.getRelatedStatements().forEach(t 
                			-> stmts.add(tripleToRDF(t,rdfHandler,RDFType.NQUADS)));        	
                	doc.setDiscoRelatedStatements(stmts);
                    
                    doc.setDiscoUri(toIndex.disco.getId().getStringValue());
                    doc.setDiscoCreatorUri(toIndex.disco.getCreator().getStringValue());               // TODO: Resolve creator and index creator properties?
                    doc.setDiscoAggregatedResourceUris(toIndex.disco.getAggregatedResources()
                            .stream().map(URI::toString).collect(Collectors.toList()));
                    doc.setDiscoDescription(toIndex.disco.getDescription().getStringValue());
                    doc.setDiscoProvenanceUri(toIndex.disco.getProvGeneratedBy() != null ? toIndex.disco.getProvGeneratedBy().getStringValue() : null);

                    doc.setAgentUri(toIndex.agent.getId().getStringValue());
                    doc.setAgentName(toIndex.agent.getName().getStringValue());
                    doc.setAgentProviderUri(toIndex.agent.getIdProvider().getStringValue());
                    // TODO? toIndex.agent.getAuthId()

                    doc.setEventUri(toIndex.event.getId().getStringValue());
                    doc.setEventAgentUri(toIndex.event.getAssociatedAgent().getStringValue());
                    doc.setEventDescription(toIndex.event.getDescription() != null ? toIndex.event.getDescription().getStringValue() : null);
                    doc.setEventStartTime(IndexUtils.dateToString(toIndex.event.getStartTime()));
                    doc.setEventEndTime(IndexUtils.dateToString(toIndex.event.getEndTime()));
                    doc.setEventType(toIndex.event.getEventType().name());
                    doc.setEventTargetObjectUris(Collections.singletonList(toIndex.eventTarget.getStringValue()));
                    if (toIndex.eventSource != null) {
                        doc.setEventSourceObjectUris(Collections.singletonList(toIndex.eventSource.getStringValue()));
                    }

                    return doc;
                })

                .forEach(doc -> discoRepository.save(doc));
    }


    @Test
    @SuppressWarnings("unchecked")
    public void javabinTest() throws Exception {
        try (InputStream in = this.getClass().getResourceAsStream("/javabin.out")) {
            NamedList<Object> response = (NamedList) new JavaBinCodec().unmarshal(in);
            assertNotNull(response);
        }
    }

    private static void registerUriConverter(SolrTemplate solrTemplate) {
        DefaultQueryParser queryParser = new DefaultQueryParser();
        queryParser.registerConverter(new Converter<URI, String>() {
            @Nullable
            @Override
            public String convert(URI uri) {
                if (uri == null) {
                    return null;
                }
                String converted = uri.toString().replaceAll(":", "\\\\:");
                return converted;
            }
        });
        solrTemplate.registerQueryParser(Query.class, queryParser);
    }

    @Test
    public void testKafkaOffsetSort() throws Exception {
        discoRepository.deleteAll();
        assertEquals(0, discoRepository.count());

        // Create 100 documents, increasing the offset, and storing them in the index

        Set<DiscoSolrDocument> toStore = new HashSet<>();

        for (int i = 0; i < 100; i++) {
            DiscoSolrDocument doc = new DiscoSolrDocument.Builder()
                    .docId(String.valueOf(i))
                    .kafkaOffset(i)
                    .kafkaTopic("topic")
                    .kafkaPartition(2)
                    .build();
            toStore.add(doc);
        }

        discoRepository.saveAll(toStore);

        List<DiscoSolrDocument> results = discoRepository.findTopDiscoSolrDocumentByKafkaTopicAndKafkaPartition("topic", 2, new Sort(Sort.Direction.DESC, "kafka_offset"));

        assertNotNull(results);
        assertEquals(1, results.size());

        assertEquals(99, results.get(0).getKafkaOffset());

        assertEquals(0, discoRepository.findTopDiscoSolrDocumentByKafkaTopicAndKafkaPartition("topic", 0, new Sort(Sort.Direction.DESC, "kafka_offset")).size());
        assertEquals(0, discoRepository.findTopDiscoSolrDocumentByKafkaTopicAndKafkaPartition("foo", 2, new Sort(Sort.Direction.DESC, "kafka_offset")).size());
    }

    /**
     * Domain instance document
     * @param id
     * @param testDescription
     * @return
     */
    @SuppressWarnings("serial")
    private static DiscoSolrDocument discoDocument(String id, String testDescription) {
        DiscoSolrDocument doc = new DiscoSolrDocument();
        doc.setDiscoDescription(testDescription);
        doc.setDocId(id);
        doc.setDiscoUri("http://rmapproject.org/disco/5678f");
        doc.setDiscoCreatorUri("http://foaf.org/Elliot_Metsger");
        doc.setDiscoAggregatedResourceUris(new ArrayList<String>() {
            {
                add("http://doi.org/10.1109/disco.test");
                add("http://ieeexplore.ieee.org/example/000000-mm.zip");
            }
        });
        doc.setDiscoProvenanceUri("http://rmapproject.org/prov/5678");
        doc.setDiscoRelatedStatements(new ArrayList<String>() {
        	{
        		add("TODO n3 triples");
        	}
        });
        return doc;
    }

}
