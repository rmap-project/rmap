package info.rmapproject.indexing.solr.repository;

import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.repository.SolrCrudRepository;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static info.rmapproject.indexing.IndexUtils.assertNotNull;

/**
 * Provides common indexing operations for all Solr indexes.  Allows for pre- and post- indexing hooks, and decoration
 * of the Solr document prior to indexing.
 * <p>
 * Each Solr core is expected to have exactly one concrete instance of this repository (i.e. there is a one-to-one
 * mapping between a concrete instance of {@code AbstractEventTupleIndexer} and a Solr core).  Adding (or removing) a Solr
 * core from the indexing infrastructure should result in the configuration (or removal) of a {@code AbstractEventTupleIndexer}.
 * </p>
 *
 * @param <T> the type representing the Solr document
 * @param <ID> the type representing the identifier of the Solr document
 */
public abstract class AbstractEventTupleIndexer<T, ID extends Serializable> implements EventTupleIndexingRepository<T> {

    /**
     * Maps an {@link EventDiscoTuple} to a Solr document
     */
    protected Function<EventDiscoTuple, T> documentMapper;

    /**
     * Solr document repository
     */
    protected SolrCrudRepository<T, ID> repository;

    /**
     * Solr template for the {@link #repository repository}
     */
    protected SolrTemplate template;

    /**
     * Decorates/customizes the Solr document produced by the {@link #documentMapper mapper} prior to
     * repository deposit.
     */
    protected Consumer<T> documentDecorator = (doc) -> { };

    /**
     * Constructs a repository implementation that:
     * <ul>
     *     <li>Produces Solr documents from {@link EventDiscoTuple}s using the supplied {@code documentMapper}</li>
     *     <li>Deposits Solr documents in the supplied {@link SolrCrudRepository}</li>
     *     <li>Provides the supplied {@link SolrTemplate} to implementations, allowing custom processing of the
     *         index</li>
     * </ul>
     * @param documentMapper maps tuples to Solr documents
     * @param repository stores Solr documents
     * @param template perform custom operations on indexes
     */
    public AbstractEventTupleIndexer(Function<EventDiscoTuple, T> documentMapper, SolrCrudRepository<T, ID> repository,
                                     SolrTemplate template) {
        this.documentMapper = assertNotNull(documentMapper,
                "EventDiscoTuple document mapper must not be null.");
        this.repository = assertNotNull(repository, "Solr repository must not be null.");
        this.template = assertNotNull(template, "Solr template must not be null");
    }

    /**
     * Executed for each {@link EventDiscoTuple} in the tuple stream provided to {@link #index(Stream)} prior to
     * indexing.
     *
     * @param tuple an {@code EventDiscoTuple} eventually mapped to a Solr document for indexing
     */
    public abstract void preIndex(EventDiscoTuple tuple);

    /**
     * Executed for each {@link org.springframework.data.solr.core.mapping.SolrDocument} that was indexed.
     *
     * @param solrDocument the Solr document that was sent to the index.
     */
    public abstract void postIndex(T solrDocument);

    /**
     * {@inheritDoc}
     * <h3>Implementation discussion</h3>
     * Responsible for indexing a stream of {@link EventDiscoTuple}s.
     * Executes in order:
     * <ol>
     *     <li>Invokes the {@link #preIndex(EventDiscoTuple) pre-index} hook for each tuple in the stream, allowing sub
     *         classes to perform custom logic prior to indexing</li>
     *     <li>Invokes the {@link #documentMapper document mapper}, which maps tuples to Solr documents to be
     *         indexed</li>
     *     <li>Indexes each document to the {@link #repository Solr repository}</li>
     *     <li>Invokes the {@link #postIndex(Object) post-index} hook for each Solr document, allowing sub classes
     *         to perform custom logic after indexing a document</li>
     * </ol>
     *
     * @param tupleStream {@inheritDoc}
     */
    @Override
    public void index(Stream<EventDiscoTuple> tupleStream) {
        index(tupleStream, this.documentDecorator);
    }

    /**
     * {@inheritDoc}
     * <h3>Implementation discussion</h3>
     * Responsible for indexing a stream of {@link EventDiscoTuple}s.
     * Executes in order:
     * <ol>
     *     <li>Invokes the {@link #preIndex(EventDiscoTuple) pre-index} hook for each tuple in the stream, allowing sub
     *         classes to perform custom logic prior to indexing</li>
     *     <li>Invokes the {@link #documentMapper document mapper}, which maps tuples to Solr documents to be
     *         indexed</li>
     *     <li>Decorates the documents with the {@link #documentDecorator document decorator}, allowing the
     *         caller to add additional information to the Solr document prior to indexing</li>
     *     <li>Indexes each document to the {@link #repository Solr repository}</li>
     *     <li>Invokes the {@link #postIndex(Object) post-index} hook for each Solr document, allowing sub classes
     *         to perform custom logic after indexing a document</li>
     * </ol>
     *
     * @param tupleStream {@inheritDoc}
     * @param decorator decorates each Solr document prior to indexing
     */
    @Override
    public void index(Stream<EventDiscoTuple> tupleStream, Consumer<T> decorator) {
        tupleStream
                .peek(this::preIndex)
                .map(tuple -> documentMapper.apply(tuple))
                .peek(decorator)
                .map(document -> repository.save(document))
                .forEach(this::postIndex);
    }

}
