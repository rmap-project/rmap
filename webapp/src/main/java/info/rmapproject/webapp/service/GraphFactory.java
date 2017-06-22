package info.rmapproject.webapp.service;

import info.rmapproject.webapp.domain.Graph;
import info.rmapproject.webapp.domain.GraphNodeType;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public interface GraphFactory {

    Graph newGraph();

}
