/*******************************************************************************
 * Copyright 2018 Johns Hopkins University
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
import info.rmapproject.webapp.utils.WebappUtils;

import java.io.Serializable;

/**
 * Holds a Graph Edge description.
 *
 * @author khanson
 */
public class GraphEdge implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The source node. */
	private GraphNode source;
	
	/** The target node. */
	private GraphNode target;
	
	/** The edge label. */
	private String label;
	
	/** The edge shortlabel. */
	private String shortlabel;
	
	/**
	 * Instantiates a new graph edge.
	 */
	public GraphEdge() {

	}
	
	/**
	 * Instantiates a new graph edge.
	 *
	 * @param source the source node
	 * @param target the target node
	 * @param label the edge label
	 */
	public GraphEdge(GraphNode source, GraphNode target, String label) {
		setSource(source);
		setTarget(target);
		setLabel(label);
	}
	
	/**
	 * Gets the source node ID.
	 *
	 * @return the source node ID
	 */
	public GraphNode getSource() {
		return source;
	}
	
	/**
	 * Sets the source node ID
	 *
	 * @param source the new source node ID
	 */
	public void setSource(GraphNode source) {
		this.source = source;
	}
	
	/**
	 * Gets the target node ID
	 *
	 * @return the target node ID
	 */
	public GraphNode getTarget() {
		return target;
	}
	
	/**
	 * Sets the target node ID
	 *
	 * @param target the new target node ID
	 */
	public void setTarget(GraphNode target) {
		this.target = target;
	}
	
	/**
	 * Gets the edge label.
	 *
	 * @return the edge label
	 */
	public String getLabel() {
		return label;
	}
	
	/**
	 * Sets the edge label and uses this to populate the short label.
	 *
	 * @param label the new edge label
	 */
	public void setLabel(String label) {
		this.label = label;
		String shortlabel = label;
		if (shortlabel != null){
			shortlabel = WebappUtils.removeNamespace(shortlabel);
			shortlabel = WebappUtils.ellipsize(shortlabel, Constants.MAX_EDGETEXT_LENGTH);
		}
		setShortlabel(shortlabel);	
	}

	/**
	 * Gets the edge shortlabel.
	 *
	 * @return the edge shortlabel
	 */
	public String getShortlabel() {
		return shortlabel;
	}
	

	/**
	 * Sets the edge shortlabel.
	 *
	 * @param shortlabel the new edge shortlabel
	 */
	public void setShortlabel(String shortlabel) {
		this.shortlabel = shortlabel;
	}

	
}
