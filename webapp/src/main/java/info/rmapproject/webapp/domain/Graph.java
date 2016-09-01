/*******************************************************************************
 * Copyright 2016 Johns Hopkins University
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

import info.rmapproject.webapp.utils.Constants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Holds a Graph description.
 *
 * @author khanson
 */
public class Graph {

	/**  Unique list of nodes stored to prevent duplicate nodes being added. */
    private Set<String> uniqueNodes;  
	
	/**  List of nodes. */
    private List<GraphNode> nodes;
	
	/**  List of edges. */
    private List<GraphEdge> edges;
    
    /** Unique list of node types to prevent duplicate types*. */
    private Set<String> uniqueNodeTypes;
    
	/**  List of node types. */
    private List<GraphNodeType> nodeTypes;
        
    //Each node must be assigned a number that is unique within the context of the graph
    /** The counter. */
    //The counter keeps track of an incrementing number that is assigned to each node as it is added to the graph
    private Integer counter = 0; 

	/**
	 * Instantiates a new graph.
	 */
	public Graph(){
		//initiate lists.
		this.uniqueNodes = new HashSet<String>();
		this.uniqueNodeTypes = new HashSet<String>();
		this.nodes = new ArrayList<GraphNode>();
		this.edges = new ArrayList<GraphEdge>();
		this.nodeTypes = new ArrayList<GraphNodeType>();
	}
	
	/**
	 * Gets the graph's nodes.
	 *
	 * @return the nodes list
	 */
	public List<GraphNode> getNodes() {
		return nodes;
	}
	
	/**
	 * Sets the graph's nodes.
	 *
	 * @param nodes the new list of nodes
	 */
	public void setNodes(List<GraphNode> nodes) {
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
	 * Gets the unique nodes.
	 *
	 * @return the unique set of nodes in the graph
	 */
	public Set<String> getUniqueNodes() {
		return uniqueNodes;
	}
	
	/**
	 * Gets a list of node types in the graph.
	 *
	 * @return the node types
	 */
	public List<GraphNodeType> getNodeTypes() {
		return nodeTypes;
	}
	
	/**
	 * Creates GraphNode object and adds it to the Graph.
	 *
	 * @param sNode the node as a string
	 * @param nodeType the node type
	 * @return the node ID as an integer, unique within the graph
	 * @throws Exception the exception
	 */
	public Integer addNode(String sNode, String nodeType) throws Exception{
		Integer id = 0;
		if (!uniqueNodes.contains(sNode)) {
			id = getNextId();
			uniqueNodes.add(sNode);	
			nodes.add(new GraphNode(id, sNode, Constants.NODE_WEIGHT_INCREMENT, nodeType));
			addNodeType(nodeType); //only if it's a new value
		}
		else {
			//find matching node, add to weight
			for (GraphNode node:this.nodes) {
				if (node.getName().equals(sNode)){
					node.setWeight(node.getWeight() + Constants.NODE_WEIGHT_INCREMENT);
					id = node.getId();
				}
			}
		}
		return id;
	}
	
	/**
	 * Adds a graph edge.
	 *
	 * @param edge the edge
	 */
	public void addEdge(GraphEdge edge)	{
		edges.add(edge);
	}
			
	/**
	 * Creates GraphEdge object and adds it to the Graph.
	 *
	 * @param sourceKey the source node key
	 * @param targetKey the target node key
	 * @param label the label for the edge
	 * @param sourceNodeType the source node type
	 * @param targetNodeType the target node type
	 * @throws Exception the exception
	 */
	public void addEdge(String sourceKey, String targetKey, String label, 
							String sourceNodeType, String targetNodeType) throws Exception {
		GraphEdge edge = new GraphEdge();
		targetKey = targetKey.replaceAll("[\n\r]", "");
		targetKey = targetKey.replaceAll("[ ]+", " ");
		Integer source = addNode(sourceKey, sourceNodeType);
		Integer target = addNode(targetKey, targetNodeType);
		edge.setLabel(label);
		edge.setSource(source);
		edge.setTarget(target);
		edge.setTargetNodeType(targetNodeType);
		addEdge(edge);
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
				&& !uniqueNodeTypes.contains(sType)){
			GraphNodeType type = new GraphNodeType(sType);
			this.nodeTypes.add(type);
			this.uniqueNodeTypes.add(sType);//for detecting duplicates
		}
	}

}
