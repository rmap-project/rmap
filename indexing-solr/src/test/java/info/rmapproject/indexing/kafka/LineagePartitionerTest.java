package info.rmapproject.indexing.kafka;

import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.model.event.RMapEventTargetType;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.PartitionInfo;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static info.rmapproject.indexing.kafka.LineagePartitioner.TOPIC;
import static info.rmapproject.indexing.kafka.LineagePartitioner.calculatePartitionIdUsingLineage;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class LineagePartitionerTest {

    private final byte[] EMPTY_BYTE_ARRAY = {};

    private LineagePartitioner underTest = new LineagePartitioner();

    /**
     * RMap event that targets a disco.  Events targeting discos have non-null lineage progenitors
     */
    private RMapEvent discoEvent = mock(RMapEvent.class);

    /**
     * RMap event that targets an agent.  Events targeting non-disco types will have null lineage progenitors
     */
    private RMapEvent agentEvent = mock(RMapEvent.class);

    /**
     * Collaborating object for the Kafka Cluster that represents the leader of an arbitrary number of partitions.
     */
    private Node leader = mock(Node.class);

    @Before
    public void setUp() throws Exception {
        when(discoEvent.getEventTargetType()).thenReturn(RMapEventTargetType.DISCO);
        when(discoEvent.getLineageProgenitor()).thenReturn(new RMapIri(URI.create("http://rmapproject.info/event/1")));

        when(agentEvent.getEventTargetType()).thenReturn(RMapEventTargetType.AGENT);

        // Collaborator that must be mocked since the Cluster class is final.  It must have a node ID, and must be
        // returned as the leader of the lone partition
        Node node = mock(Node.class);
        when(node.id()).thenReturn(10); // who cares, the node id just needs to be present
    }

    /**
     * Insure an RMap event is assigned to the expected partition when there is only a single partition in the Kafka
     * cluster for the RMap topic.
     *
     * @throws Exception
     */
    @Test
    public void testAssignSinglePartition() throws Exception {
        int expectedPartitionId = 0;

        // Mock a single PartitionInfo representing the single partition in the RMap event topic
        PartitionInfo partitionInfo = mock(PartitionInfo.class);
        when(partitionInfo.partition()).thenReturn(expectedPartitionId);
        when(partitionInfo.topic()).thenReturn(TOPIC);
        when(partitionInfo.leader()).thenReturn(leader);
        List<PartitionInfo> partitions = singletonList(partitionInfo);

        // Can't mock Cluster, it's final
        Cluster clusterInfo = new Cluster("clusterId", singletonList(leader), partitions,
                emptySet(), emptySet(), null);

        // Assign the partition and verify it against the expected value
        // (any number modulo 1 is 0)
        assertEquals(expectedPartitionId,
                underTest.partition(TOPIC, "fooKey", EMPTY_BYTE_ARRAY, discoEvent, EMPTY_BYTE_ARRAY, clusterInfo));
    }

    /**
     * Insure an RMap event is assigned to the expected partition when there is six partitions in the Kafka cluster for
     * the RMap topic.
     *
     * @throws Exception
     */
    @Test
    public void testAssignWithSixPartitions() throws Exception {
        int expectedPartitionId = 4;
        List<PartitionInfo> partitions = new ArrayList<>();

        // Mock a six PartitionInfo objects representing the six partitions in the RMap event topic
        // Each PartitionInfo will have the above Node as its leader
        for (int partitionId = 0; partitionId < 6; partitionId++) {
            PartitionInfo partitionInfo = mock(PartitionInfo.class);
            when(partitionInfo.partition()).thenReturn(partitionId);
            when(partitionInfo.topic()).thenReturn(TOPIC);
            when(partitionInfo.leader()).thenReturn(leader);
            partitions.add(partitionInfo);
        }

        Cluster clusterInfo = new Cluster("clusterId", singletonList(leader), partitions,
                emptySet(), emptySet(), null);

        // Assign the partition and verify it against the expected value
        assertEquals(expectedPartitionId,
                underTest.partition(TOPIC, "fooKey", EMPTY_BYTE_ARRAY, discoEvent, EMPTY_BYTE_ARRAY, clusterInfo));
    }

    /**
     * Insure that the logic calculating the partition id works over a range of partition counts
     *
     * @throws Exception
     */
    @Test
    public void testCalculatePartitionId() throws Exception {
        assertEquals(0, calculatePartitionIdUsingLineage(discoEvent, 1));
        assertEquals(0, calculatePartitionIdUsingLineage(discoEvent, 2));
        assertEquals(1, calculatePartitionIdUsingLineage(discoEvent, 3));
        assertEquals(0, calculatePartitionIdUsingLineage(discoEvent, 4));
        assertEquals(1, calculatePartitionIdUsingLineage(discoEvent, 5));
        assertEquals(4, calculatePartitionIdUsingLineage(discoEvent, 6));
        assertEquals(4, calculatePartitionIdUsingLineage(discoEvent, 7));
        assertEquals(4, calculatePartitionIdUsingLineage(discoEvent, 8));
        assertEquals(7, calculatePartitionIdUsingLineage(discoEvent, 9));
        assertEquals(6, calculatePartitionIdUsingLineage(discoEvent, 10));
    }

    /**
     * Insure that the logic calculating the partition id is idempotent.  The same inputs should yield the same output.
     * That is, for a specified progenitor lineage URI, and number of partitions in the topic, the assigned partition
     * id should be the same for multiple invocations of the
     * {@link LineagePartitioner#calculatePartitionIdUsingLineage(RMapEvent, int)} method.
     *
     * @throws Exception
     */
    @Test
    public void testCalculatePartitionIdIdempotent() throws Exception {
        assertEquals(0, calculatePartitionIdUsingLineage(discoEvent, 4));
        assertEquals(0, calculatePartitionIdUsingLineage(discoEvent, 4));
    }

    /**
     * Insure a runtime exception is thrown when a partition is not available (occurs when a partition has no leader).
     *
     * @throws Exception
     */
    @Test(expected = RuntimeException.class)
    public void testAssignToUnavailablePartition() throws Exception {
        List<PartitionInfo> partitions = new ArrayList<>();

        for (int partitionId = 0; partitionId < 3; partitionId++) {
            PartitionInfo partitionInfo = mock(PartitionInfo.class);
            when(partitionInfo.partition()).thenReturn(partitionId);
            when(partitionInfo.topic()).thenReturn(TOPIC);

            // Partition ID 1 is the partition id that will be calculated for the supplied iri and number of partitions
            // Prevent this partition from being considered active by returning null when asked for its leader
            if (partitionId != 1) {
                when(partitionInfo.leader()).thenReturn(leader);
            }
            partitions.add(partitionInfo);
        }

        // Can't mock Cluster, it's final
        Cluster clusterInfo = new Cluster("clusterId", singletonList(leader), partitions,
                emptySet(), emptySet(), null);

        // Will throw a RuntimeException because the assigned partition is not available.
        underTest.partition(TOPIC, "fooKey", EMPTY_BYTE_ARRAY, discoEvent, EMPTY_BYTE_ARRAY, clusterInfo);
    }

    /**
     * Insure a RuntimeException is thrown when there is some other error encountered calculating the partition id.
     *
     * @throws Exception
     */
    @Test(expected = RuntimeException.class)
    public void testUnexpectedExceptionWhenCalculatingHashcode() throws Exception {
        PartitionInfo partitionInfo = mock(PartitionInfo.class);
        when(partitionInfo.partition()).thenReturn(0);
        when(partitionInfo.topic()).thenReturn(TOPIC);
        List<PartitionInfo> partitions = singletonList(partitionInfo);

        RMapEvent event = mock(RMapEvent.class);
        // This will cause an NPE to be thrown in the partition calculation logic
        when(event.getLineageProgenitor()).thenReturn(null);

        // Can't mock Cluster, it's final
        Cluster clusterInfo = new Cluster("clusterId", singletonList(leader), partitions,
                emptySet(), emptySet(), null);

        underTest.partition(TOPIC, "fooKey", EMPTY_BYTE_ARRAY, event, EMPTY_BYTE_ARRAY, clusterInfo);
    }
}