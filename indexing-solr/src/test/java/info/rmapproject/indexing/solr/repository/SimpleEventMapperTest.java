package info.rmapproject.indexing.solr.repository;

import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.impl.openrdf.ORMapEvent;
import info.rmapproject.core.model.impl.openrdf.ORMapEventCreation;
import info.rmapproject.indexing.solr.model.DiscoSolrDocument;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SimpleEventMapperTest {

    /**
     * Insures that the SimpleEventMapper will map the progenitor lineage uri into the Solr document.
     */
    @Test
    public void testMapLineageProgenitor() {
        String lineageProgenitor = "http://some/lineage/progenitor";
        ORMapEvent event = mock(ORMapEventCreation.class);
        when(event.getLineageProgenitor()).thenReturn(new RMapIri(URI.create(lineageProgenitor)));
        DiscoSolrDocument doc = new DiscoSolrDocument();
        SimpleEventMapper underTest = new SimpleEventMapper();

        underTest.apply(event, doc);

        verify(event, atLeastOnce()).getLineageProgenitor();
        assertEquals(lineageProgenitor, doc.getEventLineageProgenitorUri());
    }

    /**
     * Insures that a null progenitor lineage uri will not trip up the SimpleEventMapper.
     */
    @Test
    public void testMapNullLineageProgenitor() {
        ORMapEvent event = mock(ORMapEventCreation.class);
        DiscoSolrDocument doc = new DiscoSolrDocument();
        SimpleEventMapper underTest = new SimpleEventMapper();

        underTest.apply(event, doc);

        verify(event, atLeastOnce()).getLineageProgenitor();
        assertEquals(null, doc.getEventLineageProgenitorUri());
    }

}