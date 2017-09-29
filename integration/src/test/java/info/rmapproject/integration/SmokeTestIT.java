package info.rmapproject.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import info.rmapproject.integration.fixtures.FixtureConfig;
import info.rmapproject.spring.triplestore.support.TriplestoreManager;
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
@ContextConfiguration(classes = {FixtureConfig.class})
@TestPropertySource({"classpath:/rmap.properties", "classpath:/integration-db.properties"})
public class SmokeTestIT {

    private static String scheme = "http";

    private static String host = "localhost";

    private static String port = System.getProperty("rmap.webapp.test.port");

    private static String webappCtxPath = System.getProperty("rmap.webapp.context");

    private static String apiCtxPath = System.getProperty("rmap.api.context");

    private static URL apiBaseUrl;

    private static URL appBaseUrl;

    @Autowired
    private TriplestoreManager tsManager;

    @Autowired
    private OkHttpClient http;

    @Autowired
    private DataSource ds;

    @BeforeClass
    public static void setUpBaseUrls() throws Exception {
        Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);
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
    public void testAuthenticatedRequest() throws IOException {
        String accessKey = "uah2CKDaBsEw3cEQ";
        String secret = "NSbdzctrP46ZvhTi";
        URL url = new URL(apiBaseUrl, apiCtxPath + "/discos");
        String sampleDisco = IOUtils.toString(this.getClass().getResourceAsStream("/discos/discoA.ttl"));

        try (Response res =
                     http.newCall(new Request.Builder()
                             .post(RequestBody.create(MediaType.parse("text/turtle"), sampleDisco))
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
    }

    private static String encodeAuthCreds(String accessKey, String secret) {
        return Base64.getEncoder().encodeToString(String.valueOf(accessKey + ":" + secret).getBytes());
    }

}
