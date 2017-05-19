package info.rmapproject.integration;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class SmokeTestIT {

    private static String scheme = "http";

    private static String host = "localhost";

    private static String port = System.getProperty("rmap.webapp.test.port");

    private static String webappCtxPath = System.getProperty("rmap.webapp.context");

    private static String apiCtxPath = System.getProperty("rmap.api.context");

    private static URL apiBaseUrl;

    private static URL appBaseUrl;

    private OkHttpClient http;

    @BeforeClass
    public static void setUp() throws Exception {
        assertNotNull("System property 'rmap.webapp.test.port' must be specified.", port);

        assertTrue("System property 'rmap.webapp.test.port' must be an integer greater than 0",
                Integer.parseInt(port) > 0);

        assertNotNull("System property 'rmap.webapp.context' must be specified.", webappCtxPath);

        assertNotNull("System property 'rmap.api.context' must be specified.", apiCtxPath);

        setBaseUrls();
    }

    @Before
    public void setUpOkHttp() throws Exception {
        http = new OkHttpClient();
    }

    @Test
    public void testApi200Ok() throws Exception {
        URL url = new URL(apiBaseUrl, apiCtxPath + "/discos");
        Response res = http.newCall(new Request.Builder().get().url(url).build()).execute();
        assertEquals(url.toString() + " failed with: '" + res.code() + "', '" + res.message() + "'",
                200, res.code());
    }

    @Test
    public void testWebapp200Ok() throws Exception {
        Response res = http.newCall(new Request.Builder().get().url(appBaseUrl).build()).execute();
        assertEquals(appBaseUrl.toString() + " failed with: '" + res.code() + "', '" + res.message() + "'",
                200, res.code());
    }


    private static void setBaseUrls() throws MalformedURLException {
        apiBaseUrl = new URL(scheme, host, Integer.parseInt(port), apiCtxPath);
        appBaseUrl = new URL(scheme, host, Integer.parseInt(port), webappCtxPath);
    }

}
