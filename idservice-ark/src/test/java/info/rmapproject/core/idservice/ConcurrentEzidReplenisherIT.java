package info.rmapproject.core.idservice;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.net.URLEncoder;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.joda.time.DateTime.now;
import static org.joda.time.format.ISODateTimeFormat.basicDate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/ark-idservice.xml")
@ActiveProfiles("ark-idserviceintegration")
public class ConcurrentEzidReplenisherIT {

    @Autowired
    private ConcurrentEzidReplenisher underTest;

    @Autowired
    private LockHolder lockHolder;

    @Value("${idservice.idMinterUrl}")
    private String ezidEndpoint;

    private ConcurrentMap<Integer, String> idCache = new ConcurrentHashMap<>();

    @Before
    public void setUp() throws Exception {
        underTest.setLockHolder(lockHolder);
    }

    /**
     * Insure that a minted identifier contains expected metadata
     */
    @Test
    public void mintWithMetadata() throws Exception {
        // we only need to test one id
        underTest.setMaxStoreSize(1);

        // Start the replenisher, have it get one identifier, then terminate the thread.

        Thread t = new Thread(() -> underTest.replenish(idCache));
        t.start();
        t.interrupt();
        t.join();

        assertEquals(1, idCache.size());
        assertNotNull(idCache.values().iterator().next());

        URI id = URI.create(idCache.values().iterator().next());

        // Retrieve the metadata for the newly minted id

        String resolverUrl = String.format("%s/id/%s", ezidEndpoint, URLEncoder.encode(id.toString(), "UTF-8"));

        Response response = new OkHttpClient.Builder().build().newCall(
                new Request.Builder().url(resolverUrl).build())
                .execute();
        ResponseBody responseBody = response.body();
        assertNotNull(responseBody);
        String metadata = responseBody.string();

        assertEquals(200, response.code());

        // Verify the expected metadata

        ConcurrentEzidReplenisher.ID_METADATA.forEach((key, value) -> {
            assertTrue(metadata.contains(key));
            if (!key.equals("erc.when")) {
                assertTrue(metadata.contains(value));
            } else {
                assertTrue(metadata.contains(basicDate().print(now())));
            }
        });
    }

}