package info.rmapproject.indexing.kafka;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static info.rmapproject.indexing.IndexUtils.assertNotNull;
import static info.rmapproject.indexing.IndexUtils.assertNotNullOrEmpty;
import static info.rmapproject.indexing.IndexUtils.assertPositive;
import static info.rmapproject.indexing.IndexUtils.iae;
import static info.rmapproject.indexing.IndexUtils.ise;

/**
 * @param <T> bean class
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class SpringAwareConsumerInitializer<T> implements ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(SpringAwareConsumerInitializer.class);

    private ApplicationContext appCtx;

    private String consumerBeanName;

    private String kafkaTopic;

    private String brokerBootstrapServers;

    private int consumerCount;

    private ExecutorService consumerThreads;

    private ExecutorService conditionExecutorSvc = Executors.newCachedThreadPool();

    private Set<Condition> conditions;

    private String solrUrl;

    private String solrCoreName;

    public SpringAwareConsumerInitializer(String solrUrl, String solrCoreName) {
        this.solrUrl = assertNotNullOrEmpty(solrUrl, "Solr URL must not be null or empty");
        this.solrCoreName = assertNotNull(solrCoreName, "Solr Core name must not be null or empty");
    }

    private void createConditions() {
        assertNotNullOrEmpty(solrCoreName, ise("Solr core name must not be empty or null."));
        assertNotNullOrEmpty(solrUrl, ise("Solr URL must not be empty or null."));
        assertNotNullOrEmpty(getBrokerBootstrapServers(), ise("Kafka bootstrap.servers must not be null or empty."));
        OkHttpClient httpClient = new OkHttpClient();
        // http://localhost:8983/solr/<core-name>/admin/ping
        Request req = new Request.Builder().get().url(String.format("%s/%s/admin/ping", solrUrl, solrCoreName)).build();

        Condition<Integer> solrStatus = new Condition<>(
                () -> {
                    try (Response response = httpClient.newCall(req).execute()) {
                        int status = response.code();
                        if (status == 200) {
                            LOG.trace("Successfully pinged Solr core {} (HTTP response code {})", req.url().toString(), status);
                        } else {
                            LOG.trace("Failed to ping Solr core {} (HTTP response code {})", req.url().toString(), status);
                        }
                        return status;
                    } catch (Exception e) {
                        LOG.trace("Failed to ping Solr core {}: {}", req.url().toString(), e.getMessage(), e);
                        throw new RuntimeException(e.getMessage(), e);
                    }
                }, (status) -> status == 200, "Ping Solr Core " + req.url());

        Condition<Boolean> kafkaBoostrapServersStatus = new Condition<>(
                () -> {
                    assertNotNullOrEmpty(getBrokerBootstrapServers(), ise("Kafka bootstrap.servers parameter was null or empty.  Was " + SpringAwareConsumerInitializer.class.getSimpleName() + ".setBrokerBootstrapServers(...) invoked?"));
                    String[] bootstrapServer = getBrokerBootstrapServers().split(",");
                    // at least one bootstrap server must respond
                    for (int i = 0; i < bootstrapServer.length; i++) {
                        String host = bootstrapServer[i].split(":")[0];
                        int port = Integer.parseInt(bootstrapServer[i].split(":")[1]);
                        InetSocketAddress addr = null;
                        if (host.split(".").length == 4) {
                            addr = new InetSocketAddress(InetAddress.getByAddress(host.getBytes()), port);
                        } else {
                            addr = new InetSocketAddress(host, port);
                        }

                        try (Socket s = new Socket()) {
                            s.connect(addr, 5000);
                            LOG.trace("Successfully connected to Kafka broker {}:{}", host, port);
                            return true;
                        } catch (IOException e) {
                            // do nothing
                        }
                    }

                    LOG.trace("Failed to connect to Kafka broker(s) {}", getBrokerBootstrapServers());
                    return false; // no socket successfully connected
            }, (result) -> result, "Kafka broker '" + getBrokerBootstrapServers() + "'");

        conditions = new HashSet<>();
        conditions.add(solrStatus);
        conditions.add(kafkaBoostrapServersStatus);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.appCtx = assertNotNull(applicationContext, "Spring Application Context must not be null.");
    }

    public ApplicationContext getApplicationContext() {
        return appCtx;
    }

    public String getConsumerBeanName() {
        return consumerBeanName;
    }

    public void setConsumerBeanName(String consumerBeanName) {
        this.consumerBeanName = consumerBeanName;
    }

    public int getConsumerCount() {
        return consumerCount;
    }

    public void setConsumerCount(int consumerCount) {
        this.consumerCount = assertPositive(consumerCount, iae("Number of consumers must be a positive integer."));
    }

    public String getKafkaTopic() {
        return kafkaTopic;
    }

    public void setKafkaTopic(String kafkaTopic) {
        this.kafkaTopic = kafkaTopic;
    }

    public ExecutorService getConsumerThreads() {
        return consumerThreads;
    }

    public void setConsumerThreads(ExecutorService consumerThreads) {
        this.consumerThreads = consumerThreads;
    }

    public String getBrokerBootstrapServers() {
        return brokerBootstrapServers;
    }

    public void setBrokerBootstrapServers(String brokerBootstrapServers) {
        this.brokerBootstrapServers = brokerBootstrapServers;
    }

    public void start() {
        LOG.info("Beginning Kafka consumer startup procedure for topic '{}' using Kafka bootstrap server(s) {}", kafkaTopic, brokerBootstrapServers);

        LOG.info("Waiting for startup conditions to be satisfied.");

        if (!waitForConditions()) {
            LOG.error("Conditions for Kafka consumer startup were not satisfied; aborting consumer startup procedure.");
            return;
        }

        // Configure consumers thread pool, one thread per consumer
        if (this.consumerThreads == null) {
            this.consumerThreads = Executors.newFixedThreadPool(consumerCount);
        }

        LOG.info("Starting {} consumers ...", consumerCount);

        try {
            startConsumers();
        } catch (Exception e) {
            LOG.error("Unable to start Kafka consumers; aborting consumer startup procedure: {}", e.getMessage(), e);
            this.consumerThreads.shutdownNow();
            return;
        }

        LOG.info("Kafka consumer startup complete.  Started {} consumers for topic '{}', bootstrapped by Kafka broker(s) {}", consumerCount, kafkaTopic, brokerBootstrapServers);
    }

    private void startConsumers() {
        // Start each consumer
        for (int i = 0; i < consumerCount; i++) {
            IndexingConsumer consumer = appCtx.getBean(consumerBeanName, IndexingConsumer.class);
            LOG.info("Starting consumer {}", i);
            consumerThreads.submit(() -> {
                try {
                    consumer.consume(kafkaTopic, Seek.LATEST);
                } catch (UnknownOffsetException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            });
        }
    }

    private boolean waitForConditions() {
        // Create the conditions (TODO: inject)
        createConditions();

        // Block for conditions
        AtomicBoolean conditionsSatisfied = new AtomicBoolean(true);
        conditions.forEach(Condition::submit);
        conditions.forEach(c -> {
            boolean result = c.verify();
            conditionsSatisfied.compareAndSet(true, result);
            LOG.info("Condition {} {}", c.getName(), (result ? "satisfied." : "NOT satisfied."));
        });

        try {
            conditionExecutorSvc.shutdown();
            conditionExecutorSvc.awaitTermination(60000, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOG.warn("Interrupted while awaiting shutdown of Conditions thread pool; " +
                    "exiting without starting any consumers.");
            return false;
        }

        return conditionsSatisfied.get();
    }
}
