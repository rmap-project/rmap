package info.rmapproject.core.idservice;

import edu.ucsb.nceas.ezid.EZIDClient;
import edu.ucsb.nceas.ezid.EZIDException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentMap;

import static java.lang.String.format;

/**
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class ConcurrentEzidReplenisher implements ConcurrentIdReplenisher {

    /**
     * Default prefix requested from the EZID service
     */
    static String DEFAULT_PREFIX = "ark:/99999/fk4";

    /**
     * Default number of EZIDs to mint
     */
    static int DEFAULT_STORE_SIZE = 200;

    /**
     * Will retry up to 10 times, or timeout after 60 seconds, whichever comes first.
     */
    static EzidReplenisher.Retry DEFAULT_RETRY_PARAMS = new EzidReplenisher.Retry();

    private static final Logger LOG = LoggerFactory.getLogger(EzidReplenisher.class);

    private static String ERR_MINT = "EZID service %s could not mint EZID for shoulder '%s': %s";

    /**
     * Governs how often, and for how long, attempts will be made to request IDs from the EZID service when errors occur
     */
    private EzidReplenisher.Retry retryParams = DEFAULT_RETRY_PARAMS;

    /**
     * The id prefix, or "shoulder", that all IDs will start with
     */
    private String idPrefix = DEFAULT_PREFIX;

    /**
     * The user name used to log in to the ezid service
     */
    private String userName = "apitest";

    /**
     * The password used to log in to the ezid service
     */
    private String userPassword = "apitest";

    /**
     * The maximium number of ids to mint
     */
    private int maxStoreSize = DEFAULT_STORE_SIZE;

    /**
     * A configured, but not logged in, EZID client.
     */
    private EZIDClient ezidClient;

    /**
     * The EZID service endpoint.
     */
    private String serviceUrl;

    private LockHolder lockHolder;

    public ConcurrentEzidReplenisher(String serviceUrl, EZIDClient ezidClient) {
        if (ezidClient == null) {
            throw new IllegalArgumentException("EZIDClient must not be null.");
        }
        if (serviceUrl == null || serviceUrl.trim().length() == 0) {
            throw new IllegalArgumentException("Service URL must not be null or empty.");
        }
        this.ezidClient = ezidClient;
        this.serviceUrl = serviceUrl;
    }

    public LockHolder getLockHolder() {
        return lockHolder;
    }

    public void setLockHolder(LockHolder lockHolder) {
        this.lockHolder = lockHolder;
    }

    @Override
    public void replenish(ConcurrentMap<Integer, String> ezids) {
        if (lockHolder == null) {
            throw new IllegalStateException("Missing a LockHolder (was setLockHolder(LockHolder) invoked?)");
        }

        while (true) {

            lockHolder.idStoreLock.lock();

            while (ezids.size() > 0) {
                LOG.debug("ID replenisher waiting to replenish the ID store, it is not empty: max size: {}, current size: {}",
                        maxStoreSize, ezids.size());
                try {
                    lockHolder.idStoreEmptyCondition.await();
                    LOG.debug("ID replenisher waking up to replenish the ID store.");
                } catch (InterruptedException e) {
                    LOG.warn("ID replenisher thread interrupted!  Releasing lock, and returning without replenishing " +
                            "the EZID cache.");
                    lockHolder.idStoreLock.unlock();
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            boolean success = false;
            int retryCounter = 0;
            long startTimeMs = System.currentTimeMillis();
            long maxTimeMs = startTimeMs + retryParams.maxWaitTimeMs;
            long waitTimeMs = retryParams.initialWaitTimeMs;

            do {
                retryCounter++;
                LOG.debug("ID replenisher minting ids from {} (attempt: {})", serviceUrl, retryCounter);

                try {
                    ezidClient.login(userName, userPassword);
                    for (int i = 1; ezids.size() < maxStoreSize; i++) {
                        String id = ezidClient.mintIdentifier(idPrefix, null);
                        if (id == null) {
                            LOG.error("EZID service {} minted a null id.", serviceUrl);
                        } else {
                            ezids.putIfAbsent(i, id);
                        }
                    }
                    LOG.debug("ID store has {} ids.  Signalling id service that the store is not empty.", ezids.size());
                    lockHolder.idStoreNotEmptyCondition.signal();
                    success = true;
                } catch (Exception e) {
                    success = false;
                    LOG.error(format(ERR_MINT, serviceUrl, idPrefix, e.getMessage()), e);
                } finally {
                    ezidClient.shutdown();
                }

                if (!success) {
                    try {
                        LOG.debug("ID replenisher sleeping for {} ms", waitTimeMs);
                        Thread.sleep(waitTimeMs);
                        waitTimeMs = (long) (waitTimeMs * retryParams.backOffFactor);
                    } catch (InterruptedException e) {
                        LOG.warn("ID replenisher thread interrupted!  Releasing lock and returning without " +
                                "replenishing the EZID cache.");
                        lockHolder.idStoreLock.unlock();
                        return;
                    }
                }

            } while (System.currentTimeMillis() < maxTimeMs && retryCounter < retryParams.maxRetryAttempts && !success);

            if (!success) {
                LOG.error("Unable to replenish the ID store: {}",
                        ((System.currentTimeMillis() < maxTimeMs)
                                ? "maximum retry attempts reached: " + retryParams.maxRetryAttempts
                                : "timeout limit reached: " + retryParams.maxWaitTimeMs + " ms (waited " + (System.currentTimeMillis() - startTimeMs) + " ms)"));
            }

            lockHolder.idStoreLock.unlock();
        }
    }

    public EzidReplenisher.Retry getRetryParams() {
        return retryParams;
    }

    public void setRetryParams(EzidReplenisher.Retry retryParams) {
        if (retryParams.maxWaitTimeMs < 0) {
            throw new IllegalArgumentException("Max wait time must be a 0 or greater");
        }

        if (retryParams.initialWaitTimeMs < 0) {
            throw new IllegalArgumentException("Initial wait time must be 0 or greater.");
        }

        if (retryParams.backOffFactor < 1) {
            throw new IllegalArgumentException("Backoff factor must be a float greater than or equal to 1");
        }

        if (retryParams.maxRetryAttempts < 0) {
            throw new IllegalArgumentException("Max retry attempts must be 0 or greater.");
        }

        if (retryParams.initialWaitTimeMs > retryParams.maxWaitTimeMs) {
            throw new IllegalArgumentException("Initial wait time (" + retryParams.initialWaitTimeMs + " ms) must be" +
                    " less than or equal to the maximum wait time (" + retryParams.maxWaitTimeMs + " ms)");
        }

        this.retryParams = retryParams;
    }

    public String getIdPrefix() {
        return idPrefix;
    }

    public void setIdPrefix(String idPrefix) {
        this.idPrefix = idPrefix;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public int getMaxStoreSize() {
        return maxStoreSize;
    }

    public void setMaxStoreSize(int maxStoreSize) {
        if (maxStoreSize < 1) {
            throw new IllegalArgumentException("Maximum store size must be a positive integer.");
        }
        this.maxStoreSize = maxStoreSize;
    }

    /**
     * Governs retry behavior when <em>errors occur</em> while minting identifiers.   If errors <em>do not</em> occur
     * during the minting process (i.e. {@code EZIDException} is never thrown by {@link #replenish(ConcurrentMap)}), it may take
     * as long as it needs in order to fill the {@code Map} with identifiers.
     * <p>
     * <dl>
     * <dt>maxWaitTimeMs</dt>
     * <dd>The maximum amount of time {@link #replenish(ConcurrentMap)} is allowed to execute, in millis.  If
     * {@link #replenish(ConcurrentMap)} encounters an error after executing {@code maxWaitTimeMs}, then no retries will
     * occur, because this parameter applies to the <em>total duration</em> of {@link #replenish(ConcurrentMap)}</dd>
     * <dt>maxRetryAttemps</dt>
     * <dd>The maximum number of times to retry after encountering an error.</dd>
     * <dt>initialWaitTimeMs</dt>
     * <dd>The initial amount of time to wait between retry attempts, in millis.</dd>
     * <dt>backOffFactor</dt>
     * <dd>Multiplied with {@code initialWaitTimeMs} to gradually increate the time to wait between retry
     * attempts</dd>
     * </dl>
     */
    static class Retry {
        long maxWaitTimeMs = 60 * 1000;    // 60 sec

        long initialWaitTimeMs = 5 * 1000; // 5 sec

        float backOffFactor = 1.2f;

        int maxRetryAttempts = 10;
    }

}
