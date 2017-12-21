package info.rmapproject.indexing.solr.repository;

import info.rmapproject.core.model.agent.RMapAgent;
import info.rmapproject.core.model.disco.RMapDiSCO;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.indexing.solr.model.DiscoSolrDocument;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static info.rmapproject.indexing.IndexUtils.asRmapIri;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class SimpleEventDiscoTupleMapperTest {

    @Test
    public void testNoArgConstructorIse() throws Exception {
        SimpleEventDiscoTupleMapper underTest = new SimpleEventDiscoTupleMapper();

        try {
            underTest.apply(mock(EventDiscoTuple.class));
            fail("Expected an " + IllegalStateException.class.getSimpleName());
        } catch (IllegalStateException e) {
            // expected
        }

        underTest.setDiscoMapper(mock(DiscoMapper.class));

        try {
            underTest.apply(mock(EventDiscoTuple.class));
            fail("Expected an " + IllegalStateException.class.getSimpleName());
        } catch (IllegalStateException e) {
            // expected
        }

        underTest.setEventMapper(mock(EventMapper.class));

        try {
            underTest.apply(mock(EventDiscoTuple.class));
            fail("Expected an " + IllegalStateException.class.getSimpleName());
        } catch (IllegalStateException e) {
            // expected
        }

        underTest.setAgentMapper(mock(AgentMapper.class));

        assertNotNull(underTest.apply(mock(EventDiscoTuple.class)));
    }

    @Test
    public void testConstructor() throws Exception {
        try {
            new SimpleEventDiscoTupleMapper(null, null, null);
            fail("Expected an " + IllegalArgumentException.class.getSimpleName() + " exception");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            new SimpleEventDiscoTupleMapper(mock(DiscoMapper.class), null, null);
            fail("Expected an " + IllegalArgumentException.class.getSimpleName() + " exception");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            new SimpleEventDiscoTupleMapper(mock(DiscoMapper.class), mock(AgentMapper.class), null);
            fail("Expected an " + IllegalArgumentException.class.getSimpleName() + " exception");
        } catch (IllegalArgumentException e) {
            // expected
        }

        new SimpleEventDiscoTupleMapper(mock(DiscoMapper.class), mock(AgentMapper.class), mock(EventMapper.class));
    }

    @Test
    public void testConstructorWithPrototype() throws Exception {
        List<String> strings = new ArrayList<>();
        strings.add("foo");
        DiscoSolrDocument prototype = new DiscoSolrDocument.Builder().discoAggregatedResourceUris(strings).build();

        DiscoMapper mapper = mock(DiscoMapper.class);
        when(mapper.apply(any(), any())).thenAnswer(args -> {
            DiscoSolrDocument suppliedDoc = args.getArgument(1);
            assertNotSame(prototype, suppliedDoc);
            assertEquals(prototype, suppliedDoc);
            suppliedDoc.getDiscoAggregatedResourceUris().set(0, "bar");
            assertNotEquals(suppliedDoc.getDiscoAggregatedResourceUris().get(0),
                    prototype.getDiscoAggregatedResourceUris().get(0));
            return null;
        });

        SimpleEventDiscoTupleMapper underTest = new SimpleEventDiscoTupleMapper(prototype);
        underTest.setAgentMapper(mock(AgentMapper.class));
        underTest.setEventMapper(mock(EventMapper.class));
        underTest.setDiscoMapper(mapper);

        EventDiscoTuple tuple = new EventDiscoTuple();
        tuple.disco = mock(RMapDiSCO.class);

        underTest.apply(tuple);

        verify(mapper).apply(any(), any());
    }

    @Test
    public void testApplyInvokesAllMappers() throws Exception {
        DiscoMapper discoM = mock(DiscoMapper.class);
        AgentMapper agentM = mock(AgentMapper.class);
        EventMapper eventM = mock(EventMapper.class);

        EventDiscoTuple tuple = new EventDiscoTuple();
        tuple.disco = mock(RMapDiSCO.class);
        tuple.event = mock(RMapEvent.class);
        tuple.agent = mock(RMapAgent.class);

        SimpleEventDiscoTupleMapper underTest = new SimpleEventDiscoTupleMapper(discoM, agentM, eventM);
        underTest.apply(tuple);

        verify(discoM).apply(any(), any());
        verify(agentM).apply(any(), any());
        verify(eventM).apply(any(), any());
    }

    @Test
    public void testApplySetsEventTargetAndSource() throws Exception {
        DiscoMapper discoM = mock(DiscoMapper.class);
        AgentMapper agentM = mock(AgentMapper.class);
        EventMapper eventM = mock(EventMapper.class);

        EventDiscoTuple tuple = new EventDiscoTuple();
        tuple.eventTarget = asRmapIri("http://disco/target");
        tuple.eventSource = asRmapIri("http://disco/source");

        SimpleEventDiscoTupleMapper underTest = new SimpleEventDiscoTupleMapper(discoM, agentM, eventM);
        DiscoSolrDocument doc = underTest.apply(tuple);

        assertTrue(doc.getEventTargetObjectUris().contains("http://disco/target"));
        assertTrue(doc.getEventSourceObjectUris().contains("http://disco/source"));
        assertNull(doc.getDiscoEventDirection());
    }

    @Test
    public void testApplySetsEventDirectionTarget() throws Exception {
        DiscoSolrDocument proto = new DiscoSolrDocument();
        DiscoMapper discoM = mock(DiscoMapper.class);
        AgentMapper agentM = mock(AgentMapper.class);
        EventMapper eventM = mock(EventMapper.class);

        when(discoM.apply(any(), any())).thenReturn(proto);
        when(agentM.apply(any(), any())).thenReturn(proto);
        when(eventM.apply(any(), any())).thenReturn(proto);

        EventDiscoTuple tuple = new EventDiscoTuple();
        tuple.eventTarget = asRmapIri("http://disco/target");
        tuple.disco = mock(RMapDiSCO.class);
        when(tuple.disco.getId()).thenReturn(asRmapIri("http://disco/target"));

        SimpleEventDiscoTupleMapper underTest = new SimpleEventDiscoTupleMapper(discoM, agentM, eventM);
        DiscoSolrDocument doc = underTest.apply(tuple);

        assertEquals("TARGET", doc.getDiscoEventDirection());
    }

    @Test
    public void testApplySetsEventDirectionSource() throws Exception {
        DiscoSolrDocument proto = new DiscoSolrDocument();
        DiscoMapper discoM = mock(DiscoMapper.class);
        AgentMapper agentM = mock(AgentMapper.class);
        EventMapper eventM = mock(EventMapper.class);

        when(discoM.apply(any(), any())).thenReturn(proto);
        when(agentM.apply(any(), any())).thenReturn(proto);
        when(eventM.apply(any(), any())).thenReturn(proto);

        EventDiscoTuple tuple = new EventDiscoTuple();
        tuple.eventSource = asRmapIri("http://disco/source");
        tuple.disco = mock(RMapDiSCO.class);
        when(tuple.disco.getId()).thenReturn(asRmapIri("http://disco/source"));

        SimpleEventDiscoTupleMapper underTest = new SimpleEventDiscoTupleMapper(discoM, agentM, eventM);
        DiscoSolrDocument doc = underTest.apply(tuple);

        assertEquals("SOURCE", doc.getDiscoEventDirection());
    }

    @Test
    public void testApplySetsNoEventDirection() throws Exception {
        DiscoMapper discoM = mock(DiscoMapper.class);
        AgentMapper agentM = mock(AgentMapper.class);
        EventMapper eventM = mock(EventMapper.class);

        DiscoSolrDocument proto = new DiscoSolrDocument();
        when(discoM.apply(any(), any())).thenReturn(proto);
        when(agentM.apply(any(), any())).thenReturn(proto);
        when(eventM.apply(any(), any())).thenReturn(proto);

        EventDiscoTuple tuple = new EventDiscoTuple();
        tuple.eventSource = asRmapIri("http://disco/source");
        tuple.disco = mock(RMapDiSCO.class);
        when(tuple.disco.getId()).thenReturn(asRmapIri("http://foo/bar"));

        SimpleEventDiscoTupleMapper underTest = new SimpleEventDiscoTupleMapper(discoM, agentM, eventM);
        DiscoSolrDocument doc = underTest.apply(tuple);

        assertNull(doc.getDiscoEventDirection());
    }
}