package info.rmapproject.indexing.solr.repository;

import info.rmapproject.core.model.RMapStatus;
import info.rmapproject.core.rdfhandler.RDFHandler;
import info.rmapproject.indexing.solr.AbstractSpringIndexingTest;
import info.rmapproject.indexing.solr.TestResourceManager;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class StandAloneStatusInferencerTest extends AbstractSpringIndexingTest {

    @Autowired
    private RDFHandler rdfHandler;

    private TestResourceManager rm;

    private StandAloneStatusInferencer underTest = new StandAloneStatusInferencer();

    @Before
    public void setUp() throws Exception {
        super.setUp();

        rm = TestResourceManager.load(
                "/data/discos/rmd18mddcw", RDFFormat.NQUADS, rdfHandler);
    }

    @Test
    public void testDisconnectedEntities() throws Exception {

    }

    @Test
    public void testInferStatusForUpdateSource() throws Exception {
        Optional<RMapStatus> actualStatus = underTest.inferDiscoStatus(
                rm.getDisco("rmap:rmd18m7mr7"),
                rm.getEvent("rmap:rmd18mdd9v"),
                rm.getAgent("rmap:rmd18m7mj4"));

        assertTrue(actualStatus.isPresent());
        assertEquals(RMapStatus.INACTIVE, actualStatus.get());
    }

    @Test
    public void testInferStatusForUpdateTarget() throws Exception {
        Optional<RMapStatus> actualStatus = underTest.inferDiscoStatus(
                rm.getDisco("rmap:rmd18mdd8b"),
                rm.getEvent("rmap:rmd18mdd9v"),
                rm.getAgent("rmap:rmd18m7mj4"));

        assertTrue(actualStatus.isPresent());
        assertEquals(RMapStatus.ACTIVE, actualStatus.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInferStatusForCreateSource() throws Exception {
        Optional<RMapStatus> actualStatus = underTest.inferDiscoStatus(
                null,
                rm.getEvent("rmap:rmd18m7msr"),
                rm.getAgent("rmap:rmd18m7mj4"));
    }

    @Test
    public void testInferStatusForCreateTarget() throws Exception {
        Optional<RMapStatus> actualStatus = underTest.inferDiscoStatus(
                rm.getDisco("rmap:rmd18m7mr7"),
                rm.getEvent("rmap:rmd18m7msr"),
                rm.getAgent("rmap:rmd18m7mj4"));

        assertTrue(actualStatus.isPresent());
        assertEquals(RMapStatus.ACTIVE, actualStatus.get());
    }

    @Test
    public void testInferStatusForDeleteSource() throws Exception {

    }

    @Test
    public void testInferStatusForDeleteTarget() throws Exception {

    }

    @Test
    public void testInferStatusForReplaceSource() throws Exception {

    }

    @Test
    public void testInferStatusForReplaceTarget() throws Exception {

    }

    @Test
    public void testInferStatusForDeriveSource() throws Exception {

    }

    @Test
    public void testInferStatusForDeriveTarget() throws Exception {

    }

    @Test
    public void testInferStatusForTombstoneSource() throws Exception {

    }

    @Test
    public void testInferStatusForTombstoneTarget() throws Exception {

    }

    @Test
    public void testInferStatusForInactivateSource() throws Exception {

    }

    @Test
    public void testInferStatusForInactivateTarget() throws Exception {

    }

}