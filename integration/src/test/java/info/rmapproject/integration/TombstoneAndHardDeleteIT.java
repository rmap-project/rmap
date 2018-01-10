package info.rmapproject.integration;

import info.rmapproject.indexing.kafka.Condition;
import info.rmapproject.indexing.solr.model.DiscoSolrDocument;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * ITs specific to tombstoning or hard-deleting DiSCOs
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class TombstoneAndHardDeleteIT extends BaseHttpIT {

    private final String V1_VERIFY_MSG = "Verifying that the number of docs with ACTIVE status for lineage {} is " +
            "greater than 0, and that the document is ACTIVE and pertains to the correct disco uri";
    private final String V2_VERIFY_MSG = "Verifying that the number of docs for lineage {} is equal to 3, and that " +
            "one is ACTIVE, and the rest INACTIVE";
    private final String V3_VERIFY_MSG = "Verifying that the number of docs for lineage {} is equal to 5, and that " +
            "one is ACTIVE, and others INACTIVE";
    private final String TB_VERIFY_MSG = "Verifying that the number of docs for lineage {} is equal to 3, and that " +
            "one is ACTIVE, and others INACTIVE";

    /**
     * Version 1 of a DiSCO
     */
    private String discoV1;

    /**
     * Version 2 of a DiSCO
     */
    private String discoV2;

    /**
     * Version 3 of a DiSCO
     */
    private String discoV3;

    @Before
    public void setUp() throws Exception {
        discoV1 = IOUtils.toString(
                this.getClass().getResourceAsStream("/discos/discoB_v1.rdf"), "UTF-8");
        discoV2 = IOUtils.toString(
                this.getClass().getResourceAsStream("/discos/discoB_v2.rdf"), "UTF-8");
        discoV3 = IOUtils.toString(
                this.getClass().getResourceAsStream("/discos/discoB_v3.rdf"), "UTF-8");
    }

    /*

    From: https://github.com/rmap-project/rmap/issues/176

        //create discoV1
        discoV1 (active)
        //update discoV1
        discoV1 (inactive)
        discoV2 (active)
        //update discoV2
        discoV1 (inactive)
        discoV2 (inactive)
        discoV3 (active)
        //update discoV2
        Cant update, the most recent version is discoV3
        //tombstone discoV2
        discoV1 (inactive)
        discoV2 (tombstoned - but content in triplestore)
        discoV3 (active)
        //hard delete discoV3
        discoV1 (inactive)
        discoV2 (tombstoned - but content in triplestore)
        discoV3 (deleted - content not in triplestore)
     */

    /**
     * Creates three versions of a DiSCO using the RMap HTTP API, and tombstones the second version.  Insures that the
     * correct documents are in the index each step of the way.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testCreateUpdateDeleteDisco() throws IOException, InterruptedException {
        LOG.trace("** Beginning testCreateUpdateDeleteDisco");

        LOG.trace("** Depositing DiSCO V1 ...");

        // Deposit version 1 of the DiSCO; expect the DiSCO to be indexed as an ACTIVE DiSCO

        String v1DiscoUri = depositDisco(discosEndpoint.toURL(), discoV1);

        LOG.trace("** Deposited DiSCO V1 with URI {}", v1DiscoUri);

        LOG.trace("** Checking ACTIVE document count for progenitor lineage {} ...", v1DiscoUri);

        // Verify that the DiSCO is indexed, inferred by the increase in the number of active Solr documents in the
        // index for the new lineage
        Condition<List<DiscoSolrDocument>> activeDocsForLineage = new Condition<>(() -> {
            List<DiscoSolrDocument> activeCount = discoRepository
                    .findDiscoSolrDocumentsByEventLineageProgenitorUriAndDiscoStatus(v1DiscoUri, "ACTIVE")
                    .stream()
                    .sorted(comparing(DiscoSolrDocument::getDocLastUpdated).reversed())
                    .collect(toList());

            LOG.trace("Current ACTIVE document count for lineage {}: {}", v1DiscoUri, activeCount);
            return activeCount;
        }, "Count of ACTIVE documents for lineage " + v1DiscoUri);

        assertTrue(activeDocsForLineage.awaitAndVerify((docs) -> {
            LOG.trace(V1_VERIFY_MSG, v1DiscoUri);
            return docs.size() == 1
                    && docs.get(0).getDiscoStatus().equals("ACTIVE")
                    && docs.get(0).getDiscoUri().equals(v1DiscoUri)
                    && docs.get(0).getEventLineageProgenitorUri().equals(v1DiscoUri);
        }));

        LOG.trace("** Update DiSCO {} ...", v1DiscoUri);

        // Deposit version 2 of the DiSCO; expect a second document to be indexed as the ACTIVE DiSCO, and the former
        // document to be INACTIVE

        String v2DiscoUri = depositDisco(URI.create(discosEndpoint.toString() + "/" + URLEncoder.encode(v1DiscoUri, "UTF-8")).toURL(), discoV2);

        // Verify that the DiSCO is indexed, inferred by the increase in the number of Solr documents in the
        // index for the lineage.  One should be active, one should be inactive
        Condition<List<DiscoSolrDocument>> allDocsForLineage = new Condition<>(() -> {
            Set<DiscoSolrDocument> docs = discoRepository
                    .findDiscoSolrDocumentsByEventLineageProgenitorUri(v1DiscoUri);
            LOG.trace("Current document count for lineage {}: {}", v1DiscoUri, docs.size());
            return docs.stream().sorted(comparing(DiscoSolrDocument::getDocLastUpdated).reversed()).collect(toList());
        }, "Documents for lineage " + v1DiscoUri);

        assertTrue(allDocsForLineage.awaitAndVerify((docs) -> {
            LOG.trace(V2_VERIFY_MSG, v1DiscoUri);
            logDocuments(docs);
            return docs.size() == 3
                    && docs.get(0).getDiscoStatus().equals("INACTIVE")
                    && docs.get(1).getDiscoStatus().equals("INACTIVE")
                    && docs.get(2).getDiscoStatus().equals("ACTIVE")
                    && docs.get(2).getDiscoUri().equals(v2DiscoUri)
                    && docs.get(2).getEventLineageProgenitorUri().equals(v1DiscoUri);
        }));

        LOG.trace("** Update DiSCO {} ...", v2DiscoUri);

        // Deposit version 3 of the DiSCO; expect a third document to be indexed as the ACTIVE DiSCO, and the former
        // documents to be INACTIVE

        String v3DiscoUri = depositDisco(URI.create(discosEndpoint.toString() + "/" + URLEncoder.encode(v2DiscoUri, "UTF-8")).toURL(), discoV3);

        // Verify that the DiSCO is indexed, inferred by the increase in the number of Solr documents in the
        // index for the lineage.  One should be active, others should be inactive
        assertTrue(allDocsForLineage.awaitAndVerify((docs) -> {
            LOG.trace(V3_VERIFY_MSG, v1DiscoUri);
            logDocuments(docs);
            return docs.size() == 5
                    && docs.get(0).getDiscoStatus().equals("INACTIVE")
                    && docs.get(1).getDiscoStatus().equals("INACTIVE")
                    && docs.get(2).getDiscoStatus().equals("INACTIVE")
                    && docs.get(3).getDiscoStatus().equals("INACTIVE")
                    && docs.get(4).getDiscoStatus().equals("ACTIVE")
                    && docs.get(4).getDiscoUri().equals(v3DiscoUri)
                    && docs.get(4).getEventLineageProgenitorUri().equals(v1DiscoUri);
        }));

        // Tombstone version 2 of the DiSCO; expect the documents that pertain to version 2 to be deleted from the index

        deleteDisco(URI.create(discosEndpoint.toString() + "/" + URLEncoder.encode(v2DiscoUri, "UTF-8")).toURL());

        // Verify that the DiSCO is deleted, inferred by the decrease in the number of Solr documents in the
        // index for the lineage.  One should be active, others should be inactive.
        assertTrue(allDocsForLineage.awaitAndVerify((docs) -> {
            LOG.trace(TB_VERIFY_MSG, v1DiscoUri);
            logDocuments(docs);
            return docs.size() == 3
                    && docs.get(0).getDiscoStatus().equals("INACTIVE")
                    && docs.get(0).getDiscoUri().equals(v1DiscoUri)
                    && docs.get(1).getDiscoStatus().equals("INACTIVE")
                    && docs.get(1).getDiscoUri().equals(v1DiscoUri)
                    && docs.get(2).getDiscoStatus().equals("ACTIVE")
                    && docs.get(2).getDiscoUri().equals(v3DiscoUri)
                    && docs.get(2).getEventLineageProgenitorUri().equals(v1DiscoUri);
        }));

    }

    /**
     * Deposit the DiSCO by performing a {@code POST} to the {@code endpoint}.  The {@code discoBody} must be encoded as
     * rdf/xml. If the request is successful, the return should be a URI to the newly created DiSCO.
     *
     * @param endpoint the /discos API endpoint; /discos/{uri} may be used to create a new version of the disco at
     *                 /discos/{uri}
     * @param discoBody the DiSCO to deposit, encoded as rdf/xml
     * @return a URI to the newly created DiSCO
     * @throws IOException
     */
    private String depositDisco(URL endpoint, String discoBody) throws IOException {
        return decorateAndExecuteRequest(endpoint,
                new Request.Builder()
                        .post(RequestBody.create(MediaType.parse(APPLICATION_RDFXML), discoBody))
                        .url(endpoint),
                201);
    }

    /**
     * Delete (a.k.a. "tombstone", "soft delete") the DiSCO by sending a {@code DELETE} request to the {@code endpoint}.
     *
     * @param endpoint a URL that identifies the DiSCO to delete; /discos/{uri}
     * @throws IOException
     */
    private void deleteDisco(URL endpoint) throws IOException {
        decorateAndExecuteRequest(endpoint,
                new Request.Builder()
                        .delete()
                        .url(endpoint),
                200);
    }

    /**
     * Executes the supplied request and verifies the response code.  The request is decorated with authentication
     * credentials before being sent.
     *
     * @param endpoint the HTTP endpoint
     * @param req the request builder, which is complete except for authentication credentials
     * @param expectedStatus the status that is expected upon successful execution
     * @return the body of the response as a String
     * @throws IOException
     */
    private String decorateAndExecuteRequest(URL endpoint, Request.Builder req, int expectedStatus) throws IOException {
        String body;

        try (Response res =
                     http.newCall(req.addHeader("Authorization", "Basic " + encodeAuthCreds(accessKey, secret))
                             .build())
                             .execute()) {
            assertEquals(endpoint + " failed with: '" + res.code() + "', '" + res.message() + "'",
                    expectedStatus, res.code());
            body = res.body().string();
        }

        return body;
    }

    /**
     * Logs some information about each document at TRACE level.
     *
     * @param docs a Collection of documents
     */
    private static void logDocuments(Collection<DiscoSolrDocument> docs) {
        docs.forEach(doc -> LOG.trace("Doc (last updated: {}, status: {}, disco: {}): {}",
                doc.getDocLastUpdated(), doc.getDiscoStatus(), doc.getDiscoUri(), doc.getDocId()));
    }
}
