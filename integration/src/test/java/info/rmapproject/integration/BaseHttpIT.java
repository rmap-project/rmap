package info.rmapproject.integration;

import info.rmapproject.indexing.solr.repository.DiscoRepository;
import okhttp3.OkHttpClient;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URL;
import java.util.Base64;
import java.util.logging.Level;

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

}
