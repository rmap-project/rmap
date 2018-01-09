package info.rmapproject.indexing.kafka;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.agent.RMapAgent;
import info.rmapproject.core.model.impl.rdf4j.ORAdapter;
import info.rmapproject.core.model.impl.rdf4j.ORMapAgent;
import info.rmapproject.core.model.request.RequestEventDetails;
import info.rmapproject.core.rmapservice.RMapService;
import info.rmapproject.core.rmapservice.impl.rdf4j.triplestore.Rdf4jTriplestore;
import info.rmapproject.testdata.service.TestConstants;
import org.junit.Assert;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ConsumerTestUtil {

    /**
     * Asserts the supplied {@code exceptionHolder} is empty, otherwise {@link Assert#fail(String) fail} the test,
     * emitting the stacktrace in the failure message.
     *
     * @param exceptionHolder an {@code AtomicReference} which may hold an {@code Exception}
     */
    static void assertExceptionHolderEmpty(AtomicReference<Exception> exceptionHolder) {
        assertExceptionHolderEmpty("Encountered an unexpected exception", exceptionHolder);
    }

    /**
     * Asserts the supplied {@code exceptionHolder} is empty, otherwise {@link Assert#fail(String) fail} the test,
     * emitting the stacktrace in the failure message.
     *
     * @param message optional message to emit with a failed assertion
     * @param exceptionHolder an {@code AtomicReference} which may hold an {@code Exception}
     */
    static void assertExceptionHolderEmpty(String message, AtomicReference<Exception> exceptionHolder) {
        if (exceptionHolder.get() == null) {
            return;
        }

        ByteArrayOutputStream trace = new ByteArrayOutputStream();
        exceptionHolder.get().printStackTrace(new PrintStream(trace, true));
        if (message == null || message.trim().length() == 0) {
            fail("Consumer threw an unexpected exception: \n" + trace);
        } else {
            fail(message + "(Stacktrace: \n" + trace + "\n)");
        }
    }

    /**
     * Returns a {@link Runnable} that starts an indexing process upon invoking {@link Runnable#run()}.
     * <p>
     * The {@code Runnable} starts the supplied {@code indexer} by {@link IndexingConsumer#consumeEarliest(String)
     * consuming from the earliest offset} in the {@code topic}.  Any exceptions thrown by the {@code indexer} are
     * caught and stored in the supplied {@code exceptionHolder}.
     *
     * @param indexer the indexer start consuming RMap events from Kafka
     * @param topic the Kafka topic to consume from
     * @param exceptionHolder catches any exceptions thrown by the indexer
     * @return a {@code Runnable} that will start consuming RMap events when started
     */
    static Runnable newConsumerRunnable(IndexingConsumer indexer, String topic,
                                        AtomicReference<Exception> exceptionHolder) {
        return () -> {
            try {
                indexer.consumeEarliest(topic);
            } catch (Exception e) {
                exceptionHolder.set(e);
            }
        };
    }

    /**
     * Returns a {@link Runnable} that starts an indexing process upon invoking {@link Runnable#run()}.
     * <p>
     * The {@code Runnable} starts the supplied {@code indexer} by {@link IndexingConsumer#consume(String, Seek)
     * consuming} from the {@code topic}, starting from the beginning or end as directed by {@code seekBehavior}.  Any
     * exceptions thrown by the {@code indexer} are caught and stored in the supplied {@code exceptionHolder}.
     *
     * @param indexer the indexer start consuming RMap events from Kafka
     * @param topic the Kafka topic to consume from
     * @param seekBehavior whether to seek to the beginning or end of the Kafka topic
     * @param exceptionHolder catches any exceptions thrown by the indexer
     * @return a {@code Runnable} that will start consuming RMap events when started
     */
    static Runnable newConsumerRunnable(IndexingConsumer indexer, String topic, Seek seekBehavior,
                                        AtomicReference<Exception> exceptionHolder) {
        return () -> {
            try {
                indexer.consume(topic, seekBehavior);
            } catch (Exception e) {
                exceptionHolder.set(e);
            }
        };
    }

    /**
     * Instantiate an {@link ORMapAgent} to represent a System agent, and use the {@link RMapService} to create the
     * agent.  Verifies the agent was created using {@link RMapService#isAgentId(URI)}
     *
     * @param rmapService used to create the agent in the underlying triplestore
     * @throws RMapException
     * @throws RMapDefectiveArgumentException
     * @throws URISyntaxException
     */
    static RMapAgent createSystemAgent(RMapService rmapService) throws RMapException, RMapDefectiveArgumentException, URISyntaxException {
        IRI AGENT_IRI = ORAdapter.getValueFactory().createIRI(TestConstants.SYSAGENT_ID);
        IRI ID_PROVIDER_IRI = ORAdapter.getValueFactory().createIRI(TestConstants.SYSAGENT_ID_PROVIDER);
        IRI AUTH_ID_IRI = ORAdapter.getValueFactory().createIRI(TestConstants.SYSAGENT_AUTH_ID);
        Literal NAME = ORAdapter.getValueFactory().createLiteral(TestConstants.SYSAGENT_NAME);
        RMapAgent sysagent = new ORMapAgent(AGENT_IRI, ID_PROVIDER_IRI, AUTH_ID_IRI, NAME);

        RequestEventDetails requestEventDetails = new RequestEventDetails(new URI(TestConstants.SYSAGENT_ID), new URI(TestConstants.SYSAGENT_KEY));

        //create new test agent
        URI agentId = sysagent.getId().getIri();
        if (!rmapService.isAgentId(agentId)) {
            rmapService.createAgent(sysagent, requestEventDetails);
        }

        // Check the agent was created
        assertTrue(rmapService.isAgentId(agentId));

        return sysagent;
    }

    /**
     * Dumps the contents of the triplestore to the provided output stream.
     *
     * @param outputStream
     * @throws Exception
     */
    static void dumpTriplestore(Rdf4jTriplestore triplestore, OutputStream outputStream) throws Exception {
        List<Statement> statements = triplestore.getStatementListBySPARQL("select ?s ?p ?o ?c where {GRAPH ?c {?s ?p ?o}}");
        statements.forEach(
                statement -> {
                    try {
                        outputStream.write(statement.toString().getBytes("UTF-8"));
                        outputStream.write("\n".getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                }
        );
    }
}
