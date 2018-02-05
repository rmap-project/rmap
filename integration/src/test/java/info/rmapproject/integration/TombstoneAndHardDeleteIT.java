package info.rmapproject.integration;

import info.rmapproject.indexing.kafka.Condition;
import info.rmapproject.indexing.solr.model.DiscoSolrDocument;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
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

        String v2DiscoUri = depositDisco(encodeDiscoUriAsUrl(v1DiscoUri), discoV2);

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

        String v3DiscoUri = depositDisco(encodeDiscoUriAsUrl(v2DiscoUri), discoV3);

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

        deleteDisco(encodeDiscoUriAsUrl(v2DiscoUri));

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
     * Logs some information about each document at TRACE level.
     *
     * @param docs a Collection of documents
     */
    private static void logDocuments(Collection<DiscoSolrDocument> docs) {
        docs.forEach(doc -> LOG.trace("Doc (last updated: {}, status: {}, disco: {}): {}",
                doc.getDocLastUpdated(), doc.getDiscoStatus(), doc.getDiscoUri(), doc.getDocId()));
    }
}
