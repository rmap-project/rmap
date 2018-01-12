package info.rmapproject.webapp.service;

import info.rmapproject.webapp.domain.GraphNode;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public interface GraphNodeFactory {

    /**
     * Instantiates a new graph node.
     *
     * @param id     the node id
     * @param name   the node name
     * @param weight the node weight
     * @param type   the node type
     */
    GraphNode newGraphNode(Integer id, String name, String label, Integer weight, String type);

}
