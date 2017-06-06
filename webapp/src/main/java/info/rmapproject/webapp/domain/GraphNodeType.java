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

import info.rmapproject.webapp.utils.ConfigUtils;
import info.rmapproject.webapp.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import java.util.Locale;
import java.util.Map;

/**
 * Holds a node type. For example name="Physical_Object", displayName="Physical Object", color="#11111"
 * @author khanson
 *
 */
public class GraphNodeType {

	/** The property file name for node types configuration. */
	private static final String NODETYPE_PROPFILE = "nodetypes";
	
	/** The colors list. */
	@Autowired
	@Qualifier("typemappings")
	private MessageSource colors;
		
	/** The node type name. */
	private String name = "";
	
	/** The node type display name. */
	private String displayName = "";
	
	/** The node type color. */
	private String color = "";
	
	/** The node type shape. */
	private String shape = "";

	/** The node type image path. */
	private String image = "";
	
	/**
	 * Instantiates a new graph node type.
	 *
	 * @param name the nodetype name
	 */
	public GraphNodeType(String name, MessageSource colors){
		//just pass in name, the rest is constructed using configuration
		if (name!=null){
			this.name=name;
			this.displayName=name.replace("_", " ");

			String nodeProps = null;
			try {
				nodeProps = colors.getMessage(name, null, Locale.ENGLISH);
			} catch (NoSuchMessageException e) {
				// null nodeProps handled below
			}

			if (nodeProps!=null && nodeProps.contains("|")) {
				String[] props = nodeProps.split("\\|");
				this.color = props[0];
				this.shape = props[1];
				if (this.shape.equals(Constants.NODESHAPE_IMAGE) && props.length==3){
					this.image = props[2];					
				}
			} else { //set defaults
				this.color=Constants.DEFAULT_NODE_COLOR;
				this.shape=Constants.DEFAULT_NODE_SHAPE;
			}		
		}
	}
	
	/**
	 * Gets the node type name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Gets the node type display name.
	 *
	 * @return the display name
	 */
	public String getDisplayName() {
		return displayName;
	}
	
	/**
	 * Gets the node type color.
	 *
	 * @return the color
	 */
	public String getColor() {
		return color;
	}
	
	/**
	 * Gets the node type shape.
	 *
	 * @return the shape
	 */
	public String getShape() {
		return shape;
	}
	
	/**
	 * Gets the node type imagepath.
	 *
	 * @return the image path
	 */
	public String getImage() {
		return image;
	}
	
}
