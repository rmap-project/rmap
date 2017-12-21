package info.rmapproject.indexing.solr.repository;

import info.rmapproject.indexing.solr.model.DiscoSolrDocument;
import org.junit.Test;
import org.mockito.InOrder;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.repository.SolrCrudRepository;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Verifies the abstract implementation of {@link EventTupleIndexingRepository}.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class AbstractEventTupleIndexerTest {

    /**
     * Insure that a non-null document decorator is provided by the abstract impl\
     */
    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testDefaultDocumentDecorator() {
        Function documentMapper = mock(Function.class);
        SolrCrudRepository repository = mock(SolrCrudRepository.class);
        SolrTemplate template = mock(SolrTemplate.class);

        AbstractEventTupleIndexer underTest = new AbstractEventTupleIndexer(documentMapper, repository, template) {
            @Override
            public void preIndex(EventDiscoTuple tuple) {

            }

            @Override
            public void postIndex(Object solrDocument) {

            }
        };

        assertNotNull(underTest.documentDecorator);
    }

    /**
     * Insure that the sequence documented by {@link AbstractEventTupleIndexer#index(Stream, Consumer)} is executed in proper
     * order.
     */
    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testIndexSequence() {
        Probe preIndexProbe = spy(new Probe());
        Probe postIndexProbe = spy(new Probe());

        Function documentMapper = mock(Function.class);
        SolrCrudRepository repository = mock(SolrCrudRepository.class);
        SolrTemplate template = mock(SolrTemplate.class);
        Consumer decorator = mock(Consumer.class);

        EventDiscoTuple tuple = new EventDiscoTuple();
        DiscoSolrDocument doc = new DiscoSolrDocument();

        AbstractEventTupleIndexer underTest = new AbstractEventTupleIndexer(documentMapper, repository, template) {
            @Override
            public void preIndex(EventDiscoTuple tuple) {
                preIndexProbe.fireBoolean(true);
            }

            @Override
            public void postIndex(Object solrDocument) {
                postIndexProbe.fireBoolean(true);
            }
        };

        doNothing().when(preIndexProbe).fireBoolean(true);
        doNothing().when(postIndexProbe).fireBoolean(true);
        when(documentMapper.apply(tuple)).thenReturn(doc);
        doNothing().when(decorator).accept(doc);
        when(repository.save(doc)).thenReturn(doc);
        InOrder inOrder = inOrder(preIndexProbe, documentMapper, decorator, repository, postIndexProbe);

        underTest.index(Stream.of(tuple), decorator);

        inOrder.verify(preIndexProbe).fireBoolean(true);
        inOrder.verify(documentMapper).apply(tuple);
        inOrder.verify(decorator).accept(doc);
        inOrder.verify(repository).save(doc);
        inOrder.verify(postIndexProbe).fireBoolean(true);
    }

    /**
     * Insure that the sequence documented by {@link AbstractEventTupleIndexer#index(Stream, Consumer)} is executed in proper
     * order when no decorator is provided by the caller.
     */
    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testIndexSequenceNoDecorator() throws Exception {
        Probe preIndexProbe = spy(new Probe());
        Probe postIndexProbe = spy(new Probe());
        Probe decoratorProbe = spy(new Probe());

        Function documentMapper = mock(Function.class);
        SolrCrudRepository repository = mock(SolrCrudRepository.class);
        SolrTemplate template = mock(SolrTemplate.class);

        EventDiscoTuple tuple = new EventDiscoTuple();
        DiscoSolrDocument doc = new DiscoSolrDocument();

        AbstractEventTupleIndexer underTest = new AbstractEventTupleIndexer(documentMapper, repository, template) {
            @Override
            public void preIndex(EventDiscoTuple tuple) {
                preIndexProbe.fireBoolean(true);
            }

            @Override
            public void postIndex(Object solrDocument) {
                postIndexProbe.fireBoolean(true);
            }
        };
        underTest.documentDecorator = o -> decoratorProbe.fireBoolean(true);

        doNothing().when(preIndexProbe).fireBoolean(true);
        doNothing().when(postIndexProbe).fireBoolean(true);
        when(documentMapper.apply(tuple)).thenReturn(doc);
        when(repository.save(doc)).thenReturn(doc);
        InOrder inOrder = inOrder(preIndexProbe, documentMapper, decoratorProbe, repository, postIndexProbe);

        underTest.index(Stream.of(tuple));

        inOrder.verify(preIndexProbe).fireBoolean(true);
        inOrder.verify(documentMapper).apply(tuple);
        inOrder.verify(decoratorProbe).fireBoolean(true);
        inOrder.verify(repository).save(doc);
        inOrder.verify(postIndexProbe).fireBoolean(true);
    }

    /**
     * Probe for verifying a method occurred.  Can be used as a Mockito spy for order-sensitive tests.
     */
    private class Probe {
        private boolean fired = false;
        void fireBoolean(boolean arg) {
            fired = arg;
        }
    }
}