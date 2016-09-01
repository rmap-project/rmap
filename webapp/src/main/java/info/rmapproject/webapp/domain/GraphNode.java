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
    
    /** Name of node to be used as a label. */
	private String name;
	
	/** Shortened version of the node name for tidy label display. */
	private String shortname; 
	
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
	public GraphNode(Integer id, String name, Integer weight, String type){
		setId(id);
		setType(type);
		setName(name);
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
		String shortname = name;
		if (this.type!=null && this.type.equals(Constants.NODETYPE_TYPE)){
			//for types see if we can do a short name
			shortname=WebappUtils.replaceNamespace(shortname);
		}
		if (shortname.length() > Constants.MAX_NODETEXT_LENGTH) {
			setShortname(shortname.substring(0, Constants.MAX_NODETEXT_LENGTH-3) + "...");
		}
		else {
			setShortname(shortname);			
		}
		
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
	 * Gets the node shortname.
	 *
	 * @return the node shortname
	 */
	public String getShortname() {
		return shortname;
	}

	/**
	 * Sets the node shortname.
	 *
	 * @param shortname the new node shortname
	 */
	public void setShortname(String shortname) {
		this.shortname = shortname;
	}	
	
}
