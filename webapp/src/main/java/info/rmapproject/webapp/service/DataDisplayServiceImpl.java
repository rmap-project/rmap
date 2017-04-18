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
package info.rmapproject.webapp.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import info.rmapproject.core.exception.RMapObjectNotFoundException;
import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapLiteral;
import info.rmapproject.core.model.RMapTriple;
import info.rmapproject.core.model.RMapValue;
import info.rmapproject.core.model.agent.RMapAgent;
import info.rmapproject.core.model.disco.RMapDiSCO;
import info.rmapproject.core.model.event.RMapEvent;
import info.rmapproject.core.model.event.RMapEventCreation;
import info.rmapproject.core.model.event.RMapEventDeletion;
import info.rmapproject.core.model.event.RMapEventDerivation;
import info.rmapproject.core.model.event.RMapEventInactivation;
import info.rmapproject.core.model.event.RMapEventTombstone;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.core.model.event.RMapEventUpdate;
import info.rmapproject.core.model.event.RMapEventUpdateWithReplace;
import info.rmapproject.core.model.request.RMapSearchParams;
import info.rmapproject.core.model.request.RMapStatusFilter;
import info.rmapproject.core.model.request.ResultBatch;
import info.rmapproject.core.rmapservice.RMapService;
import info.rmapproject.core.utils.ConfigUtils;
import info.rmapproject.core.utils.Terms;
import info.rmapproject.webapp.domain.Graph;
import info.rmapproject.webapp.domain.PageStatus;
import info.rmapproject.webapp.domain.ResourceDescription;
import info.rmapproject.webapp.domain.TripleDisplayFormat;
import info.rmapproject.webapp.exception.RMapWebException;
import info.rmapproject.webapp.service.dto.AgentDTO;
import info.rmapproject.webapp.service.dto.DiSCODTO;
import info.rmapproject.webapp.service.dto.EventDTO;
import info.rmapproject.webapp.utils.Constants;
import info.rmapproject.webapp.utils.WebappUtils;

/**
 * Implements Data Display Service
 *
 * @author khanson
 */

@Service("dataDisplayService")
public class DataDisplayServiceImpl implements DataDisplayService {

	//private static final Logger logger = LoggerFactory.getLogger(DiSCOServiceImpl.class);

	/** The RMap Service. */
	private RMapService rmapService;
	
	/** The DiSCO Node Type. */
	private String discoNodeType;
	
	/** The Agent Node Type. */
	private String agentNodeType;
	
	/** Max relationships in an object graph to display*/
	private int maxObjGraphRelationships=60;

	/** Max relationships in a resource graph to display*/
	private int maxResGraphRelationships=50;
	
	/** Max Agent DiSCOs to display on Agent page*/
	private int maxAgentDiSCOs = 50;
	
	/** Max number of table rows to display on one page*/
	private int maxTableRows = 50;

	/** Max number of resource related DiSCOs to display on one page*/
	private int maxResRelatedDiSCOs = 20;
	
	/** Max number of rows to show in node info popup **/
	private int maxNodeInfoRows = 8;
		
	/**
	 * Instantiates a new data display service implementation.
	 *
	 * @param rmapService the RMap Service
	 */
	@Autowired 
	public DataDisplayServiceImpl(RMapService rmapService){
		this.rmapService = rmapService;
		//this is to support customization of node types - support those who would like to categorize DiSCO and Agent 
		//as something other than default.
		try {
			discoNodeType = WebappUtils.getNodeType(new URI(Terms.RMAP_DISCO_PATH));
			agentNodeType = WebappUtils.getNodeType(new URI(Terms.RMAP_AGENT_PATH));
		} catch (Exception e){
			//set default
			discoNodeType = "Undefined";
			agentNodeType = "Undefined";
		}
		
		String maxObjGraphRelationships = ConfigUtils.getPropertyValue(Constants.RMAPWEB_PROPSFILE, Constants.MAX_OBJECT_GRAPH_RELATIONSHIPS_PROPKEY);
		if (maxObjGraphRelationships!=null && maxObjGraphRelationships.length()>0){
			this.maxObjGraphRelationships =  Integer.parseInt(maxObjGraphRelationships);
		}
		
		String maxResGraphRelationships = ConfigUtils.getPropertyValue(Constants.RMAPWEB_PROPSFILE, Constants.MAX_RESOURCE_GRAPH_RELATIONSHIPS_PROPKEY);
		if (maxResGraphRelationships!=null && maxResGraphRelationships.length()>0){
			this.maxResGraphRelationships =  Integer.parseInt(maxResGraphRelationships);
		}

		String maxAgentDiSCOs = ConfigUtils.getPropertyValue(Constants.RMAPWEB_PROPSFILE, Constants.MAX_AGENT_DISCOS_PROPKEY);
		if (maxAgentDiSCOs!=null && maxAgentDiSCOs.length()>0){
			this.maxAgentDiSCOs =  Integer.parseInt(maxAgentDiSCOs);
		}
		
		String maxTableRows = ConfigUtils.getPropertyValue(Constants.RMAPWEB_PROPSFILE, Constants.MAX_TABLE_ROWS_PROPKEY);
		if (maxTableRows!=null && maxTableRows.length()>0){
			this.maxTableRows =  Integer.parseInt(maxTableRows);
		}
		
		String maxResRelatedDiSCOs = ConfigUtils.getPropertyValue(Constants.RMAPWEB_PROPSFILE, Constants.MAX_RESOURCE_RELATED_DISCOS_PROPKEY);
		if (maxResRelatedDiSCOs!=null && maxResRelatedDiSCOs.length()>0){
			this.maxResRelatedDiSCOs =  Integer.parseInt(maxResRelatedDiSCOs);
		}
		
		String maxNodeInfoRows = ConfigUtils.getPropertyValue(Constants.RMAPWEB_PROPSFILE, Constants.MAX_NODE_INFO_ROWS_PROPKEY);
		if (maxNodeInfoRows!=null && maxNodeInfoRows.length()>0){
			this.maxNodeInfoRows =  Integer.parseInt(maxNodeInfoRows);
		}
		
				
	}
		
	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#getDiSCODTO(java.lang.String)
	 */
	@Override
	public DiSCODTO getDiSCODTO(String sDiscoUri) throws Exception {
				
		DiSCODTO discoDTO = new DiSCODTO();
		
		URI discoUri = new URI(sDiscoUri);	
		discoDTO.setUri(discoUri);
		
		RMapDiSCO disco = rmapService.readDiSCO(discoUri);
    	List <URI> aggregatedResources = disco.getAggregatedResources(); 	
    	
		discoDTO.setDescription(disco.getDescription());
		discoDTO.setCreator(disco.getCreator());
		discoDTO.setProvGeneratedBy(disco.getProvGeneratedBy());
		
		discoDTO.setAgentVersions(rmapService.getDiSCOAgentVersions(discoUri));
		discoDTO.setAllVersions(rmapService.getDiSCOAllVersions(discoUri));
		
		discoDTO.setStatus(rmapService.getDiSCOStatus(discoUri));
		discoDTO.setEvents(rmapService.getDiSCOEvents(discoUri));
    	discoDTO.setAggregatedResources(aggregatedResources);
	    discoDTO.setRelatedStatements(disco.getRelatedStatements());
	    
	    rmapService.closeConnection();
		return discoDTO;		
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#createDiSCOGraph(...)
	 */
	public Graph getDiSCOGraph(DiSCODTO discoDTO) throws Exception {
		Graph graph = new Graph();
		String sDiscoUri = discoDTO.getUri().toString();
						
		if (discoDTO.getCreator().length()>0) {
			graph.addEdge(sDiscoUri, discoDTO.getCreator(), DCTERMS.CREATOR.toString(), discoNodeType, agentNodeType);
		}

		RMapSearchParams params = new RMapSearchParams();
		params.setStatusCode(RMapStatusFilter.ACTIVE);
		
		for (URI aggregate : discoDTO.getAggregatedResources()) {
			List<URI> rdfTypes = rmapService.getResourceRdfTypesInDiSCO(aggregate, discoDTO.getUri());
			String targetNodeType = WebappUtils.getNodeType(rdfTypes);
			graph.addEdge(sDiscoUri, aggregate.toString(),Terms.ORE_AGGREGATES_PATH, discoNodeType, targetNodeType);
		}

		List<RMapTriple> filteredTriples = new ArrayList<RMapTriple>();
		
		List<RMapTriple> triples = discoDTO.getRelatedStatements();
		//remove literals and types from disco graph
		for (RMapTriple triple:triples){
			RMapIri pred = triple.getPredicate();
			RMapValue obj = triple.getObject();
			
			if (!pred.toString().equals(RDF.TYPE.toString())
					&& !(obj instanceof RMapLiteral)){
				filteredTriples.add(triple);
			}
		}
		
		triples = null;

		if ((filteredTriples.size()+graph.getEdges().size())<=maxObjGraphRelationships){
			graph = addTriplesToGraph(graph, filteredTriples, discoDTO.getUri());
		} else {
			//don't do graph because it's too large and will just be an unbearable mess!!
			graph = null;
		}
		rmapService.closeConnection();
		return graph;
	}	

	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#getDiSCOTableData(java.lang.String, java.lang.Integer)
	 */
	public List<ResourceDescription> getDiSCOTableData(DiSCODTO discoDTO, Integer offset) throws Exception {

		List<URI> aggregatedResources = discoDTO.getAggregatedResources(); 
		List<RMapTriple> triples = discoDTO.getRelatedStatements(); 
		
	    List<ResourceDescription> resourceDescriptions = new ArrayList<ResourceDescription>();
	    
	    //first put other resources into an ordered sets (TreeSet)
	    Set<String> nonAggregatedResourcesDescribed = new TreeSet<String>();
	    for (RMapTriple stmt:triples) {
	    	nonAggregatedResourcesDescribed.add(stmt.getSubject().toString());
	    }
	    
	    Set<String> aggregatedResourcesDescribed = new TreeSet<String>();
	    for (URI aggregate : aggregatedResources) {
	    	aggregatedResourcesDescribed.add(aggregate.toString());
	    }
	    
	    //Now put into a list that will maintain ordering (LinkedHashSet). 
	    //we want the aggregates list first then other resources
	    Set<String> resourcesDescribed = new LinkedHashSet<String>();
	    resourcesDescribed.addAll(aggregatedResourcesDescribed);
	    resourcesDescribed.addAll(nonAggregatedResourcesDescribed);
	    
	    int position = 0;
	    int count = 0;
	    int remainingRows = maxTableRows;
	    
	    //now get resource description for each resource, truncate according to offset and max rows
	    for (String resource : resourcesDescribed) {	    	
	    	ResourceDescription resDescrip = getObjectResourceDescription(resource, triples);  	
	    	Map<String, TripleDisplayFormat> tripleMap = resDescrip.getPropertyValues(); 
	    	//keep getting new resource descriptions til we hit the offset
	    	if (position < offset) {
	    		if (tripleMap.size() <= offset){
	    			position = position + tripleMap.size();
	    			tripleMap.clear();
	    		} else {
	    			position = offset;
	    			tripleMap.keySet().removeAll(Arrays.asList(tripleMap.keySet().toArray()).subList(0, offset-1));
	    		}
	    	} 
	    	//once we're at offset, keep adding resdes until we reach max
	    	if (position >= offset) {
		    	if (tripleMap.size()>remainingRows) {
		    		Map<String, TripleDisplayFormat> truncatedMap = new HashMap<String,TripleDisplayFormat>();
		    		for (Map.Entry<String, TripleDisplayFormat> entry:tripleMap.entrySet()) {
		    		     if (count >= remainingRows) break;
		    		     truncatedMap.put(entry.getKey(), entry.getValue());
		    		     count++;
		    		  }
		    		resDescrip.setPropertyValues(truncatedMap);
		    		remainingRows=0;
		    	} else {
		    		remainingRows= remainingRows - tripleMap.size();
		    	}
	    	resourceDescriptions.add(resDescrip);	
	    	}    
	    	if (remainingRows==0){break;}
	    }

	    return resourceDescriptions;
	}
		
	public PageStatus getDiSCOPageStatus(List<RMapTriple> triples, Integer offset) {
	    PageStatus pageStatus = new PageStatus();
	    List<RMapTriple> triplesNoTypes = new ArrayList<RMapTriple>();
	    for (RMapTriple triple : triples){
	    	if (!triple.getPredicate().toString().equals(RDF.TYPE.toString())) {
	    		triplesNoTypes.add(triple);
	    	}
	    }
	    
	    if (offset==null){offset=0;}
	    boolean hasNext = triplesNoTypes.size()>(offset + maxTableRows);
	    boolean hasPrevious = offset>0;
	    int endposition = triplesNoTypes.size();
		if ((offset+maxTableRows)<triplesNoTypes.size()) {
			endposition = offset+maxTableRows;
		}
		pageStatus.setHasNext(hasNext);
		pageStatus.setHasPrevious(hasPrevious);
		pageStatus.setStartPosition(offset+1);
		pageStatus.setEndPosition(endposition);  
		pageStatus.setSize(triplesNoTypes.size());
		pageStatus.setLimit(maxTableRows);
		return pageStatus;
	}
	
	/**
	 * Generate resource description for resource based on triples provided. Assumes all triples have been returned, unlike for
	 * plain resource description where it may be truncate Truncation happens here instead. Will filter out triples that
	 * do not have a subject URI that matches the resource in question.
	 * @param resource
	 * @param triples 
	 * @return resource description
	 */
	private ResourceDescription getObjectResourceDescription(String resource, List<RMapTriple> triples) throws RMapWebException{
    	//start new resource description
    	ResourceDescription rd = new ResourceDescription(resource);	  
    	
    	try {
    		List<URI> resourceTypes = getResourceRDFTypes(new URI(resource));
    		rd.addResourceTypes(resourceTypes);
    		    		
    		for (RMapTriple triple : triples) { 
	    		boolean subjMatchesResource = triple.getSubject().toString().equals(resource.toString());
	    		boolean predIsRdfType = triple.getPredicate().toString().equals(RDF.TYPE.toString());
	    		
	    		if (subjMatchesResource && !predIsRdfType) {
		    		//only include this if subj matches resource and isn't a type, types are displayed separately
			    	TripleDisplayFormat tripleDF = new TripleDisplayFormat(triple);
		    		rd.addPropertyValue(tripleDF);		
		    	}
	    	}
    	} catch (RMapWebException ex){
    		throw ex;
    	} catch (Exception ex){
    		RMapWebException.wrap(ex);
    	}
    	
    	return rd;
	}
	
	
	
	
	
	/*************************************************************
	 * RESOURCE PATH METHODS
	 */
	

	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#getResourceBatch(java.lang.String, java.lang.Integer, java.lang.String)
	 */
	@Override
	public ResultBatch<RMapTriple> getResourceBatch(String resourceUri, Integer offset, String view) throws Exception {
		URI uri = new URI(resourceUri);

		RMapSearchParams params = new RMapSearchParams();
		params.setStatusCode(RMapStatusFilter.ACTIVE);
		params.setExcludeTypes(true);	
		params.setOffset(offset);
		if (view.equals("graph")){
			params.setExcludeLiterals(true);	
			params.setLimit(maxResGraphRelationships);
		} else {
			params.setLimit(maxTableRows);
		}
		
		ResultBatch<RMapTriple> triplebatch = rmapService.getResourceRelatedTriples(uri, params);
    	
		//if there are no triples, don't load an empty screen, show a not found error.
		//note that because only connected graphs are allowed, every URI should have at
		//least one uri link.
    	if (triplebatch.size()==0)	{
    		throw new RMapObjectNotFoundException();
    	}
    	return triplebatch;
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#getResourceGraph(info.rmapproject.core.model.request.ResultBatch<RMapTriple>)
	 */
	@Override
	public Graph getResourceGraph(ResultBatch<RMapTriple> triplebatch) throws Exception {
		Graph graph = new Graph();
		graph = addTriplesToGraph(graph, triplebatch.getResultList());  	
		rmapService.closeConnection();
		return graph;
	    
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#getResourceTableData(java.lang.String, info.rmapproject.core.model.request.ResultBatch<RMapTriple>)
	 */
	@Override
	public ResourceDescription getResourceTableData(String resourceUri, ResultBatch<RMapTriple> triplebatch) throws Exception {
		ResourceDescription rd = getResourceDescription(resourceUri, triplebatch, true);
	    rmapService.closeConnection();
		return rd;
	}			
	
	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#getResourceRelatedDiSCOs(java.lang.String, java.lang.Integer)
	 */
	@Override
	public ResultBatch<URI> getResourceRelatedDiSCOs(String resourceUri, Integer offset) throws Exception {
		RMapSearchParams params = new RMapSearchParams();
		params.setLimit(maxResRelatedDiSCOs);
		params.setOffset(offset);
		params.setStatusCode(RMapStatusFilter.ACTIVE);
		ResultBatch<URI> relatedDiSCOs = rmapService.getResourceRelatedDiSCOs(new URI(resourceUri), params);
		return relatedDiSCOs;
	}
	
	
	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#getPageStatus(info.rmapproject.core.model.request.ResultBatch<?>)
	 */
	@Override
	public PageStatus getPageStatus(ResultBatch<?> results, String pageType) {
		PageStatus pageStatus = new PageStatus();
		pageStatus.setHasNext(results.hasNext());
		pageStatus.setHasPrevious(results.hasPrevious());
		pageStatus.setStartPosition(results.getStartPosition());
		pageStatus.setEndPosition(results.getEndPosition());  
		pageStatus.setSize(results.getResultList().size());
		if (pageType.equals("resource_graph")) {
			pageStatus.setLimit(this.maxResGraphRelationships);
		} else if (pageType.equals("resource_table")){
			pageStatus.setLimit(this.maxTableRows);
		} else if (pageType.equals("resource_discos")){
			pageStatus.setLimit(this.maxResRelatedDiSCOs);
		} else if (pageType.equals("agent_discos")) {
			pageStatus.setLimit(this.maxAgentDiSCOs);
		} else if (pageType.equals("node_info")) {
			pageStatus.setLimit(this.maxNodeInfoRows);
		} else {
			pageStatus.setLimit(this.maxResGraphRelationships);
		}
		
		
		return pageStatus;
	}


	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#getAgentDTO(java.lang.String)
	 */
	@Override
	public AgentDTO getAgentDTO(String sAgentUri) throws Exception {
				
		AgentDTO agentDTO = new AgentDTO();
		
		URI agentUri = new URI(sAgentUri);	
		agentDTO.setUri(agentUri);
		
		RMapAgent agent = rmapService.readAgent(agentUri);
		agentDTO.setName(agent.getName());		
		agentDTO.setStatus(rmapService.getAgentStatus(agentUri));
		agentDTO.setEvents(rmapService.getAgentEvents(agentUri));
		agentDTO.setIdProvider(agent.getIdProvider().getStringValue());
		agentDTO.setAuthId(agent.getAuthId().getStringValue());
			    	  	    	    
	    rmapService.closeConnection();
		
		return agentDTO;
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#getAgentDTO(info.rmapproject.webapp.service.dto.AgentDTOg)
	 */
	@Override
	public Graph getAgentGraph(AgentDTO agentDTO) throws Exception{
		Graph graph = new Graph();
		String sAgentUri = agentDTO.getUri().toString();
		
		graph.addEdge(sAgentUri, agentDTO.getIdProvider(),Terms.RMAP_IDENTITYPROVIDER_PATH, agentNodeType, agentNodeType);
		graph.addEdge(sAgentUri, agentDTO.getAuthId(),Terms.RMAP_USERAUTHID_PATH, agentNodeType, WebappUtils.getNodeType(new URI(Terms.RMAP_USERAUTHID_PATH)));

		return graph;			
	}

	public ResourceDescription getAgentTableData(AgentDTO agentDTO) throws Exception {
		ResourceDescription rd = new ResourceDescription();
		
		return rd;
	}
	
	
	public ResultBatch<URI> getAgentDiSCOs(String agentUri, Integer offset) throws Exception {
		RMapSearchParams params = new RMapSearchParams();
		params.setStatusCode(RMapStatusFilter.ACTIVE);	   
		params.setOffset(offset);
		params.setLimit(maxAgentDiSCOs);
	    rmapService.closeConnection();
		ResultBatch<URI> agentDiSCOs = rmapService.getAgentDiSCOs(new URI(agentUri), params);
		return agentDiSCOs;
	}
	

	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#getEventDTO(java.lang.String)
	 */
	public EventDTO getEventDTO(String sEventUri) throws Exception {
			
		EventDTO eventDTO = new EventDTO();

		URI eventUri = new URI(sEventUri);
		eventDTO.setUri(eventUri);
		
		RMapEvent event = rmapService.readEvent(eventUri);
		RMapValue description = event.getDescription();
		if (description!=null) {
			eventDTO.setDescription(description);
		}	
		eventDTO.setAssociatedAgent(event.getAssociatedAgent());
		eventDTO.setAssociatedKey(event.getAssociatedKey());
		eventDTO.setTargetType(event.getEventTargetType());
		eventDTO.setStartTime(event.getStartTime());
		eventDTO.setEndTime(event.getEndTime());

		RMapEventType eventType = event.getEventType();
		eventDTO.setType(eventType);
		
	    Map<String, String> resourcesAffected = getEventResourcesAffected(event, eventType);
	    eventDTO.setResourcesAffected(resourcesAffected);  
	    rmapService.closeConnection();
	
	    return eventDTO;
	}
	
	
	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#getResourceLiterals(java.lang.String)
	 */
	public ResultBatch<RMapTriple> getResourceLiterals(String resourceUri, Integer offset) throws Exception{

		URI uri = new URI(resourceUri);
		
		RMapSearchParams params = new RMapSearchParams();
		params.setStatusCode(RMapStatusFilter.ACTIVE);
		params.setLimit(ConfigUtils.getPropertyValue(info.rmapproject.core.utils.Constants.RMAPCORE_PROPFILE, 
				info.rmapproject.core.utils.Constants.MAX_QUERY_LIMIT_KEY));
		params.setExcludeIRIs(true);
		params.setExcludeTypes(true);
		params.setOffset(offset);
		params.setLimit(maxNodeInfoRows);
		
		ResultBatch<RMapTriple> triplebatch = rmapService.getResourceRelatedTriples(uri, params);
				
		rmapService.closeConnection();
		return triplebatch;
	}
	
	
	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#getResourceLiteralsInContext(java.lang.String,java.lang.String)
	 */
	public ResultBatch<RMapTriple> getResourceLiteralsInContext(String sResourceUri, String sGraphUri, Integer offset) throws Exception{

		URI resourceUri = new URI(sResourceUri);
		URI graphUri = new URI(sGraphUri);
		
		RMapSearchParams params = new RMapSearchParams();
		params.setLimit(ConfigUtils.getPropertyValue(info.rmapproject.core.utils.Constants.RMAPCORE_PROPFILE, 
									info.rmapproject.core.utils.Constants.MAX_QUERY_LIMIT_KEY));
		params.setStatusCode(RMapStatusFilter.ACTIVE);
		params.setExcludeIRIs(true);
		params.setOffset(offset);
		params.setLimit(maxNodeInfoRows);
		
		ResultBatch<RMapTriple> resultbatch = rmapService.getResourceRelatedTriples(resourceUri, graphUri, params);
		rmapService.closeConnection();
		return resultbatch;
	}
	
	

	
	
	/**
	 * Generate resource description for resource based on triples provided. Will filter out triples that
	 * do not have a subject URI that matches the resource in question.
	 * @param resource
	 * @param triples 
	 * @param includeBothDirections - true when matching resource based on subject or object. False for subject matching only.
	 * @return resource description
	 */
	private ResourceDescription getResourceDescription(String resource, ResultBatch<RMapTriple> triplebatch, boolean includeBothDirections) throws RMapWebException{

    	//start new resource description
    	ResourceDescription rd = new ResourceDescription(resource);	  
    	
    	try {

    		List<URI> resourceTypes = getResourceRDFTypes(new URI(resource));
    		rd.addResourceTypes(resourceTypes);
    		
    		List<RMapTriple> triples = triplebatch.getResultList();    		
    		
    		for (RMapTriple triple : triples) { 
	    		boolean objectIsLiteral = (triple.getObject() instanceof RMapLiteral);
	    		boolean subjMatchesResource = triple.getSubject().toString().equals(resource.toString());
	    		boolean objMatchesResource = !objectIsLiteral && triple.getObject().toString().equals(resource.toString());
	    		
	    		if (subjMatchesResource || (objMatchesResource && includeBothDirections)) {
		    		//only include this if subj or object matches resource
			    	TripleDisplayFormat tripleDF = new TripleDisplayFormat(triple);
		    		rd.addPropertyValue(tripleDF);		
		    	}
	    	}
    	} catch (RMapWebException ex){
    		throw ex;
    	} catch (Exception ex){
    		RMapWebException.wrap(ex);
    	}
    	
    	return rd;
	}
	
		

			
		/**
		 * Adds triples to the graph.
		 *
		 * @param graph the graph
		 * @param triples the triples
		 * @return the updated graph
		 * @throws Exception the exception
		 */
		private Graph addTriplesToGraph(Graph graph, List<RMapTriple> triples) throws Exception{
			return addTriplesToGraph(graph, triples, null);
		}
					
		/**
		 * Adds the triples to the graph.
		 *
		 * @param graph the graph
		 * @param triples the triples
		 * @param contextUri the context URI
		 * @return the graph
		 * @throws Exception the exception
		 */
		private Graph addTriplesToGraph(Graph graph, List<RMapTriple> triples, URI contextUri) throws Exception{
			URI lastSubject = null;
			String lastSourceNodeType = null;
			for (RMapTriple triple : triples) {
				URI subject = new URI(triple.getSubject().toString());
				URI predicate = new URI(triple.getPredicate().toString());
				RMapValue object = triple.getObject();

				String sourceNodeType = null;
				String targetNodeType = null;	
				
				//don't call database again if the subject has not changed
				if (lastSubject!=null && lastSubject.equals(subject)){
					sourceNodeType = lastSourceNodeType;
				}
				
				if (sourceNodeType==null){
					List <URI> sourceRdfTypes = getResourceRDFTypes(subject, contextUri);
					sourceNodeType = WebappUtils.getNodeType(sourceRdfTypes);
				}
				
				if (object instanceof RMapIri && targetNodeType==null){
					List <URI> targetRdfTypes = getResourceRDFTypes(new URI(object.toString()), contextUri);		
					targetNodeType = WebappUtils.getNodeType(targetRdfTypes);			
				}						
				
				graph.addEdge(subject.toString(), object.toString(), predicate.toString(), sourceNodeType, targetNodeType);
				
				lastSubject = subject;
				lastSourceNodeType = sourceNodeType;
			}
			return graph;
		}
		
		
		/**
		 * Gets the resource RDF types.
		 *
		 * @param resourceUri the Resource URI
		 * @param contextUri the context URI
		 * @return the resource RDF types
		 * @throws Exception the exception
		 */
		private List<URI> getResourceRDFTypes(URI resourceUri, URI contextUri) throws Exception{
			if (contextUri==null){
				return getResourceRDFTypes(resourceUri);
			}
			List<URI> rdfTypes = rmapService.getResourceRdfTypesInDiSCO(resourceUri, contextUri);
			rmapService.closeConnection();
			return rdfTypes;
		}
		
		
		/**
		 * Gets the resource RDF types.
		 *
		 * @param resource the Resource
		 * @return the Resource RDF types
		 * @throws Exception the exception
		 */
		public List<URI> getResourceRDFTypes(URI resource) throws Exception{
			List<URI> rdfTypes = new ArrayList<URI>();
			
			RMapSearchParams params=new RMapSearchParams();
			params.setStatusCode(RMapStatusFilter.ACTIVE);
	
			Map <URI, Set<URI>> types = rmapService.getResourceRdfTypesAllContexts(resource, params);
			
			if (types!=null){
				for (Map.Entry<URI, Set<URI>> type : types.entrySet()){
					Set<URI> contexttypes = type.getValue();
					for (URI contexttype : contexttypes) {
						if (contexttype!=null&!rdfTypes.contains(contexttype)) {
							rdfTypes.add(contexttype);
						}
					}
				}
			}		
			rmapService.closeConnection();
			return rdfTypes;
		}
		
		
	
		/**
		 * Gets the Event Resources affected.
		 *
		 * @param event the event
		 * @param eventType the event type
		 * @return the Event Resources affected
		 * @throws Exception the exception
		 */
		private Map<String, String> getEventResourcesAffected(RMapEvent event, RMapEventType eventType) throws Exception {
	
		    Map<String, String> resourcesAffected = new HashMap<String, String>();
		
		    if (eventType == RMapEventType.CREATION){
		    	RMapEventCreation creationEvent = (RMapEventCreation) event;
		    	List<RMapIri> uris = creationEvent.getCreatedObjectIds();
		    	for (RMapIri uri : uris){
		    		rmapService.isDiSCOId(new java.net.URI(uri.toString()));
		    		String type = getRMapTypeDisplayName(uri);
		    		resourcesAffected.put(uri.toString(), "Created " + type);
		    	}
		    }
		    else if (eventType == RMapEventType.DELETION)	{
		    	RMapEventDeletion deletionEvent = (RMapEventDeletion) event;
		    	List<RMapIri> uris = deletionEvent.getDeletedObjectIds();
		    	for (RMapIri uri : uris){
		    		String type = getRMapTypeDisplayName(uri);
		    		resourcesAffected.put(uri.toString(), "Deleted " + type);
		    	}
		    }
		    else if (eventType == RMapEventType.TOMBSTONE)	{
		    	RMapEventTombstone tombstoneEvent = (RMapEventTombstone) event;
		    	RMapIri uri = tombstoneEvent.getTombstonedResourceId();
				String type = getRMapTypeDisplayName(uri);
				resourcesAffected.put(uri.toString(), "Tombstoned " + type);
		    }
		    else if (eventType == RMapEventType.DERIVATION)	{
		    	RMapEventDerivation derivationEvent = (RMapEventDerivation) event;
		    	List<RMapIri> createdUris = derivationEvent.getCreatedObjectIds();
		    	for (RMapIri uri : createdUris){
		    		String type = getRMapTypeDisplayName(uri);
		    		resourcesAffected.put(uri.toString(), "Created " + type);
		    	}
		    	RMapIri derivedUri = derivationEvent.getDerivedObjectId();
				String type = getRMapTypeDisplayName(derivedUri);
				resourcesAffected.put(derivedUri.toString(), "Derived " + type);	
				
		    	RMapIri sourceObjectUri = derivationEvent.getSourceObjectId();
		    	type = getRMapTypeDisplayName(sourceObjectUri);
				resourcesAffected.put(sourceObjectUri.toString(), "Source " + type);	    	
		    }
		    else if (eventType == RMapEventType.UPDATE)	{
		    	RMapEventUpdate updateEvent = (RMapEventUpdate) event;	   
		    	List<RMapIri> createdUris = updateEvent.getCreatedObjectIds();
		    	for (RMapIri uri : createdUris){
		    		String type = getRMapTypeDisplayName(uri);
		    		resourcesAffected.put(uri.toString(), "Created " + type);
		    	}
		    	RMapIri derivedUri = updateEvent.getDerivedObjectId();
				String type = getRMapTypeDisplayName(derivedUri);
				resourcesAffected.put(derivedUri.toString(), "Derived " + type);	
				
		    	RMapIri inactivatedUri = updateEvent.getInactivatedObjectId();
		    	type = getRMapTypeDisplayName(inactivatedUri);
				resourcesAffected.put(inactivatedUri.toString(), "Inactivated " + type);	  	    			    	
		    }
		    else if (eventType == RMapEventType.INACTIVATION)	{
		    	RMapEventInactivation inactivateEvent = (RMapEventInactivation) event;	    
		    	RMapIri uri = inactivateEvent.getInactivatedObjectId();
				String type = getRMapTypeDisplayName(uri);
				resourcesAffected.put(uri.toString(), "Inactivated " + type);	  
		    }
		    else if (eventType == RMapEventType.REPLACE)	{
		    	RMapEventUpdateWithReplace replaceEvent = (RMapEventUpdateWithReplace) event;	    
		    	RMapIri uri = replaceEvent.getUpdatedObjectId();
				String type = getRMapTypeDisplayName(uri);
				resourcesAffected.put(uri.toString(), "Replaced " + type);	  
		    }
		    
		    return resourcesAffected;
		}
	
		/**
		 * Gets the RMap Type display name.
		 *
		 * @param resourceUri the Resource URI
		 * @return the r map type display name
		 * @throws Exception the exception
		 */
		private String getRMapTypeDisplayName(RMapIri resourceUri) throws Exception {
			URI uriResourceUri = new URI(resourceUri.toString());
			return getRMapTypeDisplayName(uriResourceUri);
		}
		
		/* (non-Javadoc)
		 * @see info.rmapproject.webapp.service.DataDisplayService#getRMapTypeDisplayName(java.net.URI)
		 */
		@Override
		public String getRMapTypeDisplayName(URI resourceUri) throws Exception {
	
			if (rmapService.isDiSCOId(resourceUri)) {
				return Terms.RMAP_DISCO;			
			}
			if (rmapService.isAgentId(resourceUri)) {
				return Terms.RMAP_AGENT;			
			}
			if (rmapService.isEventId(resourceUri)) {
				return Terms.RMAP_EVENT;			
			}
			//otherwise
			return "";
		}

		
}
