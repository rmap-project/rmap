package info.rmapproject.indexing.kafka;

import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.agent.RMapAgent;
import info.rmapproject.core.model.disco.RMapDiSCO;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.core.model.impl.openrdf.ORMapEventDeletion;
import info.rmapproject.core.model.impl.openrdf.ORMapEventTombstone;
import info.rmapproject.core.rmapservice.RMapService;
import info.rmapproject.indexing.IndexingInterruptedException;
import info.rmapproject.indexing.IndexingTimeoutException;
import info.rmapproject.indexing.solr.model.DiscoSolrDocument;
import info.rmapproject.indexing.solr.model.KafkaMetadata;
import info.rmapproject.indexing.solr.repository.DiscosSolrOperations;
import info.rmapproject.indexing.solr.repository.EventDiscoTuple;
import info.rmapproject.indexing.solr.repository.EventTupleIndexingRepository;
import info.rmapproject.indexing.solr.repository.IndexDTOMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static info.rmapproject.indexing.IndexUtils.EventDirection.SOURCE;
import static info.rmapproject.indexing.IndexUtils.EventDirection.TARGET;
import static info.rmapproject.indexing.IndexUtils.assertNotNull;
import static info.rmapproject.indexing.IndexUtils.assertPositive;
import static info.rmapproject.indexing.IndexUtils.findEventIri;
import static info.rmapproject.indexing.IndexUtils.iae;
import static info.rmapproject.indexing.IndexUtils.ise;
import static java.lang.System.currentTimeMillis;

/**
 * Attempts to index a {@link RMapEvent}, retrying until the operation succeeds or a timeout is exceeded.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class DefaultIndexRetryHandler implements IndexingRetryHandler, EventTupleIndexingRepository<DiscoSolrDocument> {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultIndexRetryHandler.class);

    private int indexRetryTimeoutMs;

    private float indexRetryBackoffFactor;

    private int indexRetryMaxMs;

    private EventTupleIndexingRepository<DiscoSolrDocument> indexer;

    private IndexDTOMapper dtoMapper;

    @Autowired
    private RMapService rmapService;

    private DiscosSolrOperations discosSolrOperations;

    /**
     * Retry indexing operations every {@code (indexRetryMs * indexRetryBackoffFactor)} ms, up to
     * {@code indexRetryMaxMs}.
     * Constructs a retry handler with:
     * <dl>
     *     <dt>indexRetryTimeoutMs</dt>
     *     <dd>100</dd>
     *     <dt>indexRetryMaxMs</dt>
     *     <dd>120000</dd>
     *     <dt>indexRetryBackoffFactor</dt>
     *     <dd>1.5</dd>
     * </dl>
     *
     * @param indexer the Solr repository, must not be {@code null}
     * @param dtoMapper maps {@code IndexDTO} objects to a stream of {@code EventDiscoTuple}
     * @throws IllegalArgumentException if {@code repository} is {@code null}, if any time-out related parameter is not
     *                                  1 or greater, if {@code indexRetryTimeoutMs} is greater than
     *                                  {@code indexRetryMaxMs}
     */
    public DefaultIndexRetryHandler(EventTupleIndexingRepository<DiscoSolrDocument> indexer,
                                    IndexDTOMapper dtoMapper, DiscosSolrOperations solrOperations) {
        this(indexer, dtoMapper, solrOperations, 100, 120000);
    }

    /**
     * Retry indexing operations every {@code (indexRetryMs * indexRetryBackoffFactor)} ms, up to
     * {@code indexRetryMaxMs}.
     * Constructs a retry handler with:
     * <dl>
     *     <dt>indexRetryBackoffFactor</dt>
     *     <dd>1.5</dd>
     * </dl>
     *
     * @param indexer the Solr repository, must not be {@code null}
     * @param dtoMapper maps {@code IndexDTO} objects to a stream of {@code EventDiscoTuple}
     * @param indexRetryTimeoutMs initial time to wait between retry attempts, in ms
     * @param indexRetryMaxMs  absolute amount of time to wait before timing out, in ms
     * @throws IllegalArgumentException if {@code repository} is {@code null}, if any time-out related parameter is not
     *                                  1 or greater, if {@code indexRetryTimeoutMs} is greater than
     *                                  {@code indexRetryMaxMs}
     */
     public DefaultIndexRetryHandler(EventTupleIndexingRepository<DiscoSolrDocument> indexer,
                                     IndexDTOMapper dtoMapper, DiscosSolrOperations solrOperations,
                                     int indexRetryTimeoutMs, int indexRetryMaxMs) {
        this(indexer, dtoMapper, solrOperations, indexRetryTimeoutMs, indexRetryMaxMs, 1.5F);
     }

    /**
     * Retry indexing operations every {@code (indexRetryMs * indexRetryBackoffFactor)} ms, up to
     * {@code indexRetryMaxMs}.
     *
     * @param indexer the Solr repository, must not be {@code null}
     * @param dtoMapper maps {@code IndexDTO} objects to a stream of {@code EventDiscoTuple}
     * @param indexRetryTimeoutMs initial time to wait between retry attempts, in ms
     * @param indexRetryMaxMs  absolute amount of time to wait before timing out, in ms
     * @param indexRetryBackoffFactor multiplied by the {@code indexRetryTimeoutMs} on each attempt
     * @throws IllegalArgumentException if {@code repository} is {@code null}, if any time-out related parameter is not
     *                                  1 or greater, if {@code indexRetryTimeoutMs} is greater than
     *                                  {@code indexRetryMaxMs}
     */
    public DefaultIndexRetryHandler(EventTupleIndexingRepository<DiscoSolrDocument> indexer,
                                    IndexDTOMapper dtoMapper, DiscosSolrOperations solrOperations,
                                    int indexRetryTimeoutMs, int indexRetryMaxMs, float indexRetryBackoffFactor) {
        this.indexRetryTimeoutMs = assertPositive(indexRetryTimeoutMs,
                iae("Index retry timeout must be a positive integer."));
        this.indexRetryBackoffFactor = assertPositive(indexRetryBackoffFactor,
                iae("Index retry backoff factor must be a positive float greater than one."));
        this.indexRetryMaxMs = assertPositive(indexRetryMaxMs,
                iae("Index retry max ms must be a positive integer."));
        this.indexer = assertNotNull(indexer, iae("Repository must not be null."));
        this.dtoMapper = assertNotNull(dtoMapper, iae("DTO Mapper must not be null."));
        this.discosSolrOperations = assertNotNull(solrOperations, iae("Disco Solr Operations must not be null."));

        validateRetryMaxMs(indexRetryTimeoutMs, indexRetryMaxMs,
                "Index retry max ms must be equal or greater than index retry timeout");
    }

    /**
     * Attempts to index the supplied {@code event} until the operation succeeds, or exceeds
     * {@link #getIndexRetryMaxMs()}.
     *
     * @param event the RMap event being indexed
     * @param documentDecorator decorates the solr document prior to indexing
     * @throws IndexingTimeoutException if {@code indexRetryMaxMs} is exceeded prior to successfully indexing the
     *                                  {@code event}
     * @throws IndexingInterruptedException if the thread performing the indexing is interrupted before successfully
     *                                      indexing the {@code event}
     */
    @Override
    public void retry(RMapEvent event, KafkaMetadata metadata, Consumer<DiscoSolrDocument> documentDecorator)
            throws IndexingTimeoutException, IndexingInterruptedException {

        assertNotNull(rmapService, ise("RMapService must not be null.  Was setRmapService(...) invoked?"));

        long start = currentTimeMillis();
        int attempt = 1;
        long timeout = indexRetryTimeoutMs;
        boolean success = false;
        Exception retryFailure = null;

        do {

            try {
                /*
                    Source DiSCOs of DELETION and TOMBSTONE events cannot be retrieved from the RMAP API, so they are
                    handled "specially".  There's no point to proceed in creating a KafkaDTO, or sending the DTO to the
                    indexer, because the indexer requires that the DTO be a connected graph of the event,
                    source disco, and target disco.  Because the RMAP API doesn't give out tombstone or deleted discos
                    (maybe that should be a policy implemented in the web api and not in the java api?), there's no
                    point in pursuing the creation of a connected graph: the elements of the graph can't be retrieved
                    from the API.  So just short-circuit the process here.
                */
                if (event.getEventType() == RMapEventType.DELETION || event.getEventType() == RMapEventType.TOMBSTONE) {
                    LOG.trace("Cannot index events of type {}, however, the lineage with progenitor URI {} will be " +
                            "removed from the index.", event.getEventType(), event.getLineageProgenitor());
                    discosSolrOperations.deleteDocumentsForLineage(event.getLineageProgenitor().getStringValue());
                    success = true;
                    continue;
                }

                LOG.trace("Indexing event {}, attempt {} (total elapsed time {} ms)",
                        event.getId().getStringValue(), attempt, (currentTimeMillis() - start));
                KafkaDTO dto = composeDTO(event, rmapService);

                // Store offsets in the index
                dto.setTopic(metadata.getKafkaTopic());
                dto.setPartition(metadata.getKafkaPartition());
                dto.setOffset(metadata.getKafkaOffset());

                index(dtoMapper.apply(dto), documentDecorator);
                success = true;
                LOG.info("Indexed event {} after {} attempt(s) from Kafka topic/partition/offset {}/{}/{}, " +
                                "total elapsed time {} ms",
                        event.getId().getStringValue(),
                        attempt,
                        metadata.getKafkaTopic(),
                        metadata.getKafkaPartition(),
                        metadata.getKafkaOffset(),
                        (currentTimeMillis() - start));
            } catch (Exception e) {
                retryFailure = e;
                LOG.trace("Indexing attempt {} failed for event {}: {}", attempt, event.getId().getStringValue(),
                        e.getMessage(), e);
            }

            if (!success) {
                try {
                    Thread.sleep(timeout);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                    String fmt = "Retry operation was interrupted after %s attempts, %s ms: failed to index %s";
                    String msg = String.format(fmt, attempt, (currentTimeMillis() - start), event);
                    throw new IndexingInterruptedException(msg, e);
                }
                attempt++;
                timeout = Math.round(timeout * indexRetryBackoffFactor);
            }

        } while ((currentTimeMillis() - start) < indexRetryMaxMs && !success);

        if (!success) {
            String fmt = "Timeout after %s attempts, %s ms: failed to index %s: %s";
            String msg = String.format(fmt, attempt, (currentTimeMillis() - start), event, retryFailure.getMessage());
            throw new IndexingTimeoutException(msg, retryFailure);
        }
    }

    private KafkaDTO composeDTO(RMapEvent event, RMapService rmapService) {
        LOG.trace("Composing KafkaDTO for event (id '{}', type '{}'): {}", event.getId(), event.getEventType(), event);
        RMapDiSCO sourceDisco = getDisco(findEventIri(event, SOURCE), rmapService);
        RMapDiSCO targetDisco = getDisco(findEventIri(event, TARGET), rmapService);
        RMapAgent agent = getAgent(event.getAssociatedAgent().getIri(), rmapService);

        return new KafkaDTO(event, agent, sourceDisco, targetDisco);
    }

    private static RMapDiSCO getDisco(Optional<RMapIri> optionalIri, RMapService rmapService) {
        RMapDiSCO disco = null;
        if (optionalIri.isPresent()) {
            disco = rmapService.readDiSCO(optionalIri.get().getIri());
        }

        return disco;
    }

    private static RMapAgent getAgent(URI agentUri, RMapService rmapService) {
        return rmapService.readAgent(agentUri);
    }

    @Override
    public void index(Stream<EventDiscoTuple> tupleStream) {
        assertNotNull(indexer, ise("EventTupleIndexingRepository must not be null."));
        indexer.index(tupleStream);
    }

    @Override
    public void index(Stream<EventDiscoTuple> tupleStream, Consumer<DiscoSolrDocument> decorator) {
        assertNotNull(indexer, ise("EventTupleIndexingRepository must not be null."));
        indexer.index(tupleStream, decorator);
    }

    public RMapService getRmapService() {
        return rmapService;
    }

    public void setRmapService(RMapService rmapService) {
        this.rmapService = assertNotNull(rmapService, "RMapService must not be null.");
    }

    /**
     * The amount of time in ms, combined with {@link #getIndexRetryBackoffFactor() the backoff factor}, determines
     * how long to wait between retry attempts.  For example, if the {@code indexRetryTimeoutMs} is {@code 100}, and the
     * {@code indexRetryBackoffFactor} is {@code 1.5}, the first retry attempt will occur after 100 ms, the second
     * retry attempt in (100*1.5) 150 ms, and the third retry attempt in (150*1.5) 225 ms, and so on, until
     * {@code indexRetryMaxMs} is exceeded.
     *
     * @return initial time to wait, in ms, between retry attempts (combined with {@code indexRetryBackoffFactor}), must
     *         be a positive integer and less than {@code indexRetryMaxMs}
     */
    public int getIndexRetryTimeoutMs() {
        return indexRetryTimeoutMs;
    }

    /**
     * The amount of time in ms, combined with {@link #getIndexRetryBackoffFactor() the backoff factor}, determines
     * how long to wait between retry attempts.  For example, if the {@code indexRetryTimeoutMs} is {@code 100}, and the
     * {@code indexRetryBackoffFactor} is {@code 1.5}, the first retry attempt will occur after 100 ms, the second
     * retry attempt in (100*1.5) 150 ms, and the third retry attempt in (150*1.5) 225 ms, and so on, until
     * {@code indexRetryMaxMs} is exceeded.
     *
     * @param indexRetryTimeoutMs initial time to wait, in ms, between retry attempts (combined with
     *                            {@code indexRetryBackoffFactor}), must be a positive integer and less than
     *                            {@code indexRetryMaxMs}
     */
    public void setIndexRetryTimeoutMs(int indexRetryTimeoutMs) {
        validateRetryMaxMs(indexRetryTimeoutMs, indexRetryMaxMs,
                "Retry max ms must be a positive integer and be greater than the retry timeout.");
        this.indexRetryTimeoutMs = assertPositive(indexRetryTimeoutMs,
                iae("Index retry timeout must be a positive integer."));
    }

    /**
     * A multiplier that determines the amount of time to wait between retry attempts.  For example, if the
     * {@code indexRetryTimeoutMs} is {@code 100}, and the {@code indexRetryBackoffFactor} is {@code 1.5}, the first
     * retry attempt will occur after 100 ms, the second retry attempt in (100*1.5) 150 ms, and the third retry attempt
     * in (150*1.5) 225 ms, and so on, until {@code indexRetryMaxMs} is exceeded.
     *
     * @return the multiplier which determines the amount of time to wait between retry attempts (combined with
     *         {@code indexRetryTimeoutMs}), must be a positive value greater than or equal to 1.
     */
    public float getIndexRetryBackoffFactor() {
        return indexRetryBackoffFactor;
    }

    /**
     * A multiplier that determines the amount of time to wait between retry attempts.  For example, if the
     * {@code indexRetryTimeoutMs} is {@code 100}, and the {@code indexRetryBackoffFactor} is {@code 1.5}, the first
     * retry attempt will occur after 100 ms, the second retry attempt in (100*1.5) 150 ms, and the third retry attempt
     * in (150*1.5) 225 ms, and so on, until {@code indexRetryMaxMs} is exceeded.
     *
     * @param indexRetryBackoffFactor the multiplier which determines the amount of time to wait between retry attempts
     *                                (combined with {@code indexRetryTimeoutMs}), must be a positive value greater than
     *                                or equal to 1.
     */
    public void setIndexRetryBackoffFactor(float indexRetryBackoffFactor) {
        this.indexRetryBackoffFactor = assertPositive(indexRetryTimeoutMs,
                iae("Index retry backoff factor must be a positive float greater than or equal to one."));
    }

    /**
     * The absolute amount of time (over all retry attempts) to wait for a successful indexing operation.
     *
     * @return the maximum amount of time to wait over all indexing attempts, in ms
     */
    public int getIndexRetryMaxMs() {
        return indexRetryMaxMs;
    }

    /**
     * The absolute amount of time (over all retry attempts) to wait for a successful indexing operation.
     *
     * @param indexRetryMaxMs the maximum amount of time to wait over all indexing attempts, in ms
     */
    public void setIndexRetryMaxMs(int indexRetryMaxMs) {
        validateRetryMaxMs(1, indexRetryMaxMs,
                "Retry max ms must be a positive integer and be greater than the retry timeout.");
        this.indexRetryMaxMs = assertPositive(indexRetryMaxMs,
                iae("Index retry max ms must be a positive integer."));
    }

    public EventTupleIndexingRepository<DiscoSolrDocument> getIndexer() {
        return indexer;
    }

    public void setIndexer(EventTupleIndexingRepository<DiscoSolrDocument> indexer) {
        this.indexer = indexer;
    }

    public IndexDTOMapper getDtoMapper() {
        return dtoMapper;
    }

    public void setDtoMapper(IndexDTOMapper dtoMapper) {
        this.dtoMapper = dtoMapper;
    }

    private static void validateRetryMaxMs(int indexRetryTimeoutMs, int indexRetryMaxMs, String s) {
        if (indexRetryMaxMs < indexRetryTimeoutMs) {
            throw new IllegalArgumentException(s);
        }
    }

}
