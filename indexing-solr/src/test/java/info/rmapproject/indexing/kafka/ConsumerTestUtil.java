package info.rmapproject.indexing.kafka;

import info.rmapproject.core.exception.RMapDefectiveArgumentException;
import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.agent.RMapAgent;
import info.rmapproject.core.model.impl.openrdf.ORAdapter;
import info.rmapproject.core.model.impl.openrdf.ORMapAgent;
import info.rmapproject.core.model.request.RequestEventDetails;
import info.rmapproject.core.rmapservice.RMapService;
import info.rmapproject.core.rmapservice.impl.openrdf.triplestore.SesameTriplestore;
import info.rmapproject.testdata.service.TestConstants;
import org.junit.Assert;
import org.openrdf.model.IRI;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;

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

    static Runnable newConsumerRunnable(IndexingConsumer indexer, String topic, AtomicReference<Exception> caughtExeption) {
        return () -> {
            try {
                indexer.consumeEarliest(topic);
            } catch (Exception e) {
                caughtExeption.set(e);
            }
        };
    }

    static Runnable newConsumerRunnable(IndexingConsumer indexer, String topic, Seek seekBehavior, AtomicReference<Exception> caughtExeption) {
        return () -> {
            try {
                indexer.consume(topic, seekBehavior);
            } catch (Exception e) {
                caughtExeption.set(e);
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
    static void dumpTriplestore(SesameTriplestore triplestore, OutputStream outputStream) throws Exception {
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
