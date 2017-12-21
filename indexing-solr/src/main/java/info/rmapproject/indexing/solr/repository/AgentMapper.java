package info.rmapproject.indexing.solr.repository;

import info.rmapproject.core.model.agent.RMapAgent;
import info.rmapproject.indexing.solr.model.DiscoSolrDocument;

import java.util.function.BiFunction;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
interface AgentMapper extends BiFunction<RMapAgent, DiscoSolrDocument, DiscoSolrDocument> {

}
