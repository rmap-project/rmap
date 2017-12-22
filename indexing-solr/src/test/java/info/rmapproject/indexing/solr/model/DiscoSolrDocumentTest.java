package info.rmapproject.indexing.solr.model;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.util.HashMap;
import java.util.List;

import org.junit.Test;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class DiscoSolrDocumentTest {

    private static final String A_URI = "http://a/uri";

    private static final List<String> LIST_WITH_URI = singletonList(A_URI);

    @Test
    @SuppressWarnings("serial")
    public void testCopyConstructor() throws Exception {
        DiscoSolrDocument.Builder builder = new DiscoSolrDocument.Builder();

        DiscoSolrDocument doc1 = builder.docId("doc_id")
                .docLastUpdated(1L)
                .discoUri(A_URI + "/1")
                .discoCreatorUri(A_URI + "/2")
                .discoDescription("discodesc")
                .discoProviderid(A_URI + "/3")
                .discoAggregatedResourceUris(singletonList(A_URI + "/9"))
                .discoProvenanceUri(A_URI + "/4")
                .discoRelatedStatements(singletonList(A_URI + "/10"))
                .discoStatus("ACTIVE")
                .discoEventDirection("TARGET")

                .eventUri(A_URI + "/5")
                .eventAgentUri(A_URI + "/6")
                .eventStartTime("start")
                .eventEndTime("end")
                .eventDescription("eventdesc")
                .eventType("CREATION")
                .eventSourceObjectUris(singletonList(A_URI + "/11"))
                .eventTargetObjectUris(singletonList(A_URI + "/12"))

                .agentUri(A_URI + "/7")
                .agentProviderUri(A_URI + "/8")
                .agentName("agentname")

                .kafkaTopic("topic")
                .kafkaPartition(0)
                .kafkaOffset(0L)
                .metadata(new HashMap<String, String>() {
                    {
                        put("foo", "bar");
                    }
                })

                .build();

        DiscoSolrDocument doc2 = new DiscoSolrDocument(doc1);

        assertEquals(doc1, doc2);
    }

    @Test
    @SuppressWarnings("serial")
    public void testBuilderCopy() throws Exception {
        DiscoSolrDocument.Builder builder = new DiscoSolrDocument.Builder();

        DiscoSolrDocument doc1 = builder.docId("doc_id")
                .docLastUpdated(1L)
                .discoUri(A_URI + "/1")
                .discoCreatorUri(A_URI + "/2")
                .discoDescription("discodesc")
                .discoProviderid(A_URI + "/3")
                .discoAggregatedResourceUris(singletonList(A_URI + "/9"))
                .discoProvenanceUri(A_URI + "/4")
                .discoRelatedStatements(singletonList(A_URI + "/10"))
                .discoStatus("ACTIVE")
                .discoEventDirection("TARGET")

                .eventUri(A_URI + "/5")
                .eventAgentUri(A_URI + "/6")
                .eventStartTime("start")
                .eventEndTime("end")
                .eventDescription("eventdesc")
                .eventType("CREATION")
                .eventSourceObjectUris(singletonList(A_URI + "/11"))
                .eventTargetObjectUris(singletonList(A_URI + "/12"))

                .agentUri(A_URI + "/7")
                .agentProviderUri(A_URI + "/8")
                .agentName("agentname")


                .kafkaTopic("topic")
                .kafkaPartition(0)
                .kafkaOffset(0L)

                .metadata(new HashMap<String, String>() {
                    {
                        put("foo", "bar");
                    }
                })

                .build();

        DiscoSolrDocument doc2 = new DiscoSolrDocument.Builder(doc1).build();

        assertEquals(doc1, doc2);
        assertNotSame(doc1, doc2);
    }
}