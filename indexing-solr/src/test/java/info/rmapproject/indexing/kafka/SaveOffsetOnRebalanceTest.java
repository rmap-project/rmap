package info.rmapproject.indexing.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({"unchecked", "rawtypes"})
public class SaveOffsetOnRebalanceTest {

    @Test(expected = IllegalArgumentException.class)
    public void testNullOffsetLookupOnConstruction() throws Exception {
        new SaveOffsetOnRebalance(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullConsumerOnConstruction() throws Exception {
        new SaveOffsetOnRebalance(mock(OffsetLookup.class), null);
    }

    @Test
    public void testNonNullConstructors() throws Exception {
        new SaveOffsetOnRebalance(mock(OffsetLookup.class));
        new SaveOffsetOnRebalance(mock(OffsetLookup.class), mock(Consumer.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNullConsumer() throws Exception {
        SaveOffsetOnRebalance underTest = new SaveOffsetOnRebalance(mock(OffsetLookup.class), mock(Consumer.class));
        underTest.setConsumer(null);
    }

    @Test
    public void testSetNonNullConsumer() throws Exception {
        SaveOffsetOnRebalance underTest = new SaveOffsetOnRebalance(mock(OffsetLookup.class), mock(Consumer.class));
        underTest.setConsumer(mock(Consumer.class));
    }

    @Test
    public void testDefaultSeekBehavior() throws Exception {
        SaveOffsetOnRebalance underTest = new SaveOffsetOnRebalance(mock(OffsetLookup.class));
        assertEquals(SaveOffsetOnRebalance.DEFAULT_SEEK_BEHAVIOR, underTest.getSeekBehavior());
    }

    @Test
    public void testSetSeekBehavior() throws Exception {
        SaveOffsetOnRebalance underTest = new SaveOffsetOnRebalance(mock(OffsetLookup.class));

        underTest.setSeekBehavior(Seek.LATEST);
        assertEquals(Seek.LATEST, underTest.getSeekBehavior());

        underTest.setSeekBehavior(Seek.EARLIEST);
        assertEquals(Seek.EARLIEST, underTest.getSeekBehavior());
    }

    @Test
    @SuppressWarnings("serial")
    public void testOnPartitionsRevoked() throws Exception {
        String topic = "topic";
        int partition = 0;
        long offset = 21;
        TopicPartition tp = new TopicPartition(topic, partition);
        OffsetAndMetadata commitOffsetMd = new OffsetAndMetadata(offset, null);
        Consumer consumer = mock(Consumer.class);
        OffsetLookup lookup = mock(OffsetLookup.class);
        SaveOffsetOnRebalance underTest = new SaveOffsetOnRebalance(lookup, consumer);

        when(consumer.position(tp)).thenReturn(offset);

        underTest.onPartitionsRevoked(Collections.singleton(tp));

        verify(consumer).position(tp);
        verify(consumer).commitSync(new HashMap(){
                {
                    put(tp, commitOffsetMd);
                }
        });
    }

    @Test
    public void testOnPartitionsAssignedZeroOffsetLookup() throws Exception {
        long offset = 0;
        performOffsetLookupTest(offset);
    }

    @Test
    public void testOnPartitionsAssignedPositiveOffsetLookup() throws Exception {
        long offset = 43;
        performOffsetLookupTest(offset);
    }

    @Test
    public void testOnPartitionsAssignedNegativeOffsetLookup() throws Exception {
        long offset = -1;
        String topic = "topic";
        int partition = 0;
        TopicPartition tp = new TopicPartition(topic, partition);

        Consumer consumer = mock(Consumer.class);
        OffsetLookup lookup = mock(OffsetLookup.class);
        SaveOffsetOnRebalance underTest = new SaveOffsetOnRebalance(lookup, consumer);

        when(lookup.lookupOffset(topic, partition, Seek.LATEST)).thenReturn(offset);

        underTest.onPartitionsAssigned(Collections.singleton(tp));

        verify(lookup).lookupOffset(topic, partition, Seek.LATEST);
        if (SaveOffsetOnRebalance.DEFAULT_SEEK_BEHAVIOR == Seek.EARLIEST) {
            verify(consumer).seekToBeginning(Collections.singleton(tp));
        } else {
            verify(consumer).seekToEnd(Collections.singleton(tp));
        }
        verify(consumer).position(tp);
    }

    private static void performOffsetLookupTest(long offset) {
        String topic = "topic";
        int partition = 0;
        TopicPartition tp = new TopicPartition(topic, partition);

        Consumer consumer = mock(Consumer.class);
        OffsetLookup lookup = mock(OffsetLookup.class);
        SaveOffsetOnRebalance underTest = new SaveOffsetOnRebalance(lookup, consumer);

        when(lookup.lookupOffset(topic, partition, Seek.LATEST)).thenReturn(offset);

        underTest.onPartitionsAssigned(Collections.singleton(tp));

        verify(lookup).lookupOffset(topic, partition, Seek.LATEST);
        verify(consumer).seek(tp, offset + 1);
        verify(consumer).position(tp);
    }
}
