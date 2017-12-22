package info.rmapproject.indexing.solr.repository;

import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.indexing.solr.model.DiscoSolrDocument;

import java.util.function.BiFunction;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
interface EventMapper extends BiFunction<RMapEvent, DiscoSolrDocument, DiscoSolrDocument> {

}
