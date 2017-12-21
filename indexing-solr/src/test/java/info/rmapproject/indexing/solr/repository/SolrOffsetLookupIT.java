package info.rmapproject.indexing.solr.repository;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import info.rmapproject.indexing.kafka.OffsetLookup;
import info.rmapproject.indexing.kafka.Seek;
import info.rmapproject.indexing.solr.AbstractSpringIndexingTest;
import info.rmapproject.indexing.solr.model.DiscoSolrDocument;

public class SolrOffsetLookupIT extends AbstractSpringIndexingTest {

    private static boolean initialized = false;

    @Autowired
    private OffsetLookup underTest;

    @Autowired
    private DiscoRepository repo;

    @Value("${rmapcore.producer.topic}")
    private String topic;

    @Before
    public void init() {
        if (initialized) {
            return;
        }

        Set<DiscoSolrDocument> docs = new HashSet<>();
        for (int i = 0; i < 82; i++) {
            docs.add(new DiscoSolrDocument.Builder()
                    .docId(String.valueOf(i))
                    .kafkaTopic(topic)
                    .kafkaPartition(0)
                    .kafkaOffset(i)
                    .build());

        }
        repo.saveAll(docs);
        initialized = true;
    }

    @Test
    public void testLatestLookup() throws Exception {
        assertEquals(81, underTest.lookupOffset(topic, 0, Seek.LATEST));
    }

    @Test
    public void testEarliestLookup() throws Exception {
        assertEquals(0, underTest.lookupOffset(topic, 0, Seek.EARLIEST));
    }

    @Test
    public void testUnknownTopicLookup() throws Exception {
        assertEquals(-1, underTest.lookupOffset("bar", 0, Seek.LATEST));
    }

    @Test
    public void testUnknownPartitionLookup() throws Exception {
        assertEquals(-1, underTest.lookupOffset(topic, 1, Seek.LATEST));
    }
}
