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
package info.rmapproject.webapp.service;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import info.rmapproject.webapp.domain.ResourceDescription;
import info.rmapproject.webapp.domain.TripleDisplayFormat;
import info.rmapproject.webapp.exception.RMapWebException;
import info.rmapproject.webapp.service.dto.AgentDTO;
import info.rmapproject.webapp.service.dto.DiSCODTO;
import info.rmapproject.webapp.service.dto.EventDTO;
import info.rmapproject.webapp.service.dto.ResourceDTO;
import info.rmapproject.webapp.utils.WebappUtils;

/**
 * Implements Data Display Service
 *
 * @author khanson
 */

@Service("dataDisplayService")
@Transactional
public class DataDisplayServiceImpl implements DataDisplayService {

	//private static final Logger logger = LoggerFactory.getLogger(DiSCOServiceImpl.class);

	/** The RMap Service. */
	private RMapService rmapService;
	
	/** The DiSCO Node Type. */
	private String discoNodeType;
	
	/** The Agent Node Type. */
	private String agentNodeType;
	
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
		
		sDiscoUri = URLDecoder.decode(sDiscoUri, "UTF-8");
		
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
    	
	    List <RMapTriple> triples = disco.getRelatedStatements();    
	    
	    Graph graph = createDiSCOGraph(discoUri, discoDTO.getCreator(), aggregatedResources, triples);	  
	    discoDTO.setGraph(graph);
	    
	    List<ResourceDescription> resourceDescriptions = getResourceDescriptions(aggregatedResources, triples);
	    discoDTO.setResourceDescriptions(resourceDescriptions);

	    rmapService.closeConnection();
		return discoDTO;
	}


	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#getAgentDTO(java.lang.String)
	 */
	@Override
	public AgentDTO getAgentDTO(String sAgentUri) throws Exception {
				
		AgentDTO agentDTO = new AgentDTO();
		
		sAgentUri = URLDecoder.decode(sAgentUri, "UTF-8");
		URI agentUri = new URI(sAgentUri);	
		agentDTO.setUri(agentUri);
		
		RMapAgent agent = rmapService.readAgent(agentUri);
		agentDTO.setName(agent.getName());		
		agentDTO.setStatus(rmapService.getAgentStatus(agentUri));
		agentDTO.setEvents(rmapService.getAgentEvents(agentUri));
		agentDTO.setIdProvider(agent.getIdProvider().getStringValue());
		agentDTO.setAuthId(agent.getAuthId().getStringValue());
		
		RMapSearchParams params = new RMapSearchParams();
		params.setStatusCode(RMapStatusFilter.ACTIVE);
		agentDTO.setDiscos(rmapService.getAgentDiSCOs(agentUri, params).getResultList());
		
	    Graph graph = createAgentGraph(agentUri,  agentDTO.getName(),  agentDTO.getIdProvider(), agentDTO.getAuthId());	  
	    agentDTO.setGraph(graph);
	    	  	    	    
	    rmapService.closeConnection();
		
		return agentDTO;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#getEventDTO(java.lang.String)
	 */
	public EventDTO getEventDTO(String sEventUri) throws Exception {
			
		EventDTO eventDTO = new EventDTO();

		sEventUri = URLDecoder.decode(sEventUri, "UTF-8");
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
	 * @see info.rmapproject.webapp.service.DataDisplayService#getResourceDTO(java.lang.String)
	 */
	public ResourceDTO getResourceDTO(String sResourceUri) throws Exception{
		ResourceDTO resourceDTO = new ResourceDTO();

		sResourceUri = URLDecoder.decode(sResourceUri, "UTF-8");

		URI resourceUri = new URI(sResourceUri);
		resourceDTO.setUri(resourceUri);

		RMapSearchParams params = new RMapSearchParams();
		params.setStatusCode(RMapStatusFilter.ACTIVE);
		
		ResultBatch<RMapTriple> triplebatch = rmapService.getResourceRelatedTriples(resourceUri, params);
    	
		//if there are no triples, don't load an empty screen, show a not found error.
    	if (triplebatch.size()==0)	{
    		throw new RMapObjectNotFoundException();
    	}

    	List<RMapTriple> triples = triplebatch.getResultList();
    	
    	//generate resource description
    	ResourceDescription resourceDescription = getResourceDescription(sResourceUri, triples, false);	    
    	    		
	    resourceDTO.setResourceDescription(resourceDescription);
	    
	    ResultBatch<URI> relatedDiscoBatch = rmapService.getResourceRelatedDiSCOs(resourceUri, params);
	    resourceDTO.setRelatedDiSCOs(relatedDiscoBatch.getResultList());
	    
	    //used to create visual graph

	    Graph graph = new Graph();
		graph = addTriplesToGraph(graph, triples);  
	    resourceDTO.setGraph(graph);
	    	    
	    rmapService.closeConnection();
		return resourceDTO;
	}
	
	
	
	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#getResourceLiterals(java.lang.String)
	 */
	public ResourceDescription getResourceLiterals(String resourceUri) throws Exception{
		resourceUri = URLDecoder.decode(resourceUri, "UTF-8");
		URI uri = new URI(resourceUri);
		
		RMapSearchParams params = new RMapSearchParams();
		params.setStatusCode(RMapStatusFilter.ACTIVE);
		params.setLimit(ConfigUtils.getPropertyValue(info.rmapproject.core.utils.Constants.RMAPCORE_PROPFILE, 
				info.rmapproject.core.utils.Constants.MAX_QUERY_LIMIT_KEY));
		
		ResultBatch<RMapTriple> triplebatch = rmapService.getResourceRelatedTriples(uri, params);
		List<RMapTriple> triples = triplebatch.getResultList(); 
		
		ResourceDescription resourceDescription = getResourceDescription(resourceUri, triples, true);
		rmapService.closeConnection();
		return resourceDescription;
	}
	
	
	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#getResourceLiteralsInContext(java.lang.String,java.lang.String)
	 */
	public ResourceDescription getResourceLiteralsInContext(String sResourceUri, String sGraphUri) throws Exception{
		sResourceUri = URLDecoder.decode(sResourceUri, "UTF-8");
		sGraphUri = URLDecoder.decode(sGraphUri, "UTF-8");
		URI resourceUri = new URI(sResourceUri);
		URI graphUri = new URI(sGraphUri);
		
		RMapSearchParams params = new RMapSearchParams();
		params.setLimit(ConfigUtils.getPropertyValue(info.rmapproject.core.utils.Constants.RMAPCORE_PROPFILE, 
									info.rmapproject.core.utils.Constants.MAX_QUERY_LIMIT_KEY));
		params.setStatusCode(RMapStatusFilter.ACTIVE);
		
		ResultBatch<RMapTriple> resultbatch = rmapService.getResourceRelatedTriples(resourceUri, graphUri, params);
		List<RMapTriple> triples = resultbatch.getResultList();
		ResourceDescription resourceDescription = getResourceDescription(sResourceUri, triples, true);
		rmapService.closeConnection();
		return resourceDescription;
	}
	
	
	
	/**
	 * Generate resource description for resource based on triples provided. Will filter out triples that
	 * do not have a subject URI that matches the resource in question.
	 * @param resource
	 * @param triples
	 * @return resource description
	 */
	private ResourceDescription getResourceDescription(String resource, List<RMapTriple> triples, boolean literalsOnly) throws RMapWebException{

    	//start new resource description
    	ResourceDescription resourceDescription = new ResourceDescription(resource.toString());	  
    	
    	try {
	    	for (RMapTriple triple : triples) { 
	    		if (triple.getPredicate().toString().equals(RDF.TYPE.toString()) 
	    				&& triple.getSubject().toString().equals(resource.toString()))	{
	    			resourceDescription.addResourceType(triple.getObject().toString());
	    		}
	    		else if (!literalsOnly || triple.getObject() instanceof RMapLiteral) {
	    			//only include this if it's either a literal, or we aren't filtering by literals.
		    		TripleDisplayFormat tripleDF = new TripleDisplayFormat(triple);
	    			resourceDescription.addPropertyValue(tripleDF);		
	    		}
	    	}
    	} catch (UnsupportedEncodingException ex){
    		RMapWebException.wrap(ex);
    	}
    	
    	return resourceDescription;
	}
	
	
	
	/**
	 * Gets the resource descriptions.
	 *
	 * @param keyResource the key resource
	 * @param triples the triples
	 * @return the resource descriptions
	 * @throws Exception the exception
	 */
	@SuppressWarnings("unused")
	private List<ResourceDescription> getResourceDescriptions(URI keyResource, List<RMapTriple> triples) throws Exception  {
		List<URI> keyResources = new ArrayList<URI>();
		keyResources.add(keyResource);
		return getResourceDescriptions(keyResources, triples);		
	}

	/**
	 * Gets the resource descriptions.
	 *
	 * @param keyResources the key resources
	 * @param triples the triples
	 * @return the resource descriptions
	 * @throws Exception the exception
	 */
	private List<ResourceDescription> getResourceDescriptions(List<URI> keyResources, List<RMapTriple> triples) throws Exception {
		
	    List<ResourceDescription> resourceDescriptions = new ArrayList<ResourceDescription>();

	    //first extract unique list of resources mentioned in subject	
	    Set<String> resourcesDescribed = new LinkedHashSet<String>();
	    for (URI aggregate : keyResources) {
	    	resourcesDescribed.add(aggregate.toString());
	    }
	    for (RMapTriple stmt:triples) {
	    	resourcesDescribed.add(stmt.getSubject().toString());
	    }
	    
	    //now get resource description for each resource
	    for (String resource : resourcesDescribed) {
	    	ResourceDescription resDescrip = getResourceDescription(resource, triples, false);
	    	resourceDescriptions.add(resDescrip);	    	
	    }

	    return resourceDescriptions;
	}
	
	
	/**
	 * Creates the DiSCO graph.
	 *
	 * @param discoUri the DiSCO URI
	 * @param discoDescription the DiSCO description
	 * @param discoCreator the DiSCO creator
	 * @param aggregatedResources the aggregated resources
	 * @param triples the triples
	 * @return the graph
	 * @throws Exception the exception
	 */
	private Graph createDiSCOGraph(URI discoUri, 
			String discoCreator,
			List<URI> aggregatedResources,
			List<RMapTriple> triples) throws Exception {
		Graph graph = new Graph();
		String sDiscoUri = discoUri.toString();
						
		if (discoCreator.length()>0) {
			graph.addEdge(sDiscoUri, discoCreator, DCTERMS.CREATOR.toString(), discoNodeType, agentNodeType);
		}

		RMapSearchParams params = new RMapSearchParams();
		params.setStatusCode(RMapStatusFilter.ACTIVE);
		
		for (URI aggregate : aggregatedResources) {
			List<URI> rdfTypes = rmapService.getResourceRdfTypesInDiSCO(aggregate, discoUri);
			String targetNodeType = WebappUtils.getNodeType(rdfTypes);
			graph.addEdge(sDiscoUri, aggregate.toString(),Terms.ORE_AGGREGATES_PATH, discoNodeType, targetNodeType);
		}

		graph = addTriplesToGraph(graph, triples, discoUri);
		
		rmapService.closeConnection();
		return graph;
		}	
		
		/**
		 * Creates the Agent graph.
		 *
		 * @param agentUri the Agent URI
		 * @param agentName the Agent name
		 * @param idProvider the ID provider
		 * @param authId the auth ID
		 * @return the graph
		 * @throws Exception the exception
		 */
		private Graph createAgentGraph(URI agentUri, 
				String agentName,
				String idProvider,
				String authId) throws Exception {
	
			Graph graph = new Graph();
			String sAgentUri = agentUri.toString();
			
			graph.addEdge(sAgentUri, idProvider,Terms.RMAP_IDENTITYPROVIDER_PATH, agentNodeType, agentNodeType);
			graph.addEdge(sAgentUri, authId,Terms.RMAP_USERAUTHID_PATH, agentNodeType, WebappUtils.getNodeType(new URI(Terms.RMAP_USERAUTHID_PATH)));
			return graph;
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
				
				//types and literals do not go in the graph
				if (!(object instanceof RMapLiteral) 
						&& !(predicate.toString().equals(RDF.TYPE.toString()))) {
				
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
		private List<URI> getResourceRDFTypes(URI resource) throws Exception{
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
