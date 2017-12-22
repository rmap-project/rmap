/*******************************************************************************
 * Copyright 2017 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This software was produced as part of the RMap Project (http://rmap-project.info),
 * The RMap Project was funded by the Alfred P. Sloan Foundation and is a 
 * collaboration between Data Conservancy, Portico, and IEEE.
 *******************************************************************************/
package info.rmapproject.webapp.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.rmapproject.webapp.service.GraphEdgeFactory;
import info.rmapproject.webapp.service.GraphNodeFactory;
import info.rmapproject.webapp.service.GraphNodeTypeFactory;
import info.rmapproject.webapp.utils.Constants;

/**
 * Holds a Graph description.
 *
 * @author khanson
 */
public class Graph {
	
	/**  List of nodes. */
    private Map<String,GraphNode> nodes;
	
	/**  List of edges. */
    private List<GraphEdge> edges;
        
	/**  List of node types. */
    private Map<String,GraphNodeType> nodeTypes;
        
    /**Each node must be assigned a number that is unique within the context of the graph
    The counter keeps track of an incrementing number that is assigned to each node as it is added to the graph*/
    private Integer counter = 0;

    private GraphNodeTypeFactory nodeTypeFactory;

    private GraphNodeFactory nodeFactory;

    private GraphEdgeFactory edgeFactory;

	/**
	 * Instantiates a new graph.
	 */
	public Graph(GraphNodeFactory nodeFactory, GraphEdgeFactory edgeFactory, GraphNodeTypeFactory nodeTypeFactory){
		//initiate lists.
		this.nodes = new HashMap<String, GraphNode>();
		this.edges = new ArrayList<GraphEdge>();
		this.nodeTypes = new HashMap<String, GraphNodeType>();
		this.nodeTypeFactory = nodeTypeFactory;
		this.nodeFactory = nodeFactory;
		this.edgeFactory = edgeFactory;
	}
	
	/**
	 * Gets the graph's nodes.
	 *
	 * @return the nodes list
	 */
	public Map<String, GraphNode> getNodes() {
		return nodes;
	}
	
	/**
	 * Sets the graph's nodes.
	 *
	 * @param nodes the new list of nodes
	 */
	public void setNodes(Map<String, GraphNode> nodes) {
		this.nodes = nodes;
	}
	
	/**
	 * Gets the graph's edges.
	 *
	 * @return the list of graph edges
	 */
	public List<GraphEdge> getEdges() {
		return edges;
	}
	
	/**
	 * Sets the edges.
	 *
	 * @param edges the new list of graph edges
	 */
	public void setEdges(List<GraphEdge> edges) {
		this.edges = edges;
	}
	
	/**
	 * Gets a list of node types in the graph.
	 *
	 * @return the node types
	 */
	public Map<String, GraphNodeType> getNodeTypes() {
		return nodeTypes;
	}
	

	
	/**
	 * Creates GraphNode object and adds it to the Graph. 
	 *
	 * @param nodeName the node as a string
	 * @param nodeType the node type
	 * @throws Exception the exception
	 */
	public void addNode(String nodeName, String nodeType) throws Exception{
		addNode(nodeName, nodeName, nodeType);
	}
	
	/**
	 * Creates GraphNode object and adds it to the Graph. if the node already exists, it will 
	 * not update anything, just return the matching ID.
	 *
	 * @param nodeName the node as a string
	 * @param nodeLabel the node label as a string
	 * @param nodeType the node type
	 * @throws Exception the exception
	 */
	public void addNode(String nodeName, String nodeLabel, String nodeType) throws Exception{
		if (!nodes.containsKey(nodeName)) {
			int id = getNextId();
			nodes.put(nodeName, nodeFactory.newGraphNode(id, nodeName, nodeLabel, Constants.NODE_WEIGHT_INCREMENT, nodeType));
			addNodeType(nodeType); //only if it's a new value
		}
	}

	/**
	 * Adds a graph edge.
	 *
	 * @param edge the edge
	 */
	public void addEdge(String sourceNode, String targetNode, String edgeLabel)	{
		GraphEdge edge = edgeFactory.newGraphEdge(nodes.get(sourceNode), nodes.get(targetNode), edgeLabel);
		addEdge(edge);
	}
	
	/**
	 * Adds a graph edge.
	 *
	 * @param edge the edge
	 */
	public void addEdge(GraphEdge edge)	{
		edges.add(edge);
		//update node weights
		edge.getSource().setWeight(edge.getSource().getWeight()+Constants.NODE_WEIGHT_INCREMENT);
	}
		
	/**
	 * Gets the next node id.
	 *
	 * @return the next id
	 */
	private Integer getNextId(){
		this.counter = this.counter+1;
		return this.counter;
	}

	/**
	 * Add type node to list, but only if it's not already in there.
	 *
	 * @param sType the type as a string
	 */
	private void addNodeType(String sType) {
		if (sType!=null && sType.length()>0
				&& !nodeTypes.containsKey(sType)){
			GraphNodeType type = nodeTypeFactory.newGraphNodeType(sType);
			this.nodeTypes.put(sType, type);
		}
	}

}
