package info.rmapproject.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import okhttp3.ResponseBody;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import info.rmapproject.indexing.kafka.Condition;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Basic integration tests against the RMap API and HTML UI web applications.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/integration-context.xml")
public class SmokeTestIT extends BaseHttpIT {

    /**
     * The RMap API webapp should return a 200, indicating successful startup.
     *
     * @throws IOException
     */
    @Test
    public void testApi200Ok() throws IOException {
        String searchString = "https://github.com/rmap-project/rmap-documentation";
        URL url = new URL(apiBaseUrl, apiCtxPath + "/discos");
        Response res = http.newCall(new Request.Builder().get().url(url).build()).execute();
        ResponseBody body = res.body();
        assertNotNull("Expected a non-null response body from " + appBaseUrl.toString(), body);
        String bodyString = body.string();
        assertTrue("Expected the HTML body returned from " + appBaseUrl.toString() + " to contain the " +
                        "string '" + searchString + "' (body was: [" + bodyString + "])",
                bodyString.contains(searchString));
        assertEquals(url.toString() + " failed with: '" + res.code() + "', '" + res.message() + "'",
                200, res.code());
    }

    /**
     * The RMap HTML webapp should return a 200, indicating successful startup.
     *
     * @throws IOException
     */
    @Test
    public void testWebapp200Ok() throws IOException {
        String searchString = "RMap";
        Response res = http.newCall(new Request.Builder().get().url(appBaseUrl).build()).execute();
        ResponseBody body = res.body();
        assertNotNull("Expected a non-null response body from " + appBaseUrl.toString(), body);
        String bodyString = body.string();
        assertTrue("Expected the HTML body returned from " + appBaseUrl.toString() + " to contain the " +
                        "string '" + searchString + "' (body was: [" + bodyString + "])",
                bodyString.contains(searchString));
        assertEquals(appBaseUrl.toString() + " failed with: '" + res.code() + "', '" + res.message() + "'",
                200, res.code());
    }

    /**
     * The RDF4J triplestore, deployed under the {@code /rdf4j-server} context, should return a 200, indicating
     * successful startup.
     *
     * @throws IOException
     */
    @Test
    public void testTriplestoreOk() throws IOException {
        String searchString = "RDF4J Server";
        // The RDF4J triplestore, deployed under the {@code /rdf4j-server} context, has problems.  When visiting the
        // base URL, e.g. http://192.168.99.100:8080/rdf4j-server/, you should expect a 200, but instead you get a 404.
        // This is because the base URL doesn't forward properly, at least in our integration environment.  Manually
        // munging the URL will get us to the right place.
        String rdf4jRepoUrl = BaseHttpIT.rdf4jRepoUrl + "/home/overview.view";
        Response res = http.newCall(new Request.Builder().get().url(rdf4jRepoUrl).build()).execute();
        ResponseBody body = res.body();
        assertNotNull("Expected a non-null response body from " + rdf4jRepoUrl, body);
        String bodyString = body.string();
        assertTrue("Expected the HTML body returned from " + rdf4jRepoUrl + " to contain the string '" +
                searchString + "' (body was: [" + bodyString + "])", bodyString.contains(searchString));
        assertEquals(rdf4jRepoUrl + " failed with: '" + res.code() + "', '" + res.message() + "'",
                200, res.code());
    }

    /**
     * The DataSource for the database should be populated with one user.
     *
     * @throws SQLException
     */
    @Test
    public void testDataSourceOk() throws SQLException {
        assertNotNull(ds);
        Connection c = ds.getConnection();
        assertNotNull(c);
        ResultSet rs = c.prepareCall("SELECT * from USERS").executeQuery();
        assertTrue(rs.next());
        assertFalse(rs.next());
    }

    /**
     * The user should be able to create a new disco.  Insure that a user that doesn't exist cannot create a disco.
     * Insure that an unauthenticated request cannot create a disco.
     *
     * @throws IOException
     */
    @Test
    public void testAuthenticatedRequest() throws IOException, InterruptedException {

        // Bypass the RMap webapps, and directly ask Solr how many documents are in the index.
        // This IT should create documents in the index when DiSCOs are deposited
        long initialDocumentCount = discoRepository.count();

        String accessKey = "uah2CKDaBsEw3cEQ";
        String secret = "NSbdzctrP46ZvhTi";
        URL url = new URL(apiBaseUrl, apiCtxPath + "/discos");
        String sampleDisco = IOUtils.toString(this.getClass().getResourceAsStream("/discos/discoA.ttl"), StandardCharsets.UTF_8);
        String sampleDisco2 = IOUtils.toString(this.getClass().getResourceAsStream("/discos/discoB_v1.rdf"), StandardCharsets.UTF_8);

        try (Response res =
                     http.newCall(new Request.Builder()
                             .post(RequestBody.create(MediaType.parse("text/turtle"), sampleDisco))
                             .url(url).addHeader("Authorization", "Basic " + encodeAuthCreds(accessKey, secret))
                             .build())
                             .execute()) {
            assertEquals(url.toString() + " failed with: '" + res.code() + "', '" + res.message() + "'",
                    201, res.code());
        }

        //make sure we can also create a second DiSCO (Agent record is transferred to 
        //triplestore on 1st DiSCO, verified on the second)
        try (Response res =
                     http.newCall(new Request.Builder()
                             .post(RequestBody.create(MediaType.parse("application/rdf+xml"), sampleDisco2))
                             .url(url).addHeader("Authorization", "Basic " + encodeAuthCreds(accessKey, secret))
                             .build())
                             .execute()) {
            assertEquals(url.toString() + " failed with: '" + res.code() + "', '" + res.message() + "'",
                    201, res.code());
        }
        
        try (Response res =
                     http.newCall(new Request.Builder()
                .post(RequestBody.create(MediaType.parse("text/turtle"), sampleDisco))
                .url(url)
                .addHeader("Authorization", "Basic " + encodeAuthCreds("foo", "bar"))
                .build())
                .execute()) {
            assertEquals(url.toString() + " failed with: '" + res.code() + "', '" + res.message() + "'",
                    401, res.code());
        }

        try (Response res =
                     http.newCall(new Request.Builder()
                .post(RequestBody.create(MediaType.parse("text/turtle"), sampleDisco))
                .url(url)
                .build())
                .execute()) {
            assertEquals(url.toString() + " failed with: '" + res.code() + "', '" + res.message() + "'",
                    401, res.code());
        }


        // Bypass the RMap webapps and check to see that there are documents in the index.
        Condition<Long> docsHaveAppeared = new Condition<>(() -> {
            long count = discoRepository.count();
            LOG.trace("Current document count: {}", count);
            return count;
        },
                "Solr documents in repo greater than " + initialDocumentCount);
        assertTrue(docsHaveAppeared.awaitAndVerify((count) -> {
            LOG.trace("Verifying that {} current docs is greater than {} initial docs", count, initialDocumentCount);
            return count > initialDocumentCount;
        }));
    }

    /**
     * The user creates a new DiSCO, once indexed they then delete it. The index should remove the DiSCO content and
     * display as deleted.
     *
     * @throws IOException
     */
    @Test
    public void testCreateDeleteDiSCOWithIndexer() throws IOException, InterruptedException {
        LOG.trace("** Beginning testCreateDeleteDiSCOWithIndexer");

        String accessKey = "uah2CKDaBsEw3cEQ";
        String secret = "NSbdzctrP46ZvhTi";
        URL url = new URL(apiBaseUrl, apiCtxPath + "/discos");
        String sampleDisco = IOUtils.toString(this.getClass().getResourceAsStream("/discos/discoA.ttl"), StandardCharsets.UTF_8);
        String discoUri;

        LOG.trace("** Depositing DiSCO ...");
        // Deposit a DiSCO; expect the DiSCO to be indexed.
        try (Response res =
                     http.newCall(new Request.Builder()
                             .post(RequestBody.create(MediaType.parse("text/turtle"), sampleDisco))
                             .url(url).addHeader("Authorization", "Basic " + encodeAuthCreds(accessKey, secret))
                             .build())
                             .execute()) {
            assertEquals(url.toString() + " failed with: '" + res.code() + "', '" + res.message() + "'",
                    201, res.code());
            discoUri = res.body().string();
        }

        LOG.trace("** Deposited DiSCO with URI {}", discoUri);

        LOG.trace("** Checking ACTIVE document count for progenitor lineage {} ...", discoUri);

        // Verify that the DiSCO is indexed, inferred by the increase in the number of active Solr documents in the
        // index for the lineage
        Condition<Long> activeCountCondition = new Condition<>(() -> {
            long activeCount = discoRepository
                    .findDiscoSolrDocumentsByEventLineageProgenitorUriAndDiscoStatus(discoUri, "ACTIVE")
                    .size();
            LOG.trace("Current ACTIVE document count for lineage {}: {}", discoUri, activeCount);
            return activeCount;
        }, "Count of ACTIVE documents for lineage " + discoUri);

        assertTrue(activeCountCondition.awaitAndVerify((currentActiveCount) -> {
            LOG.trace("Verifying that the number docs with ACTIVE status for lineage {} is greater than 0", discoUri);
            return currentActiveCount > 0;
        }));

        LOG.trace("** Deleting DiSCO {} ...", discoUri);

        // Delete the DiSCO that was just deposited.  Expect that the documents present in the index for this DiSCO be
        // removed.
        String delUrl = url + "/" + URLEncoder.encode(discoUri, "UTF-8");
        try (Response res =
                     http.newCall(new Request.Builder()
                             .delete()
                             .url(delUrl).addHeader("Authorization", "Basic " + encodeAuthCreds(accessKey, secret))
                             .build())
                             .execute()) {
            assertEquals(url.toString() + " failed with: '" + res.code() + "', '" + res.message() + "'",
                    200, res.code());
        }

        LOG.trace("** HTTP DELETE {} returned with 200 (deleted DiSCO {})", delUrl, discoUri);

        LOG.trace("** Checking ACTIVE document count progenitor lineage {} ...", discoUri);

        // Verify that the number of active Solr documents in the index for the lineage is 0; i.e. the documents that
        // were added when the DiSCO was indexed have been deleted when the DELETE HTTP request was received.
        assertTrue(activeCountCondition.awaitAndVerify((currentActiveCount) -> {
            LOG.trace("Verifying that the number docs with ACTIVE status for lineage {} is 0", discoUri);
            return currentActiveCount == 0;
        }));
    }

}
