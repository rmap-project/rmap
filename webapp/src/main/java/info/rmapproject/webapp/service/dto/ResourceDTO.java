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
package info.rmapproject.webapp.service.dto;

import info.rmapproject.webapp.domain.Graph;
import info.rmapproject.webapp.domain.ResourceDescription;

import java.net.URI;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * Used to hold the information about a Resource needed to for representation on a webpage.
 * @author khanson
 */
public class ResourceDTO {

	/** The Resource URI. */
	private URI uri;
	
	/** The Resource graph. */
	private Graph graph;
	
	/** A list of DiSCOs the reference the Resource. */
	private List<URI> relatedDiSCOs;
	
	/** The Resource description. */
	private ResourceDescription resourceDescription;

	/**
	 * Gets the Resource URI.
	 *
	 * @return the Resource URI
	 */
	public URI getUri() {
		return uri;
	}
	
	/**
	 * Sets the Resource URI.
	 *
	 * @param uri the new Resource URI
	 */
	public void setUri(URI uri) {
		this.uri = uri;
	}
	
	/**
	 * Gets the Resource description.
	 *
	 * @return the resource description
	 */
	public ResourceDescription getResourceDescription() {
		return resourceDescription;
	}
	
	/**
	 * Sets the Resource description.
	 *
	 * @param resourceDescription the new resource description
	 */
	public void setResourceDescription(ResourceDescription resourceDescription) {
		this.resourceDescription = resourceDescription;
	}

	/**
	 * Gets the Resource graph.
	 *
	 * @return the Resource graph
	 */
	public Graph getGraph() {
		return graph;
	}

	/**
	 * Sets the Resource graph.
	 *
	 * @param graph the new Resource graph
	 */
	public void setGraph(Graph graph) {
		this.graph = graph;
	}

	/**
	 * Gets the list of DiSCOs the reference the Resource
	 *
	 * @return the related DiSCOs
	 */
	public List<URI> getRelatedDiSCOs() {
		return relatedDiSCOs;
	}
	
	/**
	 * Sets the list of DiSCOs the reference the Resource
	 *
	 * @param relatedDiSCOs the new related DiSCOs
	 */
	public void setRelatedDiSCOs(List<URI> relatedDiSCOs) {
		this.relatedDiSCOs = relatedDiSCOs;
	}
		
}
