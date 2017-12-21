package info.rmapproject.indexing.solr.model;

import static info.rmapproject.indexing.IndexUtils.assertValidUri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.SolrDocument;

/**
 * Domain object that represents Solr documents managed by the {@code discos} core.  See the Solr schema for the
 * {@code discos} core for more details. The {@code DiscosSolrDocument} contains fields for:
 * <ul>
 *     <li>An {@link info.rmapproject.core.model.event.RMapEvent event}</li>
 *     <li>A DiSCO that was the {@link info.rmapproject.indexing.IndexUtils.EventDirection#SOURCE source} or {@link info.rmapproject.indexing.IndexUtils.EventDirection#TARGET target} of the event</li>
 *     <li>The {@link info.rmapproject.core.model.agent.RMapAgent agent} that was associated with the event</li>
 *     <li>Arbitrary key/value pairs that are considered as opaque {@link #getMetadata() metadata}</li>
 * </ul>
 * Since {@code DiscoSolrDocument} is a {@link KafkaMetadata Kafka metadata} document, it carries fields related to the
 * Kafka topic, partition, and record offset that contributed to the information in this document.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
@SolrDocument(collection = "discos")
public class DiscoSolrDocument implements KafkaMetadata {

    /**
     * Name of the Solr core which stores instances of this document
     */
    public static final String CORE_NAME = "discos";

    /**
     * Field name containing the Solr document id
     */
    public static final String DOC_ID = "doc_id";

    /**
     * Field name containing the timestamp of the last update of the Solr document
     */
    public static final String DOC_LAST_UPDATED = "doc_last_updated";

    /**
     * Field name containing the status of the DiSCO
     */
    public static final String DISCO_STATUS = "disco_status";

    /**
     * Field name containing the uri of the DiSCO
     */
    public static final String DISCO_URI = "disco_uri";

    /**
     * Field name containing the uri of the lineage of the DiSCO
     */
    public static final String EVENT_LINEAGE_PROGENITOR_URI = "event_lineage_progenitor_uri";

    @Id
    @Field(DOC_ID)
    private String docId;

    @Field(DOC_LAST_UPDATED)
    private long docLastUpdated;

    @Field(DISCO_URI)
    private String discoUri;

    @Field("disco_creator_uri")
    private String discoCreatorUri;

    @Field("disco_description")
    private String discoDescription;

    @Field("disco_providerid")
    private String discoProviderid;

    @Field("disco_aggregated_resource_uris")
    private List<String> discoAggregatedResourceUris;

    @Field("disco_provenance_uri")
    private String discoProvenanceUri;

    @Field("disco_related_statements")
    private List<String> discoRelatedStatements;

    @Field(DISCO_STATUS)
    private String discoStatus;

    @Field("disco_event_direction")
    private String discoEventDirection;


    @Field("event_uri")
    private String eventUri;

    @Field("event_agent_uri")
    private String eventAgentUri;

    @Field("event_start_time")
    private String eventStartTime;

    @Field("event_end_time")
    private String eventEndTime;

    @Field("event_description")
    private String eventDescription;

    @Field("event_type")
    private String eventType;

    @Field("event_source_object_uris")
    private List<String> eventSourceObjectUris;

    @Field("event_target_object_uris")
    private List<String> eventTargetObjectUris;

    @Field(EVENT_LINEAGE_PROGENITOR_URI)
    private String eventLineageProgenitorUri;


    @Field("agent_uri")
    private String agentUri;

    @Field("agent_provider_uri")
    private String agentProviderUri;

    @Field("agent_name")
    private String agentName;

    @Field("md_*")
    private Map<String, String> metadata;


    /**
     * Kafka metadata field representing the Kafka topic that contributed to information in this document.
     */
    @Field(KafkaMetadata.KAFKA_TOPIC)
    private String kafkaTopic;

    /**
     * Kafka metadata field representing the Kafka partition that contributed to information in this document.
     */
    @Field(KafkaMetadata.KAFKA_PARTITION)
    private int kafkaPartition;

    /**
     * Kafka metadata field representing the offset of the Kafka record in the {@link #KAFKA_TOPIC topic} and
     * {@link #KAFKA_PARTITION partition} that contributed to information in this document.
     */
    @Field(KafkaMetadata.KAFKA_OFFSET)
    private long kafkaOffset;

    /**
     * Create an empty {@code DiscoSolrDocument}
     */
    public DiscoSolrDocument() {

    }

    /**
     * Create a {@code DiscoSolrDocument}, copying the state of this instance from the {@code prototype}.  Strings
     * and primitives are simply assigned from the prototype; collections are deep-copied.
     *
     * @param prototype provides the state for this instance
     */
    public DiscoSolrDocument(DiscoSolrDocument prototype) {
        this.docId = prototype.docId;
        this.docLastUpdated = prototype.docLastUpdated;

        this.discoUri = prototype.discoUri;
        this.discoCreatorUri = prototype.discoCreatorUri;
        this.discoDescription = prototype.discoDescription;
        this.discoProviderid = prototype.discoProviderid;
        this.discoAggregatedResourceUris = (prototype.discoAggregatedResourceUris != null) ? new ArrayList<>(prototype.discoAggregatedResourceUris) : null;
        this.discoProvenanceUri = prototype.discoProvenanceUri;
        this.discoRelatedStatements = (prototype.discoRelatedStatements != null) ? new ArrayList<>(prototype.discoRelatedStatements) : null;
        this.discoStatus = prototype.discoStatus;
        this.discoEventDirection = prototype.discoEventDirection;

        this.eventUri = prototype.eventUri;
        this.eventAgentUri = prototype.eventAgentUri;
        this.eventStartTime = prototype.eventStartTime;
        this.eventEndTime = prototype.eventEndTime;
        this.eventDescription = prototype.eventDescription;
        this.eventType = prototype.eventType;
        this.eventSourceObjectUris = (prototype.eventSourceObjectUris != null) ? new ArrayList<>(prototype.eventSourceObjectUris) : null;
        this.eventTargetObjectUris = (prototype.eventTargetObjectUris != null) ? new ArrayList<>(prototype.eventTargetObjectUris) : null;
        this.eventLineageProgenitorUri = prototype.eventLineageProgenitorUri;

        this.agentUri = prototype.agentUri;
        this.agentProviderUri = prototype.agentProviderUri;
        this.agentName = prototype.agentName;

        this.metadata = (prototype.metadata != null) ? new HashMap<>(prototype.metadata) : null;

        this.kafkaTopic = prototype.kafkaTopic;
        this.kafkaPartition = prototype.kafkaPartition;
        this.kafkaOffset = prototype.kafkaOffset;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public long getDocLastUpdated() {
        return docLastUpdated;
    }

    public void setDocLastUpdated(long docLastUpdated) {
        this.docLastUpdated = docLastUpdated;
    }

    public String getDiscoUri() {
        return discoUri;
    }

    public void setDiscoUri(String discoUri) {
        assertValidUri(discoUri);
        this.discoUri = discoUri;
    }

    public String getDiscoCreatorUri() {
        return discoCreatorUri;
    }

    public void setDiscoCreatorUri(String discoCreatorUri) {
        assertValidUri(discoCreatorUri);
        this.discoCreatorUri = discoCreatorUri;
    }

    public String getDiscoDescription() {
        return discoDescription;
    }

    public void setDiscoDescription(String discoDescription) {
        this.discoDescription = discoDescription;
    }

    public String getDiscoProviderid() {
        return discoProviderid;
    }

    public void setDiscoProviderid(String discoProviderid) {
        assertValidUri(discoProviderid);
        this.discoProviderid = discoProviderid;
    }

    public List<String> getDiscoAggregatedResourceUris() {
        return discoAggregatedResourceUris;
    }

    public void setDiscoAggregatedResourceUris(List<String> discoAggregatedResourceUris) {
        assertValidUri(discoAggregatedResourceUris);
        this.discoAggregatedResourceUris = discoAggregatedResourceUris;
    }

    public String getDiscoProvenanceUri() {
        return discoProvenanceUri;
    }

    public void setDiscoProvenanceUri(String discoProvenanceUri) {
        assertValidUri(discoProvenanceUri);
        this.discoProvenanceUri = discoProvenanceUri;
    }

    public List<String> getDiscoRelatedStatements() {
        return discoRelatedStatements;
    }

    public void setDiscoRelatedStatements(List<String> discoRelatedStatements) {
        this.discoRelatedStatements = discoRelatedStatements;
    }

    public String getDiscoStatus() {
        return discoStatus;
    }

    public void setDiscoStatus(String discoStatus) {
        this.discoStatus = discoStatus;
    }

    public String getEventUri() {
        return eventUri;
    }

    public void setEventUri(String eventUri) {
        assertValidUri(eventUri);
        this.eventUri = eventUri;
    }

    public String getEventAgentUri() {
        return eventAgentUri;
    }

    public void setEventAgentUri(String eventAgentUri) {
        assertValidUri(eventAgentUri);
        this.eventAgentUri = eventAgentUri;
    }

    public String getEventStartTime() {
        return eventStartTime;
    }

    public void setEventStartTime(String eventStartTime) {
        this.eventStartTime = eventStartTime;
    }

    public String getEventEndTime() {
        return eventEndTime;
    }

    public void setEventEndTime(String eventEndTime) {
        this.eventEndTime = eventEndTime;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public List<String> getEventSourceObjectUris() {
        return eventSourceObjectUris;
    }

    public void setEventSourceObjectUris(List<String> eventSourceObjectUris) {
        assertValidUri(eventSourceObjectUris);
        this.eventSourceObjectUris = eventSourceObjectUris;
    }

    public List<String> getEventTargetObjectUris() {
        return eventTargetObjectUris;
    }

    public void setEventTargetObjectUris(List<String> eventTargetObjectUris) {
        assertValidUri(eventTargetObjectUris);
        this.eventTargetObjectUris = eventTargetObjectUris;
    }

    public String getDiscoEventDirection() {
        return discoEventDirection;
    }

    public void setDiscoEventDirection(String discoEventDirection) {
        this.discoEventDirection = discoEventDirection;
    }

    public String getEventLineageProgenitorUri() {
        return eventLineageProgenitorUri;
    }

    public void setEventLineageProgenitorUri(String eventLineageProgenitorUri) {
        this.eventLineageProgenitorUri = eventLineageProgenitorUri;
    }

    public String getAgentUri() {
        return agentUri;
    }

    public void setAgentUri(String agentUri) {
        assertValidUri(agentUri);
        this.agentUri = agentUri;
    }

    public String getAgentProviderUri() {
        return agentProviderUri;
    }

    public void setAgentProviderUri(String agentProviderUri) {
        assertValidUri(agentProviderUri);
        this.agentProviderUri = agentProviderUri;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public String getKafkaTopic() {
        return kafkaTopic;
    }

    public void setKafkaTopic(String kafkaTopic) {
        this.kafkaTopic = kafkaTopic;
    }

    public int getKafkaPartition() {
        return kafkaPartition;
    }

    public void setKafkaPartition(int kafkaPartition) {
        this.kafkaPartition = kafkaPartition;
    }

    public long getKafkaOffset() {
        return kafkaOffset;
    }

    public void setKafkaOffset(long kafkaOffset) {
        this.kafkaOffset = kafkaOffset;
    }

    /**
     * Convenience class for building a {@code DiscoSolrDocument} using a fluent API.
     */
    public static class Builder {
        private DiscoSolrDocument instance;

        /**
         * Instantiates a new builder with no state
         */
        public Builder() {

        }

        /**
         * Instantiates a new builder using the state supplied from the prototype
         *
         * @param prototype used as the initial state for this builder
         */
        public Builder(DiscoSolrDocument prototype) {
            instance = new DiscoSolrDocument(prototype);
        }

        public Builder docId(String docId) {
            instantiateIfNull();
            instance.setDocId(docId);
            return this;
        }

        public Builder docLastUpdated(long docLastUpdated) {
            instantiateIfNull();
            instance.setDocLastUpdated(docLastUpdated);
            return this;
        }

        public Builder discoUri(String discoUri) {
            instantiateIfNull();
            instance.setDiscoUri(discoUri);
            return this;
        }

        public Builder discoCreatorUri(String discoCreatorUri) {
            instantiateIfNull();
            instance.setDiscoCreatorUri(discoCreatorUri);
            return this;
        }

        public Builder discoDescription(String discoDescription) {
            instantiateIfNull();
            instance.setDiscoDescription(discoDescription);
            return this;
        }

        public Builder discoProviderid(String discoProviderid) {
            instantiateIfNull();
            instance.setDiscoProviderid(discoProviderid);
            return this;
        }

        public Builder discoAggregatedResourceUris(List<String> discoAggregatedResourceUris) {
            instantiateIfNull();
            instance.setDiscoAggregatedResourceUris(discoAggregatedResourceUris);
            return this;
        }

        public Builder discoProvenanceUri(String discoProvenanceUri) {
            instantiateIfNull();
            instance.setDiscoProvenanceUri(discoProvenanceUri);
            return this;
        }

        public Builder discoRelatedStatements(List<String> discoRelatedStatements) {
            instantiateIfNull();
            instance.setDiscoRelatedStatements(discoRelatedStatements);
            return this;
        }

        public Builder discoEventDirection(String discoEventDirection) {
            instantiateIfNull();
            instance.setDiscoEventDirection(discoEventDirection);
            return this;
        }

        public Builder discoStatus(String discoStatus) {
            instantiateIfNull();
            instance.setDiscoStatus(discoStatus);
            return this;
        }

        public Builder eventUri(String eventUri) {
            instantiateIfNull();
            instance.setEventUri(eventUri);
            return this;
        }

        public Builder eventAgentUri(String eventAgentUri) {
            instantiateIfNull();
            instance.setEventAgentUri(eventAgentUri);
            return this;
        }

        public Builder eventStartTime(String eventStartTime) {
            instantiateIfNull();
            instance.setEventStartTime(eventStartTime);
            return this;
        }

        public Builder eventEndTime(String eventEndTime) {
            instantiateIfNull();
            instance.setEventEndTime(eventEndTime);
            return this;
        }

        public Builder eventDescription(String eventDescription) {
            instantiateIfNull();
            instance.setEventDescription(eventDescription);
            return this;
        }

        public Builder eventType(String eventType) {
            instantiateIfNull();
            instance.setEventType(eventType);
            return this;
        }

        public Builder eventSourceObjectUris(List<String> eventSourceObjectUris) {
            instantiateIfNull();
            instance.setEventSourceObjectUris(eventSourceObjectUris);
            return this;
        }

        public Builder eventTargetObjectUris(List<String> eventTargetObjectUris) {
            instantiateIfNull();
            instance.setEventTargetObjectUris(eventTargetObjectUris);
            return this;
        }

        public Builder eventLineageUri(String eventLineageUri) {
            instantiateIfNull();
            instance.setEventLineageProgenitorUri(eventLineageUri);
            return this;
        }

        public Builder agentUri(String agentUri) {
            instantiateIfNull();
            instance.setAgentUri(agentUri);
            return this;
        }

        public Builder agentProviderUri(String agentProviderUri) {
            instantiateIfNull();
            instance.setAgentProviderUri(agentProviderUri);
            return this;
        }

        public Builder agentName(String agentName) {
            instantiateIfNull();
            instance.setAgentName(agentName);
            return this;
        }

        public Builder metadata(Map<String, String> metadata) {
            instantiateIfNull();
            instance.setMetadata(metadata);
            return this;
        }

        public Builder kafkaTopic(String topic) {
            instantiateIfNull();
            instance.setKafkaTopic(topic);
            return this;
        }

        public Builder kafkaPartition(int partition) {
            instantiateIfNull();
            instance.setKafkaPartition(partition);
            return this;
        }

        public Builder kafkaOffset(long offset) {
            instantiateIfNull();
            instance.setKafkaOffset(offset);
            return this;
        }

        public DiscoSolrDocument build() {
            instantiateIfNull();
            return instance;
        }

        private void instantiateIfNull() {
            if (instance == null) {
                instance = new DiscoSolrDocument();
            }
        }

        private void reset() {
            instance = null;
        }
    }

    @Override
    public String toString() {
        return "DiscoSolrDocument{" +
                "docId='" + docId + '\'' +
                ", docLastUpdated=" + docLastUpdated +
                ", discoUri='" + discoUri + '\'' +
                ", discoCreatorUri='" + discoCreatorUri + '\'' +
                ", discoDescription='" + discoDescription + '\'' +
                ", discoProviderid='" + discoProviderid + '\'' +
                ", discoAggregatedResourceUris=" + discoAggregatedResourceUris +
                ", discoProvenanceUri='" + discoProvenanceUri + '\'' +
                ", discoRelatedStatements=" + discoRelatedStatements +
                ", discoStatus='" + discoStatus + '\'' +
                ", discoEventDirection='" + discoEventDirection + '\'' +
                ", eventUri='" + eventUri + '\'' +
                ", eventAgentUri='" + eventAgentUri + '\'' +
                ", eventStartTime='" + eventStartTime + '\'' +
                ", eventEndTime='" + eventEndTime + '\'' +
                ", eventDescription='" + eventDescription + '\'' +
                ", eventType='" + eventType + '\'' +
                ", eventSourceObjectUris=" + eventSourceObjectUris +
                ", eventTargetObjectUris=" + eventTargetObjectUris +
                ", eventLineageProgenitorUri='" + eventLineageProgenitorUri + '\'' +
                ", agentUri='" + agentUri + '\'' +
                ", agentProviderUri='" + agentProviderUri + '\'' +
                ", agentName='" + agentName + '\'' +
                ", metadata=" + metadata +
                ", kafkaTopic='" + kafkaTopic + '\'' +
                ", kafkaPartition=" + kafkaPartition +
                ", kafkaOffset=" + kafkaOffset +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DiscoSolrDocument that = (DiscoSolrDocument) o;

        if (docLastUpdated != that.docLastUpdated) return false;
        if (kafkaPartition != that.kafkaPartition) return false;
        if (kafkaOffset != that.kafkaOffset) return false;
        if (docId != null ? !docId.equals(that.docId) : that.docId != null) return false;
        if (discoUri != null ? !discoUri.equals(that.discoUri) : that.discoUri != null) return false;
        if (discoCreatorUri != null ? !discoCreatorUri.equals(that.discoCreatorUri) : that.discoCreatorUri != null)
            return false;
        if (discoDescription != null ? !discoDescription.equals(that.discoDescription) : that.discoDescription != null)
            return false;
        if (discoProviderid != null ? !discoProviderid.equals(that.discoProviderid) : that.discoProviderid != null)
            return false;
        if (discoAggregatedResourceUris != null ? !discoAggregatedResourceUris.equals(that.discoAggregatedResourceUris) : that.discoAggregatedResourceUris != null)
            return false;
        if (discoProvenanceUri != null ? !discoProvenanceUri.equals(that.discoProvenanceUri) : that.discoProvenanceUri != null)
            return false;
        if (discoRelatedStatements != null ? !discoRelatedStatements.equals(that.discoRelatedStatements) : that.discoRelatedStatements != null)
            return false;
        if (discoStatus != null ? !discoStatus.equals(that.discoStatus) : that.discoStatus != null) return false;
        if (discoEventDirection != null ? !discoEventDirection.equals(that.discoEventDirection) : that.discoEventDirection != null)
            return false;
        if (eventUri != null ? !eventUri.equals(that.eventUri) : that.eventUri != null) return false;
        if (eventAgentUri != null ? !eventAgentUri.equals(that.eventAgentUri) : that.eventAgentUri != null)
            return false;
        if (eventStartTime != null ? !eventStartTime.equals(that.eventStartTime) : that.eventStartTime != null)
            return false;
        if (eventEndTime != null ? !eventEndTime.equals(that.eventEndTime) : that.eventEndTime != null) return false;
        if (eventDescription != null ? !eventDescription.equals(that.eventDescription) : that.eventDescription != null)
            return false;
        if (eventType != null ? !eventType.equals(that.eventType) : that.eventType != null) return false;
        if (eventSourceObjectUris != null ? !eventSourceObjectUris.equals(that.eventSourceObjectUris) : that.eventSourceObjectUris != null)
            return false;
        if (eventTargetObjectUris != null ? !eventTargetObjectUris.equals(that.eventTargetObjectUris) : that.eventTargetObjectUris != null)
            return false;
        if (eventLineageProgenitorUri != null ? !eventLineageProgenitorUri.equals(that.eventLineageProgenitorUri) : that.eventLineageProgenitorUri != null)
            return false;
        if (agentUri != null ? !agentUri.equals(that.agentUri) : that.agentUri != null) return false;
        if (agentProviderUri != null ? !agentProviderUri.equals(that.agentProviderUri) : that.agentProviderUri != null)
            return false;
        if (agentName != null ? !agentName.equals(that.agentName) : that.agentName != null)
            return false;
        if (metadata != null ? !metadata.equals(that.metadata) : that.metadata != null) return false;
        return kafkaTopic != null ? kafkaTopic.equals(that.kafkaTopic) : that.kafkaTopic == null;
    }

    @Override
    public int hashCode() {
        int result = docId != null ? docId.hashCode() : 0;
        result = 31 * result + (int) (docLastUpdated ^ (docLastUpdated >>> 32));
        result = 31 * result + (discoUri != null ? discoUri.hashCode() : 0);
        result = 31 * result + (discoCreatorUri != null ? discoCreatorUri.hashCode() : 0);
        result = 31 * result + (discoDescription != null ? discoDescription.hashCode() : 0);
        result = 31 * result + (discoProviderid != null ? discoProviderid.hashCode() : 0);
        result = 31 * result + (discoAggregatedResourceUris != null ? discoAggregatedResourceUris.hashCode() : 0);
        result = 31 * result + (discoProvenanceUri != null ? discoProvenanceUri.hashCode() : 0);
        result = 31 * result + (discoRelatedStatements != null ? discoRelatedStatements.hashCode() : 0);
        result = 31 * result + (discoStatus != null ? discoStatus.hashCode() : 0);
        result = 31 * result + (discoEventDirection != null ? discoEventDirection.hashCode() : 0);
        result = 31 * result + (eventUri != null ? eventUri.hashCode() : 0);
        result = 31 * result + (eventAgentUri != null ? eventAgentUri.hashCode() : 0);
        result = 31 * result + (eventStartTime != null ? eventStartTime.hashCode() : 0);
        result = 31 * result + (eventEndTime != null ? eventEndTime.hashCode() : 0);
        result = 31 * result + (eventDescription != null ? eventDescription.hashCode() : 0);
        result = 31 * result + (eventType != null ? eventType.hashCode() : 0);
        result = 31 * result + (eventSourceObjectUris != null ? eventSourceObjectUris.hashCode() : 0);
        result = 31 * result + (eventTargetObjectUris != null ? eventTargetObjectUris.hashCode() : 0);
        result = 31 * result + (eventLineageProgenitorUri != null ? eventLineageProgenitorUri.hashCode() : 0);
        result = 31 * result + (agentUri != null ? agentUri.hashCode() : 0);
        result = 31 * result + (agentProviderUri != null ? agentProviderUri.hashCode() : 0);
        result = 31 * result + (agentName != null ? agentName.hashCode() : 0);
        result = 31 * result + (metadata != null ? metadata.hashCode() : 0);
        result = 31 * result + (kafkaTopic != null ? kafkaTopic.hashCode() : 0);
        result = 31 * result + kafkaPartition;
        result = 31 * result + (int) (kafkaOffset ^ (kafkaOffset >>> 32));
        return result;
    }
}
