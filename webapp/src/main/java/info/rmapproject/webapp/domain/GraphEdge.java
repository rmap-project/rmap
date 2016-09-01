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
	
	/** The source node ID. */
	private Integer source;
	
	/** The target node ID. */
	private Integer target;
	
	/** The edge label. */
	private String label;
	
	/** The edge shortlabel. */
	private String shortlabel;
	
	/** The target node type. */
	private String targetNodeType;
	
	/**
	 * Instantiates a new graph edge.
	 */
	public GraphEdge(){
	}
	
	/**
	 * Instantiates a new graph edge.
	 *
	 * @param source the source node ID
	 * @param target the target node ID
	 * @param label the edge label
	 */
	public GraphEdge(Integer source, Integer target, String label){
		setSource(source);
		setTarget(target);
		setLabel(label);
	}
	
	/**
	 * Gets the source node ID.
	 *
	 * @return the source node ID
	 */
	public Integer getSource() {
		return source;
	}
	
	/**
	 * Sets the source node ID
	 *
	 * @param source the new source node ID
	 */
	public void setSource(Integer source) {
		this.source = source;
	}
	
	/**
	 * Gets the target node ID
	 *
	 * @return the target node ID
	 */
	public Integer getTarget() {
		return target;
	}
	
	/**
	 * Sets the target node ID
	 *
	 * @param target the new target node ID
	 */
	public void setTarget(Integer target) {
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
	 * Sets the edge label.
	 *
	 * @param label the new edge label
	 */
	public void setLabel(String label) {
		this.label = label;
		if (label != null){
			setShortlabel(shortenLabel(label)); //update short label based on new label assignment	
		}
	}

	/**
	 * Gets the target node type.
	 *
	 * @return the target node type
	 */
	public String getTargetNodeType() {
		return targetNodeType;
	}

	/**
	 * Sets the target node type.
	 *
	 * @param targetNodeType the new target node type
	 */
	public void setTargetNodeType(String targetNodeType) {
		this.targetNodeType = targetNodeType;
	}

	/**
	 * Gets the edge shortlabel.
	 *
	 * @return the edge shortlabel
	 */
	public String getShortlabel() {
		if (shortlabel==null && label != null){
			setShortlabel(shortenLabel(label)); //update short label based on new label assignment			
		}
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
	
	/**
	 * Creates a short version of the label so it can be used for tidy graph display.
	 * By default the shortLabel method will be used to create the shortLabel each time setLabel is called
	 * To customize the short label, do a setShortLabel after setLabel
	 *
	 * @param label the full edge label
	 * @return the short edge label
	 */
	public String shortenLabel(String label) {
		if (label != null){
			label = WebappUtils.replaceNamespace(label);
			if (label.length() > Constants.MAX_EDGETEXT_LENGTH) {
				setShortlabel(label.substring(label.length() - Constants.MAX_EDGETEXT_LENGTH) + "...");
			}
		}
		return label;
	}

	
}
