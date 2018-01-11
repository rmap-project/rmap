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

import info.rmapproject.webapp.utils.Constants;
import info.rmapproject.webapp.utils.WebappUtils;

import java.io.Serializable;

/**
 * Holds a Graph Node description.
 *
 * @author khanson
 */
public class GraphNode implements Serializable{

	/**
	 * Instantiates a new graph node.
	 */
	public GraphNode(){
	}
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
    /** ID of node - must be unique in the context of a graph. */
    private Integer id;
    
    /** Name of node in RMap, this is typically a URI. */
	private String name;
    
    /** Label to be displayed next to the node. */
	private String label;
	
	/** Shortened version of the node label for tidy display. */
	private String shortlabel; 
	
	/** Node weight. */
	private Integer weight;
	
	/** Node type*. */
	private String type;
	
	/**
	 * Instantiates a new graph node.
	 *
	 * @param id the node id
	 * @param name the node name
	 * @param weight the node weight
	 * @param type the node type
	 */
	public GraphNode(Integer id, String name, String label, Integer weight, String type){
		setId(id);
		setType(type);
		setName(name);
		setLabel(label);
		setWeight(weight);
	}

	/**
	 * Gets the node id.
	 *
	 * @return the node id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Sets the node id.
	 *
	 * @param id the new node id
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Gets the node name.
	 *
	 * @return the node name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the node name.
	 *
	 * @param name the new node name
	 */
	public void setName(String name) {
		name=name.replace("\\", "\\\\");
		name=name.replace("'", "\\'");
		this.name = name;		
	}

	/**
	 * Gets the node weight.
	 *
	 * @return the node weight
	 */
	public Integer getWeight() {
		return weight;
	}

	/**
	 * Sets the node weight.
	 *
	 * @param weight the new node weight
	 */
	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	/**
	 * Gets the node type.
	 *
	 * @return the node type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the node type.
	 *
	 * @param type the new node type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Gets the node label.
	 *
	 * @return the node label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the node label.
	 *
	 * @param label the new node label
	 */
	public void setLabel(String label) {

		label=label.replaceAll("[\n\r]", "");
		label=label.replaceAll("[ ]+", " ");		
		label=label.replace("\\", "\\\\");
		label=label.replace("'", "\\'");
		
		this.label = label;
		String shortlabel = label;
		if (shortlabel != null){
			shortlabel = WebappUtils.ellipsize(shortlabel, Constants.MAX_NODETEXT_LENGTH);
		}
		setShortlabel(shortlabel);		
	}	
	
	/**
	 * Gets the node shortlabel.
	 *
	 * @return the node shortlabel
	 */
	public String getShortlabel() {
		return shortlabel;
	}

	/**
	 * Sets the node shortlabel.
	 *
	 * @param shortname the new node shortlabel
	 */
	public void setShortlabel(String shortlabel) {
		this.shortlabel = shortlabel;
	}	
	
}
