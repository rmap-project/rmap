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

import info.rmapproject.core.model.request.RMapSearchParamsFactory;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;

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
import info.rmapproject.core.utils.Terms;
import info.rmapproject.webapp.domain.Graph;
import info.rmapproject.webapp.domain.PageStatus;
import info.rmapproject.webapp.domain.PaginatorType;
import info.rmapproject.webapp.domain.ResourceDescription;
import info.rmapproject.webapp.domain.TripleDisplayFormat;
import info.rmapproject.webapp.exception.RMapWebException;
import info.rmapproject.webapp.service.dto.AgentDTO;
import info.rmapproject.webapp.service.dto.DiSCODTO;
import info.rmapproject.webapp.service.dto.EventDTO;
import info.rmapproject.webapp.utils.WebappUtils;

/**
 * Implements Data Display Service
 *
 * @author khanson
 */
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

	@Autowired
	private RMapSearchParamsFactory paramsFactory;

	@Autowired
	private GraphFactory graphFactory;

	@Autowired
	private TripleDisplayFormatFactory tripleDisplayFactory;

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
	 * @see info.rmapproject.webapp.service.DataDisplayService#getDiSCOGraph(info.rmapproject.webapp.service.dto.DiSCODTO)
	 */
	@Override
	public Graph getDiSCOGraph(DiSCODTO discoDTO) throws Exception {
		Graph graph = graphFactory.newGraph();
		String sDiscoUri = discoDTO.getUri().toString();
						
		if (discoDTO.getCreator().length()>0) {
			graph.addEdge(sDiscoUri, discoDTO.getCreator(), DCTERMS.CREATOR.toString(), discoNodeType, agentNodeType);
		}

		RMapSearchParams params = paramsFactory.newInstance();
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
	 * @see info.rmapproject.webapp.service.DataDisplayService#getDiSCOTableData(info.rmapproject.webapp.service.dto.DiSCODTO, java.lang.Integer)
	 */
	@Override
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
	
	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#getDiSCOPageStatus(java.util.List<RMapTriple>, java.lang.Integer)
	 */
	@Override
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
	 * plain resource description where it may be truncated. Will filter out triples that do not have a subject URI that matches 
	 * the resource in question.
	 * @param resource the resource URI
	 * @param triples list of RMapTriples
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
					TripleDisplayFormat tripleDF = tripleDisplayFactory.newTripleDisplayFormat(triple);
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
	 * @see info.rmapproject.webapp.service.DataDisplayService#getResourceBatch(java.lang.String, java.lang.Integer, info.rmapproject.webapp.domain.PaginatorType)
	 */
	@Override
	public ResultBatch<RMapTriple> getResourceBatch(String resourceUri, Integer offset, PaginatorType view) throws Exception {
		URI uri = new URI(resourceUri);

		RMapSearchParams params = paramsFactory.newInstance();
		params.setStatusCode(RMapStatusFilter.ACTIVE);
		params.setExcludeTypes(true);	
		params.setOffset(offset);
		if (view.equals(PaginatorType.RESOURCE_GRAPH)){
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
		Graph graph = graphFactory.newGraph();
		graph = addTriplesToGraph(graph, triplebatch.getResultList());  	
		rmapService.closeConnection();
		return graph;
	    
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#getResourceTableData(java.lang.String, info.rmapproject.core.model.request.ResultBatch<RMapTriple>)
	 */
	@Override
	public ResourceDescription getResourceTableData(String resourceUri, ResultBatch<RMapTriple> triplebatch) throws Exception {
		ResourceDescription rd = getResourceDescription(resourceUri, triplebatch, null, true);
	    rmapService.closeConnection();
		return rd;
	}			

	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#getResourceTableData(java.lang.String, info.rmapproject.core.model.request.ResultBatch<RMapTriple>, java.lang.String)
	 */
	@Override
	public ResourceDescription getResourceTableData(String resourceUri, ResultBatch<RMapTriple> triplebatch, String contextUri) throws Exception {
		ResourceDescription rd = getResourceDescription(resourceUri, triplebatch, contextUri, true);
	    rmapService.closeConnection();
		return rd;
	}		
	
	
	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#getResourceRelatedDiSCOs(java.lang.String, java.lang.Integer)
	 */
	@Override
	public ResultBatch<URI> getResourceRelatedDiSCOs(String resourceUri, Integer offset) throws Exception {
		RMapSearchParams params = paramsFactory.newInstance();
		params.setLimit(maxResRelatedDiSCOs);
		params.setOffset(offset);
		params.setStatusCode(RMapStatusFilter.ACTIVE);
		ResultBatch<URI> relatedDiSCOs = rmapService.getResourceRelatedDiSCOs(new URI(resourceUri), params);
		return relatedDiSCOs;
	}
	
	
	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#getPageStatus(info.rmapproject.core.model.request.ResultBatch<?>, info.rmapproject.webapp.domain.PaginatorType)
	 */
	@Override
	public PageStatus getPageStatus(ResultBatch<?> results, PaginatorType pageType) {
		PageStatus pageStatus = new PageStatus();
		pageStatus.setHasNext(results.hasNext());
		pageStatus.setHasPrevious(results.hasPrevious());
		pageStatus.setStartPosition(results.getStartPosition());
		pageStatus.setEndPosition(results.getEndPosition());  
		pageStatus.setSize(results.getResultList().size());
		
		switch(pageType) {
			case RESOURCE_GRAPH: 
				pageStatus.setLimit(this.maxResGraphRelationships);
				break;
			case RESOURCE_TABLE: 
				pageStatus.setLimit(this.maxTableRows);
				break;
			case RESOURCE_DISCOS: 
				pageStatus.setLimit(this.maxResRelatedDiSCOs);
				break;
			case AGENT_DISCOS: 
				pageStatus.setLimit(this.maxAgentDiSCOs);
				break;
			case NODE_INFO: 
				pageStatus.setLimit(this.maxNodeInfoRows);
				break;				
			default:
				pageStatus.setLimit(this.maxResGraphRelationships);
				break;			
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
	 * @see info.rmapproject.webapp.service.DataDisplayService#getAgentDTO(info.rmapproject.webapp.service.dto.AgentDTO)
	 */
	@Override
	public Graph getAgentGraph(AgentDTO agentDTO) throws Exception{
		Graph graph = graphFactory.newGraph();
		String sAgentUri = agentDTO.getUri().toString();
		
		graph.addEdge(sAgentUri, agentDTO.getIdProvider(),Terms.RMAP_IDENTITYPROVIDER_PATH, agentNodeType, agentNodeType);
		graph.addEdge(sAgentUri, agentDTO.getAuthId(),Terms.RMAP_USERAUTHID_PATH, agentNodeType, WebappUtils.getNodeType(new URI(Terms.RMAP_USERAUTHID_PATH)));

		return graph;			
	}	
	
	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#getAgentDTO(java.lang.String, java.lang.Integer)
	 */
	@Override
	public ResultBatch<URI> getAgentDiSCOs(String agentUri, Integer offset) throws Exception {
		RMapSearchParams params = paramsFactory.newInstance();
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
	@Override
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
	 * @see info.rmapproject.webapp.service.DataDisplayService#getResourceLiterals(java.lang.String, java.lang.Integer)
	 */
	@Override
	public ResultBatch<RMapTriple> getResourceLiterals(String resourceUri, Integer offset) throws Exception{

		URI uri = new URI(resourceUri);
		
		RMapSearchParams params = paramsFactory.newInstance();
		params.setStatusCode(RMapStatusFilter.ACTIVE);
		params.setExcludeIRIs(true);
		params.setExcludeTypes(true);
		params.setOffset(offset);
		params.setLimit(maxNodeInfoRows);
		
		ResultBatch<RMapTriple> triplebatch = rmapService.getResourceRelatedTriples(uri, params);
				
		rmapService.closeConnection();
		return triplebatch;
	}
	
	
	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#getResourceLiteralsInContext(java.lang.String, java.lang.String, java.lang.Integer)
	 */
	@Override
	public ResultBatch<RMapTriple> getResourceLiteralsInContext(String sResourceUri, String sGraphUri, Integer offset) throws Exception{

		URI resourceUri = new URI(sResourceUri);
		URI graphUri = new URI(sGraphUri);
		
		RMapSearchParams params = paramsFactory.newInstance();
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
	 * @param resourceUri The resource URI to retrieve a description for
	 * @param triples ResultBatch of RMapTriples to generate resource description from
	 * @param contextUri for when types should be limited to the context of a specific RMap Object. 
	 * @param includeBothDirections true when matching resource based on subject or object. False for subject matching only.
	 * @return the ResourceDescription
	 */
	private ResourceDescription getResourceDescription(String resourceUri, ResultBatch<RMapTriple> triplebatch, String contextUri, boolean includeBothDirections) throws RMapWebException{

    	//start new resource description
    	ResourceDescription rd = new ResourceDescription(resourceUri);	  
    	
    	try {
    		List<URI> resourceTypes;
    		if (contextUri!=null){
    			resourceTypes = getResourceRDFTypes(new URI(resourceUri), new URI(contextUri));
    		} else {
    			resourceTypes = getResourceRDFTypes(new URI(resourceUri));
    		}
    		rd.addResourceTypes(resourceTypes);
    		
    		List<RMapTriple> triples = triplebatch.getResultList();    		
    		
    		for (RMapTriple triple : triples) { 
	    		boolean objectIsLiteral = (triple.getObject() instanceof RMapLiteral);
	    		boolean subjMatchesResource = triple.getSubject().toString().equals(resourceUri.toString());
	    		boolean objMatchesResource = !objectIsLiteral && triple.getObject().toString().equals(resourceUri.toString());
	    		
	    		if (subjMatchesResource || (objMatchesResource && includeBothDirections)) {
		    		//only include this if subj or object matches resource
			    	TripleDisplayFormat tripleDF = tripleDisplayFactory.newTripleDisplayFormat(triple);
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
	 * Adds the triples to a graph. Pass in existing object, list of triples to be added and the RMap object URI
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
	 * Gets RDF types for a resource, optionally limited to a context.
	 *
	 * @param resourceUri the Resource URI
	 * @param contextUri the context URI (null if no context restriction)
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
		
	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#getResourceLiterals(java.net.URI)
	 */
	@Override
	public List<URI> getResourceRDFTypes(URI resourceUri) throws Exception{
		List<URI> rdfTypes = new ArrayList<URI>();
		
		RMapSearchParams params = paramsFactory.newInstance();
		params.setStatusCode(RMapStatusFilter.ACTIVE);

		Map <URI, Set<URI>> types = rmapService.getResourceRdfTypesAllContexts(resourceUri, params);
		
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
	 * @param event the RMap Event object
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
	 * @return the RMap Type display name
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

	/**
	 * This set of properties determines how many rows are displayed in different parts of the page Maximum number of
	 * relationships to be shown in an Agent or DiSCO graph. If the object contains more than this limit, the graph will
	 * be replaced with a notice saying the object graph is too large to be visualized.  May be configured using the
	 * {@code rmapweb.max-object-graph-relationships} property.
	 *
	 * @return
	 */
	public int getMaxObjGraphRelationships() {
		return maxObjGraphRelationships;
	}

	public void setMaxObjGraphRelationships(int maxObjGraphRelationships) {
		this.maxObjGraphRelationships = maxObjGraphRelationships;
	}

	/**
	 * Maximum number of relationships shown in resource graph. Because everything comes out from the center of this
	 * graph, a lower number of relationships than the object graph is best.  May be configured by setting the
	 * {@code rmapweb.max-resource-graph-relationships} property.
	 *
	 * @return
	 */
	public int getMaxResGraphRelationships() {
		return maxResGraphRelationships;
	}

	public void setMaxResGraphRelationships(int maxResGraphRelationships) {
		this.maxResGraphRelationships = maxResGraphRelationships;
	}

	/**
	 * Maximum number of Agent DiSCOs to display at bottom of RMap Agent view.  May be configured by setting the
	 * {@code rmapweb.max-agent-discos} property
	 *
	 * @return
	 */
	public int getMaxAgentDiSCOs() {
		return maxAgentDiSCOs;
	}

	public void setMaxAgentDiSCOs(int maxAgentDiSCOs) {
		this.maxAgentDiSCOs = maxAgentDiSCOs;
	}

	/**
	 * Maximum number of rows to be displayed in object or resource table view.  May be configured by setting the {@code
	 * rmapweb.max-table-rows} property.
	 *
	 * @return
	 */
	public int getMaxTableRows() {
		return maxTableRows;
	}

	public void setMaxTableRows(int maxTableRows) {
		this.maxTableRows = maxTableRows;
	}

	/**
	 * Maximum number of DiSCOs that reference a resource to display in right margin.  May be configured by setting the
	 * {@code rmapweb.max-resource-related-discos} property
	 *
	 * @return
	 */
	public int getMaxResRelatedDiSCOs() {
		return maxResRelatedDiSCOs;
	}

	public void setMaxResRelatedDiSCOs(int maxResRelatedDiSCOs) {
		this.maxResRelatedDiSCOs = maxResRelatedDiSCOs;
	}

	/**
	 * Maximum number of rows of literals to display in the node info popup on the graph.  May be configured by setting
	 * the {@code rmapweb.max-node-info-rows} property.
	 *
	 * @return
	 */
	public int getMaxNodeInfoRows() {
		return maxNodeInfoRows;
	}

	public void setMaxNodeInfoRows(int maxNodeInfoRows) {
		this.maxNodeInfoRows = maxNodeInfoRows;
	}
}
