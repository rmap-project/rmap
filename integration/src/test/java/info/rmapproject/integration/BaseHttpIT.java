package info.rmapproject.integration;

import info.rmapproject.indexing.solr.repository.DiscoRepository;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.logging.Level;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Base integration test with common HTTP test fixtures.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/integration-context.xml")
public abstract class BaseHttpIT {

    static final Logger LOG = LoggerFactory.getLogger(SmokeTestIT.class);

    static String scheme = "http";

    static String host = "localhost";

    static String port = System.getProperty("rmap.webapp.test.port");

    static String webappCtxPath = System.getProperty("rmap.webapp.context");

    static String apiCtxPath = System.getProperty("rmap.api.context");

    static URL apiBaseUrl;

    static URL appBaseUrl;

    static URI discosEndpoint;

    static String accessKey = "uah2CKDaBsEw3cEQ";

    static String secret = "NSbdzctrP46ZvhTi";

    static String APPLICATION_RDFXML = "application/rdf+xml";

    @Autowired
    OkHttpClient http;

    @Autowired
    DataSource ds;

    @Autowired
    DiscoRepository discoRepository;

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
        discosEndpoint = URI.create(apiBaseUrl.toString() + "/discos");
    }

    static String encodeAuthCreds(String accessKey, String secret) {
        return Base64.getEncoder().encodeToString(String.valueOf(accessKey + ":" + secret).getBytes());
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
    String depositDisco(URL endpoint, String discoBody) throws IOException {
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
    void deleteDisco(URL endpoint) throws IOException {
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
    String decorateAndExecuteRequest(URL endpoint, Request.Builder req, int expectedStatus) throws IOException {
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

    static URL encodeDiscoUriAsUrl(String discoUri) throws MalformedURLException, UnsupportedEncodingException {
        return URI.create(discosEndpoint.toString() + "/" + URLEncoder.encode(discoUri, "UTF-8")).toURL();
    }

}
