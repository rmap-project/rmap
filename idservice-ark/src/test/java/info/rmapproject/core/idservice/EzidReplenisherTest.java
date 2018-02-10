package info.rmapproject.core.idservice;

import edu.ucsb.nceas.ezid.EZIDClient;
import edu.ucsb.nceas.ezid.EZIDException;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static info.rmapproject.core.idservice.EzidTestUtil.randomString;
import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class EzidReplenisherTest {

    private static final String SERVICE_URL = "http://example.com/idservice";

    private static final String ID_TEMPLATE = EzidReplenisher.DEFAULT_PREFIX + "%s";

    private ConcurrentMap<Integer, String> idStore;

    private EZIDClient client;

    private EzidReplenisher underTest;

    @Before
    public void setUp() throws Exception {
        idStore = new ConcurrentHashMap<>();
        client = mock(EZIDClient.class);
        when(client.mintIdentifier(anyString(), any()))
                .thenReturn(format(ID_TEMPLATE, randomString(5)));
        underTest = new EzidReplenisher(SERVICE_URL, client);
    }

    @Test
    public void mintOneId() throws Exception {
        underTest.setMaxStoreSize(1);

        underTest.replenish(idStore);

        assertEquals(1, idStore.size());
    }

    @Test
    public void mintTenIds() throws Exception {
        underTest.setMaxStoreSize(10);

        underTest.replenish(idStore);

        assertEquals(10, idStore.size());
    }

    @Test
    public void mintAlreadyFull() throws Exception {
        String value = randomString(5);
        underTest.setMaxStoreSize(1);
        idStore.put(1, value);
        underTest.replenish(idStore);
        assertEquals(1, idStore.size());
        assertEquals(value, idStore.get(1));
    }

    @Test
    public void mintOverCapacity() throws Exception {
        String valueOne = randomString(5);
        String valueTwo = randomString(5);
        underTest.setMaxStoreSize(1);
        idStore.put(1, valueOne);
        idStore.put(2, valueTwo);
        underTest.replenish(idStore);
        assertEquals(2, idStore.size());
        assertEquals(valueOne, idStore.get(1));
        assertEquals(valueTwo, idStore.get(2));
    }

    @Test
    public void defaultStoreSize() throws Exception {
        underTest.setMaxStoreSize(EzidReplenisher.DEFAULT_STORE_SIZE);
    }

    @Test
    public void defaultRetryParams() throws Exception {
        underTest.setRetryParams(EzidReplenisher.DEFAULT_RETRY_PARAMS);
    }

    @Test
    public void mintMaxStoreSize() throws Exception {
        underTest.replenish(idStore);

        assertEquals(underTest.getMaxStoreSize(), idStore.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void mintZeroIds() throws Exception {
        underTest.setMaxStoreSize(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mintNegativeIds() throws Exception {
        underTest.setMaxStoreSize(-1);
    }

    @Test
    public void exceedMaxWait() throws Exception {
        EzidReplenisher.Retry retryParams = new EzidReplenisher.Retry();
        retryParams.maxWaitTimeMs = 2;
        retryParams.initialWaitTimeMs = 0;
        retryParams.maxRetryAttempts = 9999;
        retryParams.backOffFactor = 1.0f;

        client = mock(EZIDClient.class);
        when(client.mintIdentifier(anyString(), any()))
                .thenAnswer(inv -> {
                    Thread.sleep(retryParams.maxWaitTimeMs * 2);
                    throw new EZIDException("exceedMaxWait() exception.");
                });

        EzidReplenisher underTest = new EzidReplenisher(SERVICE_URL, client);
        underTest.setRetryParams(retryParams);

        underTest.replenish(idStore);
        assertEquals(0, idStore.size());
        verify(client).shutdown();
    }

    @Test
    public void exceedMaxAttempts() throws Exception {
        EzidReplenisher.Retry retryParams = new EzidReplenisher.Retry();
        retryParams.maxWaitTimeMs = 10000;
        retryParams.initialWaitTimeMs = 0;
        retryParams.maxRetryAttempts = 1;
        retryParams.backOffFactor = 1.0f;

        client = mock(EZIDClient.class);
        when(client.mintIdentifier(anyString(), any()))
                .thenAnswer(inv -> {
                    throw new EZIDException("exceedMaxAttempts() exception.");
                });

        EzidReplenisher underTest = new EzidReplenisher(SERVICE_URL, client);
        underTest.setRetryParams(retryParams);

        underTest.replenish(idStore);
        assertEquals(0, idStore.size());
        verify(client).shutdown();
    }

    @Test
    public void executesAtLeastOnceWhenConfiguredNotToRetry() throws Exception {
        underTest.setMaxStoreSize(1);
        underTest.setRetryParams(noRetries());

        underTest.replenish(idStore);

        assertEquals(1, idStore.size());
    }

    /**
     * If a null identifier is minted, it is <em>not</em> added to the idstore by the replenisher.
     * @throws Exception
     */
    @Test
    public void mintNullId() throws Exception {
        client = mock(EZIDClient.class);
        when(client.mintIdentifier(anyString(), any())).thenReturn(null);

        EzidReplenisher underTest = new EzidReplenisher(SERVICE_URL, client);
        underTest.setMaxStoreSize(1);
        underTest.setRetryParams(noRetries());

        underTest.replenish(idStore);

        assertEquals(0, idStore.size());
        assertNull(idStore.get(1));
    }

    private static EzidReplenisher.Retry noRetries() {
        EzidReplenisher.Retry retryParams = new EzidReplenisher.Retry();
        retryParams.maxWaitTimeMs = 0;
        retryParams.initialWaitTimeMs = 0;
        retryParams.maxRetryAttempts = 0;
        retryParams.backOffFactor = 1.0f;
        return retryParams;
    }
}