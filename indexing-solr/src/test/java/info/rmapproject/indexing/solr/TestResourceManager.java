package info.rmapproject.indexing.solr;

import info.rmapproject.core.model.RMapObjectType;
import info.rmapproject.core.model.agent.RMapAgent;
import info.rmapproject.core.model.disco.RMapDiSCO;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.rdfhandler.RDFHandler;
import info.rmapproject.indexing.IndexUtils;
import org.openrdf.rio.RDFFormat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static info.rmapproject.indexing.IndexUtils.iae;

/**
 * Provides access for loading RDF from classpath resources, deserializing the RDF to RMap objects, and accessing
 * instances of specific RMap objects by URI.
 * <h3>Example usage:</h3>
 * <pre>
 * // Load RDF from the classpath resource /data/discos/rmd18mddcw (which resolves to a resource directory in the Maven project)
 * // The RDF in the /data/discos/rmd18mddcw directory is in N-QUADS format (ending in the .n4 extension)
 * TestResourceManager resourceManager = TestResourceManager.load("/data/discos/rmd18mddcw", RDFFormat.NQUADS, rdfHandler);
 *
 * // retrieve a particular Agent or DiSCO
 * RMapAgent agent = resourceManager.getAgent("rmap:rmd18m7mj4"); // rmap:rmd18m7mj4 is the URI of the Agent
 * RMapDiSCO disco = resourceManager.getDisco("rmap:rmd18m7mr7"); // rmap:rmd18m7mr7 is the URI of the DiSCO
 *
 * try {
 *     resourceManager.getEvent("rmap:does-not-exist");  // the specified URI does not identify an Event
 * } catch (IllegalArgumentException e) {
 *     // handle missing resource
 * }
 * </pre>
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class TestResourceManager {

    private RDFHandler rdfHandler;

    private Map<RMapObjectType, Set<TestUtils.RDFResource>> rmapObjects;

    /**
     * Constructs an instance of the manager with the supplied RDFHandler and RDFResources.
     *
     * @param rdfHandler
     * @param rmapObjects
     */
    private TestResourceManager(RDFHandler rdfHandler, Map<RMapObjectType, Set<TestUtils.RDFResource>> rmapObjects) {
        this.rdfHandler = rdfHandler;
        this.rmapObjects = rmapObjects;
    }

    /**
     * Obtain the agents from {@code rmapObjects} in encounter order
     *
     * @return a List of agents in encounter order
     */
    public List<RMapAgent> getAgents() {
        return TestUtils.getRmapObjects(rmapObjects, RMapObjectType.AGENT, rdfHandler);
    }

    /**
     * Obtain the specified agent
     *
     * @param iri the agent iri
     * @return the agent
     * @throws RuntimeException if the agent is not found
     */
    public RMapAgent getAgent(String iri) {
        return getAgents()
                .stream()
                .filter(a -> a.getId().getStringValue().equals(iri))
                .findAny()
                .orElseThrow(() -> new RuntimeException("Missing expected agent '" + iri + "'"));
    }

    /**
     * Obtain the events from {@code rmapObjects} in encounter order
     *
     * @return the events
     */
    public List<RMapEvent> getEvents() {
        return TestUtils.getRmapObjects(rmapObjects, RMapObjectType.EVENT, rdfHandler);
    }

    /**
     * Obtain the specified event
     *
     * @param iri the event iri
     * @return the event
     * @throws RuntimeException if the event is not found
     */
    public RMapEvent getEvent(String iri) {
        return getEvents()
                .stream()
                .filter(e -> e.getId().getStringValue().equals(iri))
                .findAny()
                .orElseThrow(iae("Missing expected event '" + iri + "'"));
    }

    /**
     * Obtain the discos from {@code rmapObjects} in encounter order
     *
     * @return the discos in encounter order
     */
    public List<RMapDiSCO> getDiscos() {
        return TestUtils.getRmapObjects(rmapObjects, RMapObjectType.DISCO, rdfHandler);
    }

    /**
     * Obtain the specified disco
     *
     * @param iri the disco iri
     * @return the disco
     * @throws RuntimeException if the disco is not found
     */
    public RMapDiSCO getDisco(String iri) {
        return getDiscos()
                .stream()
                .filter(d -> d.getId().getStringValue().equals(iri))
                .findAny()
                .orElseThrow(iae("Missing expected disco '" + iri + "'"));
    }

    /**
     * Load RDF resources from the filesystem and returns an instance of this class which provides access to the
     * loaded resources.
     * <p>
     * Assumes {@code resourcePath} specifies a directory containing files that contain DiSCOs, Agents, and
     * Events.  Each file must contain a single DiSCO, or single Event, or single Agent as retrieved from the
     * RMap HTTP API in the specified {@code format}.
     * </p>
     *
     * @param resourcePath a classpath resource that specifies a directory containing files in the specified
     *                     {@code format}
     * @param format identifies the RDF format of the files to be loaded.  The format determines which file extension
     *               is used to filter the files present in {@code resourcePath}
     * @param rdfHandler used to parse the RDF from the files found under {@code resourcePath}
     * @return a new instance of {@code TestResourceManager}, which can operate on the RDF resources loaded from the
     *         filesystem
     */
    public static TestResourceManager load(String resourcePath, RDFFormat format, RDFHandler rdfHandler) {
        Map<RMapObjectType, Set<TestUtils.RDFResource>> rmapObjects = new HashMap<>();
        TestUtils.getRmapResources(resourcePath, rdfHandler, format, rmapObjects);

        TestResourceManager instance = new TestResourceManager(rdfHandler, rmapObjects);

        return instance;
    }


}
