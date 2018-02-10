package info.rmapproject.core.idservice;

import org.h2.mvstore.MVStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Integer.toHexString;
import static java.lang.String.format;
import static java.lang.System.identityHashCode;

/**
 * An identifier service that manages the creation, allocation, and persistence of identifiers between pairs of
 * {@link ConcurrentCachingIdService} and {@link ConcurrentEzidReplenisher}.
 * <p>
 * <h3>Shared state</h3>
 * A {@code ConcurrentCachingIdService} must be paired with a {@code ConcurrentEzidReplenisher}.  This id service will
 * instantiate an id service/replenisher pair, and then properly configure the state shared between the pair.  Each pair
 * will have a lock and identifier cache distinct from other pairs.
 * </p>
 * <p>
 * <h3>Concurrent allocation</h3>
 * This id service allocates identifiers from a configured number of threads (by default it uses one thread per
 * {@link Runtime#availableProcessors() available processor}, but can be {@link #ConcurrentArkIdService(int)
 * overridden}).  This way any number of {@code ConcurrentCachingIdService} instances can be allocating identifiers,
 * while being replenished in the background.
 * </p>
 * <p>
 * <h3>Persistence</h3>
 * This id service manages the persistent store that backs the identifier cache.  This includes allocating and opening
 * a persistent store for each id service/replenisher pair, committing data to the store periodically, and properly
 * closing the store when this identifier service is shut down.
 * </p>
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class ConcurrentArkIdService implements IdService, ApplicationContextAware, InitializingBean, DisposableBean {

    /**
     * Our logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ConcurrentArkIdService.class);

    /**
     * Maintains a count of the number of threads allocated, simply used to provide a unique name to each newly created
     * thread.
     */
    private static final AtomicInteger REPLENISHER_THREAD_NO = new AtomicInteger(0);

    /**
     * Maintains a count of the number of threads allocated, simply used to provide a unique name to each newly created
     * thread.
     */
    private static final AtomicInteger IDSERVICE_THREAD_NO = new AtomicInteger(0);

    /**
     * The Spring Bean name used to retrieve a prototype {@link ConcurrentCachingIdService}
     */
    private static final String HTTP_ARK_ID_SERVICE_BEAN_NAME = "httpArkIdService";

    /**
     * The Spring Bean name used to retrieve a prototype {@link LockHolder}
     */
    private static final String LOCK_HOLDER_BEAN_NAME = "lockHolder";

    /**
     * The Spring Bean name used to retrieve a prototype {@link ConcurrentEzidReplenisher}
     */
    private static final String REPLENISHER_BEAN_NAME = "replenisher";

    /**
     * Spring Application Context used to allocate instances of collaborating objects
     */
    private ApplicationContext appCtx;

    /**
     * FIXME: Hack used to avoid initializing the {@code MVStore} for the RMAP HTML UI
     */
    private final static String RMAP_WEBAPP_SPRING_CONTEXT_ID = "/app/appServlet";

    /**
     * The name for the ID cache map
     */
    private static final String ID_CACHE_NAME = "ezidData";

    /**
     * The number of idservice/replenisher threads to start, defaults to the number of processors on the host
     */
    private int numThreads = Runtime.getRuntime().availableProcessors();

    /**
     * Executor service for managing the id service worker threads
     */
    private ExecutorService idServiceExecutor;

    /**
     * Executor service for managing the replenisher worker threads
     */
    private ExecutorService replenisherExecutor;

    /**
     * Used to provide a simple pooling mechanism for delegate id services (i.e. the pool of {@link
     * ConcurrentCachingIdService} instances)
     */
    private BlockingQueue<ConcurrentCachingIdService> serviceQueue;

    /**
     * Maintains references to the persistent {@link MVStore} instances, one instance per idservice/replenisher pair.
     */
    private Set<MVStore> openIdStores = new HashSet<>();

    /**
     * File for persistent ID storage, appended with an id to make it unique at runtime.
     */
    private String idStoreFilePrefix;

    /**
     * Instantiates this service with the number of threads equal to {@link Runtime#availableProcessors()}.
     */
    public ConcurrentArkIdService() {
        idServiceExecutor = Executors.newFixedThreadPool(numThreads,
                (runnable) -> new Thread(runnable, format("ID Service (instance %s)", IDSERVICE_THREAD_NO.getAndIncrement())));
        replenisherExecutor = Executors.newFixedThreadPool(numThreads,
                (runnable) -> new Thread(runnable, format("ID Replenisher (instance %s)", REPLENISHER_THREAD_NO.getAndIncrement())));
        serviceQueue = new ArrayBlockingQueue<>(numThreads);
    }

    /**
     * Instantiates this service with the specified number of threads.
     *
     * @param numThreads the number of threads
     */
    public ConcurrentArkIdService(int numThreads) {
        if (numThreads < 1) {
            throw new IllegalArgumentException("numThreads must be a positive integer.");
        }
        this.numThreads = numThreads;

        idServiceExecutor = Executors.newFixedThreadPool(numThreads,
                (runnable) -> new Thread(runnable, format("ID Service (instance %s)", IDSERVICE_THREAD_NO.getAndIncrement())));
        replenisherExecutor = Executors.newFixedThreadPool(numThreads,
                (runnable) -> new Thread(runnable, format("ID Replenisher (instance %s)", REPLENISHER_THREAD_NO.getAndIncrement())));
        serviceQueue = new ArrayBlockingQueue<>(numThreads);
    }

    /**
     * Allocates an identifier by taking an identifier service from the pool, and submitting it to a worker thread for
     * completion.
     *
     * @return the identifier
     * @throws Exception if the id could not be allocated
     */
    @Override
    public URI createId() throws Exception {
        LOG.debug("Taking an ID service from the pool.");
        ConcurrentCachingIdService service = serviceQueue.take();
        LOG.debug("Obtained ID service {}", serviceName(service));
        try {
            Future<URI> id = idServiceExecutor.submit(() -> {
            try {
                    LOG.debug("{} creating an ID", serviceName(service));
                    return service.createId();
                } catch (Exception e) {
                    LOG.error("{} unable to obtain ID: {}", serviceName(service), e.getMessage(), e);
                }
                return null;
            });
            return id.get();
        } finally {
            LOG.debug("Put ID service {}", serviceName(service));
            serviceQueue.put(service);
        }
    }

    @Override
    public boolean isValidId(URI id) throws Exception {
        LOG.debug("Taking an ID service from the pool.");
        ConcurrentCachingIdService service = serviceQueue.take();
        LOG.debug("Obtained ID service {}", serviceName(service));
        try {
            Future<Boolean> isValid = idServiceExecutor.submit(() -> {
                try {
                    LOG.debug("{} executing isValid", serviceName(service));
                    return service.isValidId(id);
                } catch (Exception e) {
                    LOG.error("Unable to obtain ID: {}", e.getMessage(), e);
                }
                return false;
            });
            return isValid.get();
        } finally {
            LOG.debug("Put ID service {}", serviceName(service));
            serviceQueue.put(service);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext appCtx) throws BeansException {
        this.appCtx = appCtx;
    }

    /**
     * Provides for an orderly shutdown of resources: (1) shut down and terminate worker thread pools and (2) commit
     * and close the underlying persistence store for the id caches.
     *
     * @throws Exception
     */
    @Override
    public void destroy() throws Exception {

        if (!isApi(appCtx)) {
            LOG.debug("DESTROY - Skipping destroy: {} is not being deployed by the RMap API", serviceName(this));
            return;
        }

        // Shut down the replenisherExecutor

        LOG.debug("DESTROY - Shutting down the replenisher executor.");
        replenisherExecutor.shutdown();
        LOG.debug("DESTROY - Awaiting replenisher executor termination for 30 seconds");
        replenisherExecutor.awaitTermination(30, TimeUnit.SECONDS);

        // Shut down the idServiceExecutor

        LOG.debug("DESTROY - Shutting down the idServiceExecutor executor.");
        idServiceExecutor.shutdown();
        LOG.debug("DESTROY - Awaiting id service executor termination for 30 seconds");
        idServiceExecutor.awaitTermination(30, TimeUnit.SECONDS);

        // Close the MVStore for each replenisher

        LOG.debug("DESTROY - Closing MVStores.");
        openIdStores.forEach((store) -> {
            try {
                store.commit();
                store.close();
            } catch (Exception e) {
                LOG.warn("Error closing an MVStore: " + e.getMessage(), e);
            }
        });

    }

    /**
     * Provides for the initialization of resources by (1) creating the state shared between an id service/replenisher
     * pair, (2) creating and configuring an id service/replenisher pair, (3) opening the underlying persistence store
     * for each pair, (5) starting replenisher threads, and (6) populating the pool of id services used by
     * {@link #createId()}.
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {

        if (!isApi(appCtx)) {
            LOG.debug("INIT - Skipping initialization: {} is not being deployed by the RMap API", serviceName(this));
            return;
        }

        // Every ConcurrentCachingIdService is paired with a ConcurrentEzidReplenisher.  The id service and replenisher share
        // access to a ConcurrentMap that contains a cache of identifiers.  Access to the map is mediated by the objects
        // in the LockHolder.  This means that:
        //   - each id service/replenisher pair shares an instance of LockStore
        //   - each id service/replenisher pair shares an instance of a ConcurrentMap
        //
        // The id services will be launched in one thread pool
        // The replenishers launched in another

        // For each {id service, replenisher} pair to be constructed:

        for (int i = 0; i < numThreads; i++) {

            // 1. Obtain a LockHolder instance that will be shared between the pair
            LockHolder lockHolder = appCtx.getBean(LOCK_HOLDER_BEAN_NAME, LockHolder.class);
            LOG.debug("INIT - Obtained {}", serviceName(lockHolder));

            // 2. Obtain the id service and replenisher, and set the lock holder on them
            ConcurrentCachingIdService idService = appCtx.getBean(HTTP_ARK_ID_SERVICE_BEAN_NAME, ConcurrentCachingIdService.class);
            LOG.debug("INIT - Obtained {}", serviceName(idService));
            ConcurrentEzidReplenisher replenisher = appCtx.getBean(REPLENISHER_BEAN_NAME, ConcurrentEzidReplenisher.class);
            LOG.debug("INIT - Obtained {}", serviceName(replenisher));
            idService.setLockHolder(lockHolder);
            replenisher.setLockHolder(lockHolder);
            LOG.debug("INIT - Set {} on {} and {}", serviceName(lockHolder), serviceName(idService), serviceName(replenisher));

            // 3. Open the MVStore that backs the id cache for this pair, and open the cache.
            String fileName = idStoreFilePrefix + i;
            MVStore idStore = MVStore.open(fileName);
            ConcurrentMap<Integer, String> idCache = idStore.openMap(ID_CACHE_NAME);
            openIdStores.add(idStore);
            LOG.debug("INIT - Opened {} from {}", serviceName(idStore), fileName);

            // 4. Set the cache on the id service (the same cache will be provided to the replenisher on
            //    thread construction)
            idService.setIdCache(idCache);

            // 5. Start the replenisher thread (and set the cache)
            LOG.debug("INIT - Submitting replenisher {} to the executor", serviceName(replenisher));
            replenisherExecutor.submit(() -> replenisher.replenish(idCache));

            // 6. Add the id service to the queue/pool of available id services
            LOG.debug("INIT - Adding id service {} to the service queue", serviceName(idService));
            serviceQueue.put(idService);
        }
    }

    public String getIdStoreFilePrefix() {
        return idStoreFilePrefix;
    }

    public void setIdStoreFilePrefix(String idStoreFilePrefix) {
        this.idStoreFilePrefix = idStoreFilePrefix;
    }

    private static String serviceName(Object o) {
        return format("%s@%s", o.getClass().getSimpleName(), toHexString(identityHashCode(o)));
    }

    private static boolean isApi(ApplicationContext appCtx) {
        return !appCtx.getId().contains(RMAP_WEBAPP_SPRING_CONTEXT_ID);
    }
}
