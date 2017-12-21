package info.rmapproject.indexing.solr.repository;

import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.indexing.IndexUtils;
import info.rmapproject.indexing.solr.model.DiscoSolrDocument;
import org.springframework.stereotype.Component;

import static info.rmapproject.indexing.IndexUtils.dateToString;
import static info.rmapproject.indexing.IndexUtils.notNull;

/**
 * Maps the properties of an {@code RMapEvent} object to fields in a Solr {@code DiscoSolrDocument}.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
@Component
class SimpleEventMapper implements EventMapper {

    @Override
    public DiscoSolrDocument apply(RMapEvent event, DiscoSolrDocument doc) {
        IndexUtils.assertNotNull(event, "RMapEvent must not be null.");

        if (doc == null) {
            doc = new DiscoSolrDocument();
        }

        if (notNull(event.getId())) {
            doc.setEventUri(event.getId().getStringValue());
        }
        if (notNull(event.getAssociatedAgent())) {
            doc.setEventAgentUri(event.getAssociatedAgent().getStringValue());
        }
        if (notNull(event.getDescription())) {
            doc.setEventDescription(event.getDescription().getStringValue());
        }
        if (notNull(event.getStartTime())) {
            doc.setEventStartTime(dateToString(event.getStartTime()));
        }
        if (notNull(event.getEndTime())) {
            doc.setEventEndTime(dateToString(event.getEndTime()));
        }
        if (notNull(event.getEventType())) {
            doc.setEventType(event.getEventType().name());
        }
        if (notNull(event.getLineageProgenitor())) {
            doc.setEventLineageProgenitorUri(event.getLineageProgenitor().getStringValue());
        }

        return doc;
    }

}
