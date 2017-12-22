package info.rmapproject.indexing.kafka;

import info.rmapproject.core.model.agent.RMapAgent;
import info.rmapproject.core.model.disco.RMapDiSCO;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.indexing.solr.repository.IndexDTO;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class KafkaDTO extends IndexDTO {

    private final static long serialVersionUID = 1;

    private String topic;

    private int partition;

    private long offset;

    public KafkaDTO(RMapEvent event, RMapAgent agent, RMapDiSCO sourceDisco, RMapDiSCO targetDisco) {
        super(event, agent, sourceDisco, targetDisco);
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        KafkaDTO kafkaDTO = (KafkaDTO) o;

        if (partition != kafkaDTO.partition) return false;
        if (offset != kafkaDTO.offset) return false;
        return topic != null ? topic.equals(kafkaDTO.topic) : kafkaDTO.topic == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (topic != null ? topic.hashCode() : 0);
        result = 31 * result + partition;
        result = 31 * result + (int) (offset ^ (offset >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "KafkaDTO{" +
                "topic='" + topic + '\'' +
                ", partition=" + partition +
                ", offset=" + offset +
                "} " + super.toString();
    }
}
