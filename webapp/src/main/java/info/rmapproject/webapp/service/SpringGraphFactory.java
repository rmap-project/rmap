package info.rmapproject.webapp.service;

import java.io.UnsupportedEncodingException;

import org.springframework.context.MessageSource;

import info.rmapproject.core.model.RMapTriple;
import info.rmapproject.webapp.domain.Graph;
import info.rmapproject.webapp.domain.GraphEdge;
import info.rmapproject.webapp.domain.GraphNode;
import info.rmapproject.webapp.domain.GraphNodeType;
import info.rmapproject.webapp.domain.TripleDisplayFormat;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class SpringGraphFactory implements
        GraphFactory, GraphNodeFactory, GraphEdgeFactory, GraphNodeTypeFactory, TripleDisplayFormatFactory {

    private MessageSource ontologies;

    private MessageSource typemappings;

    private MessageSource nodeTypes;

    @Override
    public Graph newGraph() {
        return new Graph(this, this, this);
    }

    @Override
    public GraphNode newGraphNode(Integer id, String name, String label, Integer weight, String type) {
        return new GraphNode(id, name, label, weight, type);
    }

    @Override
    public GraphNodeType newGraphNodeType(String type) {
        return new GraphNodeType(type, nodeTypes);
    }

    @Override
    public GraphEdge newGraphEdge() {
        return new GraphEdge();
    }

    @Override
    public GraphEdge newGraphEdge(GraphNode source, GraphNode target, String label) {
        return new GraphEdge(source, target, label);
    }

    @Override
    public TripleDisplayFormat newTripleDisplayFormat() {
        return new TripleDisplayFormat();
    }

    @Override
    public TripleDisplayFormat newTripleDisplayFormat(RMapTriple rmapTriple) {
        try {
            return new TripleDisplayFormat(rmapTriple);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public MessageSource getOntologies() {
        return ontologies;
    }

    public void setOntologies(MessageSource ontologies) {
        this.ontologies = ontologies;
    }

    public MessageSource getTypemappings() {
        return typemappings;
    }

    public void setTypemappings(MessageSource typemappings) {
        this.typemappings = typemappings;
    }

    public MessageSource getNodeTypes() {
        return nodeTypes;
    }

    public void setNodeTypes(MessageSource nodeTypes) {
        this.nodeTypes = nodeTypes;
    }
}
