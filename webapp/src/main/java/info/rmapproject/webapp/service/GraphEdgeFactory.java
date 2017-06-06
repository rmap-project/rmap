package info.rmapproject.webapp.service;

import info.rmapproject.webapp.domain.GraphEdge;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public interface GraphEdgeFactory {

    GraphEdge newGraphEdge();

    GraphEdge newGraphEdge(Integer source, Integer target, String label);

}
