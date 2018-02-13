package info.rmapproject.core.idservice;

import edu.ucsb.nceas.ezid.EZIDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;

import static java.lang.String.format;
import static org.joda.time.DateTime.now;
import static org.joda.time.format.ISODateTimeFormat.basicDateTimeNoMillis;

/**
 * Mints identifiers from the EZID service, and places them in a {@link ConcurrentMap cache}. The cache is shared
 * between this replenisher and the {@link ConcurrentCachingIdService}.  Access to the cache is mediated by a shared
 * {@link Lock}.  When this replenisher starts, it examines the cache.  If the cache is empty, it fills it, and signals
 * the id service that the cache has been filled.  After filling the cache, the replenisher waits until signalled by the
 * id service to re-fill the cache.
 * <h3>Configuration</h3>
 * <ul>
 *     <li>{@link #setLockHolder(LockHolder)}: a {@link LockHolder} must be set prior to invoking
 *         {@link #replenish(ConcurrentMap)}</li>
 * </ul>
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

    /**
     * Basic metadata attached to each ID minted by this replenisher
     * TODO: replace hard-coded metadata with more flexible, pluggable metadata generation
     *
     * @see <a href="https://ezid.cdlib.org/doc/apidoc.html#operation-get-identifier-metadata">obtaining metadata</a>
     * @see <a href="https://ezid.cdlib.org/doc/apidoc.html#profile-erc">erc metadata profile</a>
     */
    static final HashMap<String, String> ID_METADATA = new HashMap<String, String>() {
        {
            put("_profile", "erc");
            put("erc.who", "RMap Project (http://rmap-project.info/)");
            put("erc.what", "RDF Resource in RMap");
            put("erc.when", basicDateTimeNoMillis().withZoneUTC().print(now()));
        }
    };

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

    /**
     * Contains the locks and conditions used to communicate with {@link ConcurrentCachingIdService}.  This instance is
     * shared with a {@code ConcurrentCachingIdService}.
     */
    private LockHolder lockHolder;

    /**
     * Instantiates a replenisher that will mint new identifiers from the supplied {@code serviceUrl} using the supplied
     * {@code ezidClient}.  Note that this class will manage logging in and out of the client, and shutting down the
     * client when it is no longer needed.
     *
     * @param serviceUrl the EZID service url the {@code ezidClient} is configured to talk to
     * @param ezidClient the EZID client, pre-configured by the caller to talk to {@code serviceUrl}
     */
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

    /**
     * The lock and conditions used to communicate with {@link ConcurrentCachingIdService}.  This {@code lockHolder}
     * should be shared with the id service.
     *
     * @return the lock holder, shared with a {@link ConcurrentCachingIdService}
     */
    public LockHolder getLockHolder() {
        return lockHolder;
    }

    /**
     * The lock and conditions used to communicate with {@link ConcurrentCachingIdService}.  This {@code lockHolder}
     * should be shared with the id service.
     *
     * @param lockHolder the lock holder, shared with a {@link ConcurrentCachingIdService}
     */
    public void setLockHolder(LockHolder lockHolder) {
        this.lockHolder = lockHolder;
    }

    /**
     * Populate the supplied {@code ConcurrentMap} by minting new identifiers and placing them in the cache.  The size
     * of the map should not grow beyond its {@link #getMaxStoreSize() maximum store size}.  The supplied {@code
     * ConcurrentMap} may be empty, full, or partially full.
     * <p>
     * If errors are encountered when communicating with the EZID service, this method will retry according to the
     * supplied {@link #getRetryParams() retry parameters}.
     * </p>
     * @param ezids the cache of EZIDs to populate
     * @see Retry
     */
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
                        // TODO: fix hard-coded metadata
                        String id = ezidClient.mintIdentifier(idPrefix, ID_METADATA);
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
    public static class Retry {
        long maxWaitTimeMs = 60 * 1000;    // 60 sec

        long initialWaitTimeMs = 5 * 1000; // 5 sec

        float backOffFactor = 1.2f;

        int maxRetryAttempts = 10;
    }



}
