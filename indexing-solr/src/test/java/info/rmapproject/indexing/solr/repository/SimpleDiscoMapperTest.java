package info.rmapproject.indexing.solr.repository;

import info.rmapproject.core.model.RMapTriple;
import info.rmapproject.core.model.disco.RMapDiSCO;
import info.rmapproject.core.model.impl.openrdf.ORAdapter;
import info.rmapproject.indexing.solr.AbstractSpringIndexingTest;
import info.rmapproject.indexing.solr.model.DiscoSolrDocument;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import static info.rmapproject.core.model.impl.openrdf.ORAdapter.openRdfStatement2RMapTriple;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class SimpleDiscoMapperTest extends AbstractSpringIndexingTest {

    private ValueFactory valueFactory = ORAdapter.getValueFactory();

    @Autowired
    private SimpleDiscoMapper underTest;

    /**
     * A disco with a related statement that has Literal object is created.
     * The disco is mapped to a solr document.
     * Verify that the related statement is present in the solr document
     * Verify that the related statement in the solr document is equal to the statement from the disco.
     */
    @Test
    public void testRoundTripRelatedStatementsLiteral() throws IOException {
        RMapTriple expectedTriple = createTripleWithLiteral();

        DiscoSolrDocument doc = new DiscoSolrDocument();

        RMapDiSCO disco = mock(RMapDiSCO.class);
        when(disco.getRelatedStatements()).thenReturn(singletonList(expectedTriple));

        underTest.apply(disco, doc);

        assertNotNull(doc.getDiscoRelatedStatements());
        verify(disco, atLeastOnce()).getRelatedStatements();


        RMapTriple actualTriple = parseTriple(doc);
        assertTripleEquals(expectedTriple, actualTriple);
    }

    /**
     * A disco with a related statement that has blank node for an object is created.
     * The disco is mapped to a solr document.
     * Verify that the related statement is present in the solr document
     * Verify that the related statement in the solr document is equal to the statement from the disco.
     */
    @Test
    public void testRoundTripRelatedStatementsBnode() throws IOException {
        RMapTriple expectedTriple = createTripleWithBnode();

        DiscoSolrDocument doc = new DiscoSolrDocument();

        RMapDiSCO disco = mock(RMapDiSCO.class);
        when(disco.getRelatedStatements()).thenReturn(singletonList(expectedTriple));

        underTest.apply(disco, doc);

        assertNotNull(doc.getDiscoRelatedStatements());
        verify(disco, atLeastOnce()).getRelatedStatements();


        RMapTriple actualTriple = parseTriple(doc);
        assertTripleWithObjectMatches(expectedTriple, actualTriple, ".*bar");
    }

    /**
     * A disco with a related statement that has a resource for an object is created.
     * The disco is mapped to a solr document.
     * Verify that the related statement is present in the solr document
     * Verify that the related statement in the solr document is equal to the statement from the disco.
     */
    @Test
    public void testRoundTripRelatedStatementsIri() throws IOException {
        RMapTriple expectedTriple = createTripleWithIri();

        DiscoSolrDocument doc = new DiscoSolrDocument();

        RMapDiSCO disco = mock(RMapDiSCO.class);
        when(disco.getRelatedStatements()).thenReturn(singletonList(expectedTriple));

        underTest.apply(disco, doc);

        assertNotNull(doc.getDiscoRelatedStatements());
        verify(disco, atLeastOnce()).getRelatedStatements();


        RMapTriple actualTriple = parseTriple(doc);
        assertTripleEquals(expectedTriple, actualTriple);
    }

    /**
     * Creates a triple with a literal object
     * @return
     */
    private RMapTriple createTripleWithLiteral() {
        Statement s = valueFactory.createStatement(
                valueFactory.createIRI("foo:a"),
                valueFactory.createIRI("foo:b"),
                valueFactory.createLiteral("bar"));

        return openRdfStatement2RMapTriple(s);
    }

    /**
     * Creates a triple with a blank node for an object
     * @return
     */
    private RMapTriple createTripleWithBnode() {
        Statement s = valueFactory.createStatement(
                valueFactory.createIRI("foo:a"),
                valueFactory.createIRI("foo:b"),
                valueFactory.createBNode("bar"));

        return openRdfStatement2RMapTriple(s);
    }

    /**
     * Creates a triple with a resource for an object
     * @return
     */
    private RMapTriple createTripleWithIri() {
        Statement s = valueFactory.createStatement(
                valueFactory.createIRI("foo:a"),
                valueFactory.createIRI("foo:b"),
                valueFactory.createLiteral("foo:bar"));

        return openRdfStatement2RMapTriple(s);
    }

    /**
     * Parses the RMapTriple out of the supplied solr document
     *
     * @param doc
     * @return
     * @throws IOException
     */
    private RMapTriple parseTriple(DiscoSolrDocument doc) throws IOException {
        Collection<Statement> statements = new HashSet<>();
        RDFParser parser = Rio.createParser(RDFFormat.NQUADS);
        parser.setRDFHandler(new StatementCollector(statements));
        parser.parse(new ByteArrayInputStream(doc.getDiscoRelatedStatements().get(0).getBytes()), "");
        assertEquals(1, statements.size());

        return openRdfStatement2RMapTriple(statements.iterator().next());
    }

    /**
     * Asserts that the S, P, O of each triple equal each other.
     * @param expectedTriple
     * @param actualTriple
     */
    private void assertTripleEquals(RMapTriple expectedTriple, RMapTriple actualTriple) {
        assertEquals(expectedTriple.getSubject().getStringValue(), actualTriple.getSubject().getStringValue());
        assertEquals(expectedTriple.getObject().getStringValue(), actualTriple.getObject().getStringValue());
        assertEquals(expectedTriple.getPredicate().getStringValue(), actualTriple.getPredicate().getStringValue());
    }

    /**
     * Asserts that the S, P of each triple equal each other, and that the Objects of each triple match the supplied
     * regex.  Useful when the object is a blank node.
     * @param expectedTriple
     * @param actualTriple
     * @param objectRegex
     */
    private void assertTripleWithObjectMatches(RMapTriple expectedTriple, RMapTriple actualTriple, String objectRegex) {
        assertEquals(expectedTriple.getSubject().getStringValue(), actualTriple.getSubject().getStringValue());
        assertTrue(expectedTriple.getObject().getStringValue().matches(objectRegex));
        assertTrue(actualTriple.getObject().getStringValue().matches(objectRegex));
        assertEquals(expectedTriple.getPredicate().getStringValue(), actualTriple.getPredicate().getStringValue());
    }
}