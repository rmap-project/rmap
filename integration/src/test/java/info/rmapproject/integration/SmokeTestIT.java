package info.rmapproject.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.logging.Level;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import info.rmapproject.indexing.kafka.Condition;
import info.rmapproject.indexing.solr.repository.DiscoRepository;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
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
public class SmokeTestIT {

    private static final Logger LOG = LoggerFactory.getLogger(SmokeTestIT.class);

    private static String scheme = "http";

    private static String host = "localhost";

    private static String port = System.getProperty("rmap.webapp.test.port");

    private static String webappCtxPath = System.getProperty("rmap.webapp.context");

    private static String apiCtxPath = System.getProperty("rmap.api.context");

    private static URL apiBaseUrl;

    private static URL appBaseUrl;

    @Autowired
    private OkHttpClient http;

    @Autowired
    private DataSource ds;

    @Autowired
    private DiscoRepository discoRepository;

    @BeforeClass
    public static void setUpBaseUrls() throws Exception {
        java.util.logging.Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);
        assertNotNull("System property 'rmap.webapp.test.port' must be specified.", port);
        assertTrue("System property 'rmap.webapp.test.port' must be an integer greater than 0",
                Integer.parseInt(port) > 0);
        assertNotNull("System property 'rmap.webapp.context' must be specified.", webappCtxPath);
        assertNotNull("System property 'rmap.api.context' must be specified.", apiCtxPath);
        apiBaseUrl = new URL(scheme, host, Integer.parseInt(port), apiCtxPath);
        appBaseUrl = new URL(scheme, host, Integer.parseInt(port), webappCtxPath);
    }

    /**
     * The RMap API webapp should return a 200, indicating successful startup.
     *
     * @throws IOException
     */
    @Test
    public void testApi200Ok() throws IOException {
        URL url = new URL(apiBaseUrl, apiCtxPath + "/discos");
        Response res = http.newCall(new Request.Builder().get().url(url).build()).execute();
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
        Response res = http.newCall(new Request.Builder().get().url(appBaseUrl).build()).execute();
        assertEquals(appBaseUrl.toString() + " failed with: '" + res.code() + "', '" + res.message() + "'",
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
        String sampleDisco = IOUtils.toString(this.getClass().getResourceAsStream("/discos/discoA.ttl"));
        String sampleDisco2 = IOUtils.toString(this.getClass().getResourceAsStream("/discos/discoB_v1.rdf"));

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

    private static String encodeAuthCreds(String accessKey, String secret) {
        return Base64.getEncoder().encodeToString(String.valueOf(accessKey + ":" + secret).getBytes());
    }

    /**
     * The user creates a new DiSCO, once indexed they then delete it. The index should remove the DiSCO content and
     * display as deleted.
     *
     * @throws IOException
     */
    @Test
    public void testCreateDeleteDiSCOWithIndexer() throws IOException, InterruptedException {

        // Bypass the RMap webapps, and directly ask Solr how many ACTIVE documents are in the index at the start of
        // this test.
        long initialActiveDocumentCount = discoRepository.findDiscoSolrDocumentsByDiscoStatus("ACTIVE").size();

        // Re-usable logic which asks Solr how many ACTIVE documents are in the index.
        Condition<Long> activeCountCondition = new Condition<>(() -> {
            long activeCount = discoRepository.findDiscoSolrDocumentsByDiscoStatus("ACTIVE").size();
            LOG.trace("Current ACTIVE document count: {}", activeCount);
            return activeCount;
        }, "Count of ACTIVE documents.");

        String accessKey = "uah2CKDaBsEw3cEQ";
        String secret = "NSbdzctrP46ZvhTi";
        URL url = new URL(apiBaseUrl, apiCtxPath + "/discos");
        String sampleDisco = IOUtils.toString(this.getClass().getResourceAsStream("/discos/discoA.ttl"));
        String discoUri;

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

        // Verify that the DiSCO is indexed, inferred by the increase in the number of active Solr documents in the
        // index
        assertTrue(activeCountCondition.awaitAndVerify((currentActiveCount) -> {
            LOG.trace("Verifying that the number of current docs ({}) with ACTIVE status is greater than the initial " +
                    "count of documents with ACTIVE status ({})", currentActiveCount, initialActiveDocumentCount);
            return currentActiveCount > initialActiveDocumentCount;
        }));

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

        // Verify that the number of active Solr documents in the index is now equal to the number of documents present
        // when the test started; i.e. the documents that were added when the DiSCO was indexed have been deleted when
        // the DELETE HTTP request was received.
        assertTrue(activeCountCondition.awaitAndVerify((currentActiveCount) -> {
            LOG.trace("Verifying that the count of current ACTIVE docs ({}) is the same as the number of ACTIVE " +
                    "documents before this test started ({}).", currentActiveCount, initialActiveDocumentCount);
            return (currentActiveCount == initialActiveDocumentCount);
        }));
    }

}
