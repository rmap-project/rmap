package info.rmapproject.indexing.solr.repository;

import info.rmapproject.core.model.disco.RMapDiSCO;
import info.rmapproject.indexing.solr.model.DiscoSolrDocument;

import java.util.function.BiFunction;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
interface DiscoMapper extends BiFunction<RMapDiSCO, DiscoSolrDocument, DiscoSolrDocument> {

}
