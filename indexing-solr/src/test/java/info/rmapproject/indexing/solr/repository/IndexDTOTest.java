package info.rmapproject.indexing.solr.repository;

import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.event.RMapEventUpdate;
import info.rmapproject.indexing.solr.AbstractSpringIndexingTest;
import info.rmapproject.indexing.solr.TestResourceManager;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import java.net.URI;

import static info.rmapproject.core.SerializationAssertions.serializeTest;
import static info.rmapproject.core.SerializationAssertions.serializeWithCompression;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class IndexDTOTest extends AbstractSpringIndexingTest {

    private static TestResourceManager rm;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        rm = TestResourceManager.load("/data/discos/rmd18mddcw", RDFFormat.NQUADS, rdfHandler);
    }

    /**
     * Test IndexDTO creation where the event has no source.
     *
     * @throws Exception
     */
    @Test
    public void createWithConnectedGraphWithNullSource() throws Exception {
        // rmap:rmd18m7msr is a CREATION event, which has no source.
        new IndexDTO(rm.getEvent("rmap:rmd18m7msr"),
                     rm.getAgent("rmap:rmd18m7mj4"),
                     null,
                     rm.getDisco("rmap:rmd18m7mr7"));
    }

    /**
     * Test IndexDTO creation with a fully connected Event, with agent, source disco, and target disco
     *
     * @throws Exception
     */
    @Test
    public void createWithConnectedGraph() throws Exception {
        // rmap:rmd18mdd9v is an UPDATE event with a source and target
        new IndexDTO(rm.getEvent("rmap:rmd18mdd9v"),
                rm.getAgent("rmap:rmd18m7mj4"),
                rm.getDisco("rmap:rmd18m7mr7"),
                rm.getDisco("rmap:rmd18mdd8b"));
    }

    /**
     * Test IndexDTO creation where the Event object references a disco that is not in the DTO
     *
     * @throws Exception
     */
    @Test(expected = IllegalStateException.class)
    public void createWithDisconnectedSource() throws Exception {

        // Disconnect the graph by munging the URI for the source of the event
        RMapEventUpdate disconnectedSource = spy((RMapEventUpdate)rm.getEvent("rmap:rmd18mdd9v"));
        when(disconnectedSource.getInactivatedObjectId()).thenReturn(new RMapIri(URI.create("rmap:foo")));

        // Expect an ISE on construction
        new IndexDTO(disconnectedSource,
                rm.getAgent("rmap:rmd18m7mj4"),
                rm.getDisco("rmap:rmd18m7mr7"),
                rm.getDisco("rmap:rmd18mdd8b"));
    }

    /**
     * Test IndexDTO creation where the Event object references a disco that is not in the DTO
     *
     * @throws Exception
     */
    @Test(expected = IllegalStateException.class)
    public void createWithDisconnectedTarget() throws Exception {

        // Disconnect the graph by munging the URI for the target of the event
        RMapEventUpdate disconnectedSource = spy((RMapEventUpdate)rm.getEvent("rmap:rmd18mdd9v"));
        when(disconnectedSource.getDerivedObjectId()).thenReturn(new RMapIri(URI.create("rmap:foo")));

        // Expect an ISE on construction
        new IndexDTO(disconnectedSource,
                rm.getAgent("rmap:rmd18m7mj4"),
                rm.getDisco("rmap:rmd18m7mr7"),
                rm.getDisco("rmap:rmd18mdd8b"));
    }

    /**
     * Insures the IndexDTO object graph is serializable.
     * 
     * @throws Exception
     */
    @Test
    public void indexDtoSerialization() throws Exception {
        // rmap:rmd18mdd9v is an UPDATE event with a source and target
        IndexDTO dto = new IndexDTO(rm.getEvent("rmap:rmd18mdd9v"),
                rm.getAgent("rmap:rmd18m7mj4"),
                rm.getDisco("rmap:rmd18m7mr7"),
                rm.getDisco("rmap:rmd18mdd8b"));

        serializeTest(dto);

        serializeWithCompression(dto);
    }
}