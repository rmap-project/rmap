package info.rmapproject.webapp.service;

import info.rmapproject.webapp.domain.GraphEdge;
import info.rmapproject.webapp.domain.GraphNode;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public interface GraphEdgeFactory {

    GraphEdge newGraphEdge();

    GraphEdge newGraphEdge(GraphNode source, GraphNode target, String label);

}
