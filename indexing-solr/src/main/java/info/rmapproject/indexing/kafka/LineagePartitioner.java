/*******************************************************************************
 * Copyright 2018 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This software was produced as part of the RMap Project (http://rmap-project.info),
 * The RMap Project was funded by the Alfred P. Sloan Foundation and is a
 * collaboration between Data Conservancy, Portico, and IEEE.
 *******************************************************************************/
package info.rmapproject.indexing.kafka;

import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.model.event.RMapEventTargetType;
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;

/**
 * Insures that every RMap event associated with a specific lineage will be assigned to the same partition, assuming
 * that the number of available Kafka partitions is constant. This class uses a hash-based algorithm to determine the
 * partition; the hash code of the RMap event's {@link RMapEvent#getLineageProgenitor() lineage URI} is calculated,
 * modulo the number of available partitions.
 * <p>
 * Note that if the number of partitions is changed (e.g. an administrator adds more partitions), then there is no
 * longer the guarantee that the events for a lineage are contained within a single partition.  Adding partitions to
 * the Kafka cluster is something that should be seriously considered, as a full re-build of the Kafka topic may be
 * necessary.
 * </p>
 * <p>
 * There may be times when the assigned partition is not available.  This can occur when the leader for a partition is
 * unavailable.  In this case, there is probably a serious problem with the health of the Kakfa cluster.  When the
 * assigned partition is not available, a {@code RuntimeException} is thrown, instead of breaking the "one lineage
 * contained within a single partition" guarantee.
 * </p>
 * <p>
 * If the RMap event does <em>not</em> have a progenitor lineage URI (this is the case if the event target type is
 * not a {@link RMapEventTargetType#DISCO}), it is simply assigned to a partition based on the hashcode of the event
 * object.  Because these objects do not participate in a lineage, there is no need to be concerned about which
 * partition they end up in.
 * </p>
 */
public class LineagePartitioner implements Partitioner {

    private static final Logger LOG = LoggerFactory.getLogger(LineagePartitioner.class);

    private static final String ERR_INCORRECT_TYPE = "Unable to assign a partition to object of type '%s' received " +
            "on topic '%s': only instances of RMapEvent can be assigned by this partitioner.";

    private static final String ERR_UNAVAILABLE_PARTITIONS = "Unable to assign RMap Event '%s' received on topic " +
            "'%s' to a partition, because partition id '%s' is not available (is the leader for '%s', partition %s " +
            "available?)";

    private static final String ERR_PARTITION_CALCULATION = "Assigning object of type '%s' to a partition failed " +
            "because an exception was encountered when calculating the lineage progenitor hashcode: %s";

    /**
     * Assigns a Kafka partition to the supplied RMap event based on the {@link RMapEvent#getLineageProgenitor() lineage
     * progenitor}.
     *
     * @param topic      the Kafka topic the record is being sent to
     * @param key        the record key
     * @param keyBytes   the byte representation of the key
     * @param value      the record value (an {@code RMapEvent})
     * @param valueBytes the byte representation of the value
     * @param cluster    encapsulates information about topics, nodes, and partitions in the Kafka cluster
     * @return the partition identifier (zero-based)
     */
    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {

        if (!(value instanceof RMapEvent)) {
            String msg = format(ERR_INCORRECT_TYPE, value.getClass().getName(), topic);
            LOG.error(msg);
            throw new RuntimeException(msg);
        }

        // Total number of partitions for the topic
        int partCount = cluster.partitionCountForTopic(topic);

        try {
            // The partition offset is a number between 0 and 'partCount - 1'.
            // According to the javadoc for the partition(...) method, the partition id is zero-based.
            // Therefore, the partition offset should be equal to the partition identifier.
            AtomicInteger partitionId;
            if (((RMapEvent) value).getEventTargetType() == RMapEventTargetType.DISCO) {
                partitionId = new AtomicInteger(calculatePartitionIdUsingLineage((RMapEvent) value, partCount));
            } else {
                partitionId = new AtomicInteger(calculatePartitionIdUsingHash(((RMapEvent) value), partCount));
            }

            return cluster.availablePartitionsForTopic(topic)
                    .stream()
                    .map(PartitionInfo::partition)
                    .filter(candidateId -> candidateId == partitionId.get())
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException(format(ERR_UNAVAILABLE_PARTITIONS,
                            value, topic, partitionId.get(), topic, partitionId.get())));
        } catch (Exception e) {
            String msg = format(ERR_PARTITION_CALCULATION, value.getClass().getName(), e.getMessage());
            LOG.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    /**
     * Calculates the partition identifier based on the hash of the RMapEvent's lineage progenitor URI.
     *
     * @param event RMap event objects that target DiSCOs
     * @param totalPartitionCountForTopic the total number of partitions configured for this topic
     * @return the partition identifier this event should be assigned to
     */
    static int calculatePartitionIdUsingLineage(RMapEvent event, int totalPartitionCountForTopic) {
        String normalizedLineageIri = event.getLineageProgenitor()
                .getStringValue()
                .trim()
                .toLowerCase();
        int partitionId = Math.abs(normalizedLineageIri.hashCode()) % totalPartitionCountForTopic;
        LOG.trace("Calculated partition id {} for event (target type: {}, lineage {}, uri {}) lineage hashcode {} " +
                        "and partition count {}", partitionId, event.getEventTargetType(), normalizedLineageIri,
                event.getId(), normalizedLineageIri.hashCode(), totalPartitionCountForTopic);
        return partitionId;
    }

    /**
     * Calculates the partition identifier based on the hash of the event object itself.  This method is used when
     * assigning a partition to an RMap event that does not have a lineage progenitor URI.
     *
     * @param event RMap event objects that target objects <em>other than</em> DiSCOs
     * @param totalPartitionCountForTopic the total number of partitions configured for this topic
     * @return the partition identifier this event should be assigned to
     */
    static int calculatePartitionIdUsingHash(RMapEvent event, int totalPartitionCountForTopic) {
        int partitionId = Math.abs(event.hashCode()) % totalPartitionCountForTopic;
        LOG.trace("Calculated partition id {} for event (target type: {}, uri {}) using event hashcode {} and " +
                        "partition count {}",
                partitionId, event.getEventTargetType(), event.getId(), event.hashCode(),
                totalPartitionCountForTopic);
        return partitionId;
    }

    @Override
    public void close() {
        // no-op
    }

    @Override
    public void configure(Map<String, ?> configs) {
        // no-op
    }
}
