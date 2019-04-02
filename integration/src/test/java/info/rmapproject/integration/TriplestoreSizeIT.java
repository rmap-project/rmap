package info.rmapproject.integration;

import info.rmapproject.core.model.RMapObjectType;
import info.rmapproject.core.rdfhandler.RDFHandler;
import info.rmapproject.core.rdfhandler.impl.rdf4j.RioRDFHandler;
import info.rmapproject.indexing.TestUtils;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.io.IOUtils;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static info.rmapproject.core.model.RMapObjectType.DISCO;
import static info.rmapproject.indexing.TestUtils.getRmapResources;
import static info.rmapproject.integration.Rdf4jUtil.convertRdf;
import static java.lang.String.format;
import static org.eclipse.rdf4j.rio.RDFFormat.NQUADS;
import static org.eclipse.rdf4j.rio.RDFFormat.RDFXML;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TriplestoreSizeIT extends BaseHttpIT {

    private static final Logger LOG = LoggerFactory.getLogger(BaseHttpIT.class);

    private static final String RMAP_NS = "http://purl.org/ontology/rmap#";

    /**
     * The URL template for retrieving the endpoint for a particular triplestore managed by the RDF4J server
     */
    private static final String RDF4J_SERVER_REPOSITORY_TEMPLATE = "%s/repositories/%s";

    /**
     * The URL template for retrieving the number of statements present in a context from the RDF4J server
     * (N.B. OkHttp has a url encoding issue with parameters)
     */
    private static final String SIZE_URL_TEMPLATE = RDF4J_SERVER_REPOSITORY_TEMPLATE + "/size?context=<%s%%3e";

    /**
     * If the disco in {@link #depositDisco()} has been deposited
     */
    private static boolean initialized;

    /**
     * The URI of the disco deposited in {@link #depositDisco()}
     */
    private static String discoUri;

    /**
     * The disco deposited in {@link #depositDisco()} in RDF/XML
     */
    private static String discoRdfXml;

    /**
     * The ID of our test disco
     */
    private static final String DISCO_ID = "rmd18m7mr7";

    private static final String DISCO_IRI = RMAP_NS + DISCO_ID;

    /**
     * Used to manipulate RDF
     */
    private RDFHandler rdfHandler = new RioRDFHandler();

    /**
     * For direct communication with the triplestore, bypassing RMap.
     */
    private HTTPRepository triplestore;

    /**
     * Deposits a single disco using the RMap API, and provides the URI for the newly deposited disco in
     * {@link #discoUri}.
     *
     * @throws Exception
     */
    @Before
    public void depositDisco() throws Exception {
        if (initialized) {
            return;
        }

        Map<RMapObjectType, Set<TestUtils.RDFResource>> testObjects = new HashMap<>();
        getRmapResources("/data/discos/rmd18mddcw", rdfHandler, NQUADS, testObjects);
        TestUtils.RDFResource rdf = testObjects.get(DISCO)
                .stream()
                .filter(resource -> {
                    try {
                        return resource.getURL().getPath().endsWith(DISCO_ID + ".n4");
                    } catch (IOException e) {
                        throw new RuntimeException("Unable to determine the URL for resource '" + resource + "':" +
                                e.getMessage(), e);
                    }
                })
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Missing rdf for disco '" + DISCO_IRI + "'"));
        discoRdfXml = IOUtils.toString(convertRdf(rdf.getInputStream(), rdf.getRdfFormat(), "", RDFXML), "UTF-8");
        LOG.debug("Depositing DiSCO to '{}':\n{}",  discosEndpoint.toURL(), discoRdfXml);
        discoUri = depositDisco(discosEndpoint.toURL(), discoRdfXml);
        initialized = true;
    }

    /**
     * Creates a {@link HTTPRepository}, and insures it can hand out connections; sets {@link #triplestore}.
     *
     * @throws Exception
     */
    @Before
    public void setUpTriplestore() throws Exception {
        final String repoUrl = repositoryUrl();
        triplestore = new HTTPRepository(repoUrl);
        try (RepositoryConnection c = triplestore.getConnection()) {
            assertNotNull("Unable to obtain connection from " + repoUrl, c);
        }
    }

    /**
     * Attempts to count the number of statements for the {@link #discoUri} context. Fails with RDF4J version 2.2.4.
     *
     * If you uncomment the code in the test which dumps the triplestore, the test succeeds.
     *
     * {@link #depositDisco()} deposits a disco via RMap, and now this test tries to count the statements in the graph.
     *
     * @throws Exception
     */
    @Test
    public void simpleTest() throws Exception {
        String rdf4jSizeEndpoint = sizeEndpoint(discoUri);
        LOG.debug("Retrieving count for context '{}' from '{}'", discoUri, rdf4jSizeEndpoint);
        Request req = new Request.Builder().get().url(rdf4jSizeEndpoint).build();
        Response res = http.newCall(req).execute();
        ResponseBody body = res.body();
        assertNotNull(body);
        String bodyText = body.string();
        res.close();
//         Test passes with 2.2.4 if you dump the triplestore.
//        try (RepositoryConnection c = triplestore.getConnection()) {
//            dumpTriplestore(c, System.err);
//        }
        assertTrue("Expected more than one statement in the context '" + discoUri + "'",
                Integer.parseInt(bodyText) > 1);
    }

    /**
     * Creates a trivial statement in the triplestore, and attempts to count the statements.  No RMap APIs are
     * used.  Succeeds with RDF4J 2.2.2 and 2.2.4.
     *
     * @throws Exception
     */
    @Test
    public void simpleTestToTriplestore() throws Exception {
        String fooUri = "ns:/foo";
        String n4 = String.format("<%s> <ns:/bar> <ns:/baz> <%s> .", fooUri, fooUri);
        LOG.debug("Adding the following statement directly to the triplestore: [{}]", n4);
        try (InputStream in = IOUtils.toInputStream(n4, "UTF-8");
             RepositoryConnection c = triplestore.getConnection()) {
            c.add(in, "", RDFFormat.NQUADS);
        }

        String rdf4jSizeEndpoint = sizeEndpoint(fooUri);
        LOG.debug("Retrieving count for context '{}' from '{}'", fooUri, rdf4jSizeEndpoint);
        Request req = new Request.Builder().get().url(rdf4jSizeEndpoint).build();
        Response res = http.newCall(req).execute();
        ResponseBody body = res.body();
        assertNotNull(body);
        String bodyText = body.string();
        res.close();
        assertEquals("Expected one statement in the context '" + fooUri + "'", 1, Integer.parseInt(bodyText));
    }

    /**
     * Creates statements in the triplestore for the test disco in {@link #discoRdfXml} using a specified context, and
     * attempts to count the statements.  Succeeds with RDF4J 2.2.2 and 2.2.4. No RMap APIs are used.
     *
     * @throws Exception
     */
    @Test
    public void discoTestToTriplestore() throws Exception {
        String discoIri = DISCO_IRI.replace(RMAP_NS, "rmap:");
        Resource context = SimpleValueFactory.getInstance().createIRI(discoIri);

        try (InputStream in = IOUtils.toInputStream(discoRdfXml, "UTF-8");
             RepositoryConnection c = triplestore.getConnection()) {
            c.add(in, "", RDFFormat.RDFXML, context);
        }

        String rdf4jSizeEndpoint = sizeEndpoint(discoIri);
        LOG.debug("Retrieving count for context '{}' from '{}'", discoIri, rdf4jSizeEndpoint);
        Request req = new Request.Builder().get().url(rdf4jSizeEndpoint).build();
        Response res = http.newCall(req).execute();
        ResponseBody body = res.body();
        assertNotNull(body);
        String bodyText = body.string();
        res.close();
        assertTrue("Expected more than one statement in the context '" + discoIri + "'",
                Integer.parseInt(bodyText) > 1);
    }

    private static String sizeEndpoint(String contextIri) {
        return format(SIZE_URL_TEMPLATE, rdf4jRepoUrl, rdf4jRepoName, contextIri);
    }

    private static String repositoryUrl() {
        return format(RDF4J_SERVER_REPOSITORY_TEMPLATE + "/", rdf4jRepoUrl, rdf4jRepoName);
    }

}
