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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.collections4.map.MultiValueMap;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import info.rmapproject.core.model.RMapIri;
import info.rmapproject.core.model.RMapLiteral;
import info.rmapproject.core.model.RMapResource;
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
import info.rmapproject.core.model.request.ResultBatch;
import info.rmapproject.core.rmapservice.RMapService;
import info.rmapproject.core.utils.Terms;
import info.rmapproject.core.vocabulary.impl.rdf4j.RMAP;
import info.rmapproject.webapp.domain.Graph;
import info.rmapproject.webapp.domain.PageStatus;
import info.rmapproject.webapp.domain.PaginatorType;
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
public class DataDisplayServiceImpl implements DataDisplayService {

	private static final Logger log = LoggerFactory.getLogger(DataDisplayServiceImpl.class);

	/** The RMap Service. */
	private RMapService rmapService;
	
	/** Search service to retrieve data from index */
	private SearchService searchService;
		
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

	/** The label type list. */
	@Value("${rmapweb.label-types}")
	private String labelTypes;
	
	private GraphFactory graphFactory;

	private TripleDisplayFormatFactory tripleDisplayFactory;

	/**
	 * Instantiates a new data display service implementation.
	 * @param rmapService
	 * @param paramsFactory
	 * @param graphFactory
	 * @param tripleDisplayFactory
	 */
	@Autowired 
	public DataDisplayServiceImpl(RMapService rmapService, SearchService searchService, 
			GraphFactory graphFactory, TripleDisplayFormatFactory tripleDisplayFactory){
		this.rmapService = rmapService;
		this.searchService = searchService;
		this.graphFactory = graphFactory;
		this.tripleDisplayFactory = tripleDisplayFactory;
		//this is to support customization of node types - support those who would like to categorize DiSCO and Agent 
		//as something other than default.
		try {
			discoNodeType = WebappUtils.getNodeType(new URI(Terms.RMAP_DISCO_PATH));
			agentNodeType = WebappUtils.getNodeType(new URI(Terms.RMAP_AGENT_PATH));
		} catch (Exception e){
			//set default
			discoNodeType = Constants.NODETYPE_OTHER;
			agentNodeType = Constants.NODETYPE_OTHER;
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
		discoDTO.setProviderId(disco.getProviderId());
		
		discoDTO.setAgentVersions(rmapService.getDiSCOVersions(discoUri));
		discoDTO.setAllVersions(rmapService.getDiSCODVersionsAndDerivatives(discoUri));
		
		discoDTO.setStatus(rmapService.getDiSCOStatus(discoUri));
		discoDTO.setEvents(rmapService.getDiSCOEvents(discoUri));
    	discoDTO.setAggregatedResources(aggregatedResources);
	    discoDTO.setRelatedStatements(disco.getRelatedStatements());

		return discoDTO;		
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#getDiSCOGraph(info.rmapproject.webapp.service.dto.DiSCODTO)
	 */
	@Override
	public Graph getDiSCOGraph(DiSCODTO discoDTO) throws Exception {
		Graph graph = graphFactory.newGraph();
		String sDiscoUri = discoDTO.getUri().toString();
		
		graph.addNode(sDiscoUri, sDiscoUri, discoNodeType);
						
		if (WebappUtils.isUri(discoDTO.getCreator())) {
			graph.addNode(discoDTO.getCreator(), agentNodeType);
			graph.addEdge(sDiscoUri, discoDTO.getCreator(), DCTERMS.CREATOR.toString());
		}
		
		if (WebappUtils.isUri(discoDTO.getProviderId())) {
			List<URI> rdfTypes = rmapService.getResourceRdfTypesInContext(new URI(discoDTO.getProviderId()), discoDTO.getUri());
			String targetNodeType = WebappUtils.getNodeType(rdfTypes);
			graph.addNode(discoDTO.getProviderId(), targetNodeType);
			graph.addEdge(sDiscoUri, discoDTO.getProviderId(), RMAP.PROVIDERID.toString());
		}
		
		for (URI aggregate : discoDTO.getAggregatedResources()) {
			List<URI> rdfTypes = rmapService.getResourceRdfTypesInContext(aggregate, discoDTO.getUri());
			String targetNodeType = WebappUtils.getNodeType(rdfTypes);
			graph.addNode(aggregate.toString(), targetNodeType);
			graph.addEdge(sDiscoUri, aggregate.toString(), Terms.ORE_AGGREGATES_PATH);
		}

		List<RMapTriple> triples = discoDTO.getRelatedStatements();

		List<RMapTriple> filteredTriples = new ArrayList<RMapTriple>();
		List<RMapTriple> labelTriples = new ArrayList<RMapTriple>();
		List<String> labelFlds = Arrays.asList(this.labelTypes.split(","));
		
		MultiValueMap<String,URI> typeMap = new MultiValueMap<String,URI>();
		
		//sort in triples into lists that will be used for graph creation
		for (RMapTriple triple:triples){
			RMapResource subj = triple.getSubject();
			RMapIri pred = triple.getPredicate();
			RMapValue obj = triple.getObject();
			
			boolean isType = pred.toString().equals(RDF.TYPE.toString());
			
			if (!isType	&& !(obj instanceof RMapLiteral)){
				filteredTriples.add(triple);
			}
			
			if (isType && obj instanceof RMapIri) {
				typeMap.put(subj.toString(), new URI(obj.toString()));
			}
			
			if (obj instanceof RMapLiteral && labelFlds.contains(pred.toString())) {				
				labelTriples.add(triple);
			}
			
		}
		
		triples = null;

		if ((filteredTriples.size()+graph.getEdges().size())<=maxObjGraphRelationships){						
			for (RMapTriple triple : filteredTriples) {
				String subject = triple.getSubject().toString();
				String predicate = triple.getPredicate().toString();
				String object = triple.getObject().toString();
				
				List<URI> subjTypes = null;
				List<URI> objTypes = null;
				if (typeMap!=null && typeMap.getCollection(subject)!=null) {
					new ArrayList<URI>(typeMap.getCollection(subject));
				}
				if (typeMap!=null && typeMap.getCollection(object)!=null) {
					objTypes = new ArrayList<URI>(typeMap.getCollection(object));
				}
				
				graph.addNode(subject, WebappUtils.getNodeType(subjTypes));
				graph.addNode(object, WebappUtils.getNodeType(objTypes));
				graph.addEdge(subject, object, predicate);
			}
			
			//make node labels from label triples within the graph
			for (RMapTriple triple:labelTriples) {			
				if (graph.getNodes().containsKey(triple.getSubject().toString())) {
					graph.getNodes().get(triple.getSubject().toString()).setLabel(triple.getObject().toString());
				}
			}
			
		} else {
			//don't do graph because it's too large and will just be an unbearable mess!!
			graph = null;
		}
		return graph;
	}	

	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#getDiSCOTableData(info.rmapproject.webapp.service.dto.DiSCODTO, java.lang.Integer)
	 */
	@Override
	public List<ResourceDescription> getDiSCOTableData(DiSCODTO discoDTO, Integer offset) throws Exception {
		
		List<ResourceDescription> resourceDescriptions = new ArrayList<ResourceDescription>();

		List<URI> aggregatedResources = discoDTO.getAggregatedResources(); 
		List<RMapTriple> triples = discoDTO.getRelatedStatements(); 
		
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
	    	ResourceDescription resDescrip = getResourceTableDataInContext(resource, triples, discoDTO.getUri().toString(),false);  	
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
		
	
	/*************************************************************
	 * RESOURCE PATH METHODS
	 */
	

	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#getResourceBatch(java.lang.String, info.rmapproject.core.model.request.RMapSearchParams, info.rmapproject.webapp.domain.PaginatorType)
	 */
	@Override
	public ResultBatch<RMapTriple> getResourceBatch(String resourceUri, RMapSearchParams params, PaginatorType view) throws Exception {
		
		ResultBatch<RMapTriple> triplebatch = null;
		URI uri = new URI(resourceUri);

		params.setExcludeTypes(true);	
		if (view.equals(PaginatorType.RESOURCE_GRAPH)){
			params.setExcludeLiterals(true);	
			params.setLimit(maxResGraphRelationships);
		} else {
			params.setExcludeLiterals(false);	
			params.setLimit(maxTableRows);
		}
		
		triplebatch = rmapService.getResourceRelatedTriples(uri, params);

    	return triplebatch;
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#getResourceGraph(info.rmapproject.core.model.request.ResultBatch<RMapTriple>)
	 */
	@Override
	public Graph getResourceGraph(ResultBatch<RMapTriple> triplebatch, RMapSearchParams params) throws Exception {
		Graph graph = graphFactory.newGraph();
				
		List<RMapTriple> triples = triplebatch.getResultList();
		
		Map<String,String> nodeTypeMap = generateTypeMapForTriples(triples, params);
		for (RMapTriple triple : triples) {
			String subject = triple.getSubject().toString();
			String predicate = triple.getPredicate().toString();
			String object = triple.getObject().toString();
			
			graph.addNode(subject, nodeTypeMap.get(subject));
			graph.addNode(object, nodeTypeMap.get(object));
			graph.addEdge(subject, object, predicate);
		}
		
		//generate node labels
		for (String node: graph.getNodes().keySet()) {
			String label = getResourceLabel(node, params);
			if (label!=null) {
				graph.getNodes().get(node).setLabel(label);
			}
			log.debug("Label for node {} is {}" + label, node, label);
		}		
		return graph;	    
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#getResourceTableData(java.lang.String, info.rmapproject.core.model.request.ResultBatch<RMapTriple>,info.rmapproject.core.model.request.RMapStatusFilter)
	 */
	@Override
	public ResourceDescription getResourceTableData(String resourceUri, ResultBatch<RMapTriple> triplebatch, RMapSearchParams params, boolean bothdirections) throws RMapWebException {
    	ResourceDescription rd = new ResourceDescription(resourceUri);	  
    	try {
    		List<URI> resourceTypes = getResourceRDFTypes(new URI(resourceUri), params);
    		rd.addResourceTypes(resourceTypes);
    		
    		Set<TripleDisplayFormat> dfTriples = tripleBatchToTripleDFSet(resourceUri, triplebatch, bothdirections);
    		rd.addPropertyValues(dfTriples);
    		
    	} catch (RMapWebException ex){
    		throw ex;
    	} catch (Exception ex){
    		RMapWebException.wrap(ex);
    	}    	
    	return rd;
	}			

	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#getResourceTableData(java.lang.String, info.rmapproject.core.model.request.ResultBatch<RMapTriple>, java.lang.String,info.rmapproject.core.model.request.RMapStatusFilter)
	 */
	@Override
	public ResourceDescription getResourceTableDataInContext(String resourceUri, ResultBatch<RMapTriple> triplebatch, String contextUri, boolean bothDirections) throws Exception {
		List<RMapTriple> triples = triplebatch.getResultList();    	
		return getResourceTableDataInContext(resourceUri, triples, contextUri, bothDirections);
	}

	/**
	 * Generate resource description for resource based on triples provided. Assumes all triples have been returned, unlike for
	 * plain resource description where it may be truncated. Will filter out triples that do not have a subject URI that matches 
	 * the resource in question.
	 * @param resource the resource URI
	 * @param triples list of RMapTriples
	 * @return resource description
	 */	
	private ResourceDescription getResourceTableDataInContext(String resourceUri, List<RMapTriple> triples, String contextUri, boolean bothDirections) throws Exception {
    	ResourceDescription rd = new ResourceDescription(resourceUri);	  
    	try {
    		List<URI> resourceTypes = rmapService.getResourceRdfTypesInContext(new URI(resourceUri), new URI(contextUri));
    		rd.addResourceTypes(resourceTypes);
    		
    		Set<TripleDisplayFormat> dfTriples = triplesToTripleDFSet(resourceUri, triples, bothDirections);
    		rd.addPropertyValues(dfTriples);
    	} catch (RMapWebException ex){
    		throw ex;
    	} catch (Exception ex){
    		RMapWebException.wrap(ex);
    	}
    	
    	return rd;
	}		
	
	
	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#getResourceRelatedDiSCOs(java.lang.String, java.lang.Integer,info.rmapproject.core.model.request.RMapStatusFilter)
	 */
	@Override
	public ResultBatch<URI> getResourceRelatedDiSCOs(String resourceUri, RMapSearchParams params) throws Exception {
		params.setLimit(maxResRelatedDiSCOs);
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
		
		return agentDTO;
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#getAgentDTO(info.rmapproject.webapp.service.dto.AgentDTO)
	 */
	@Override
	public Graph getAgentGraph(AgentDTO agentDTO) throws Exception{
		Graph graph = graphFactory.newGraph();
		String sAgentUri = agentDTO.getUri().toString();
		
		graph.addNode(sAgentUri, agentDTO.getName(), agentNodeType);
		graph.addNode(agentDTO.getIdProvider(), agentNodeType);
		graph.addNode(agentDTO.getAuthId(), WebappUtils.getNodeType(new URI(Terms.RMAP_USERAUTHID_PATH)));
		
		graph.addEdge(sAgentUri, agentDTO.getIdProvider(), Terms.RMAP_IDENTITYPROVIDER_PATH);
		graph.addEdge(sAgentUri, agentDTO.getAuthId(), Terms.RMAP_USERAUTHID_PATH);

		return graph;			
	}	
	
	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#getAgentDTO(java.lang.String, java.lang.Integer)
	 */
	@Override
	public ResultBatch<URI> getAgentDiSCOs(String agentUri, RMapSearchParams params) throws Exception {
		params.setLimit(maxAgentDiSCOs);
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
	    return eventDTO;
	}
	
	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#getResourceLiterals(java.lang.String, java.lang.Integer,info.rmapproject.core.model.request.RMapStatusFilter)
	 */
	@Override
	public ResultBatch<RMapTriple> getResourceLiterals(String resourceUri, RMapSearchParams params) throws Exception{
		
		URI uri = new URI(resourceUri);
		params.setExcludeIRIs(true);
		params.setExcludeTypes(true);
		params.setLimit(maxNodeInfoRows);
		
		ResultBatch<RMapTriple> triplebatch =rmapService.getResourceRelatedTriples(uri, params);
		
		return triplebatch;
	}
	
	
	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#getResourceLiteralsInContext(java.lang.String, java.lang.String, java.lang.Integer)
	 */
	@Override
	public ResultBatch<RMapTriple> getResourceLiteralsInContext(String sResourceUri, String sGraphUri, RMapSearchParams params) throws Exception{

		URI resourceUri = new URI(sResourceUri);
		URI graphUri = new URI(sGraphUri);
		
		params.setExcludeIRIs(true);
		params.setLimit(maxNodeInfoRows);
		
		ResultBatch<RMapTriple> resultbatch= rmapService.getResourceRelatedTriples(resourceUri, graphUri, params);

		return resultbatch;
	}
	
	/**
	 * Extracts triples from triplebatch that are connected to resourceUri and formats them as TripleDisplayFormat
	 * do not have a subject URI that matches the resource in question.
	 * @param resourceUri The resource URI to retrieve a description for
	 * @param triples ResultBatch of RMapTriples to generate resource description from
	 * @param if true will return triples whether subject or object matches, false only where subject matches.
	 * @return set of TripleDisplayFormats
	 */
	private Set<TripleDisplayFormat> tripleBatchToTripleDFSet(String resourceUri, ResultBatch<RMapTriple> triplebatch, boolean bothDirections) throws RMapWebException{
		List<RMapTriple> triples = triplebatch.getResultList();    	
		return triplesToTripleDFSet(resourceUri, triples, bothDirections);
	}
	
	/**
	 * Extracts triples from triple list that are connected to resourceUri and formats them as TripleDisplayFormat
	 * do not have a subject URI that matches the resource in question.
	 * @param resourceUri The resource URI to retrieve a description for
	 * @param triples List of RMapTriples to generate resource description from
	 * @param if true will return triples whether subject or object matches, false only where subject matches.
	 * @return set of TripleDisplayFormats
	 */
	private Set<TripleDisplayFormat> triplesToTripleDFSet(String resourceUri, List<RMapTriple> triples, boolean bothDirections) throws RMapWebException{
		Set<TripleDisplayFormat> tripleDfs = new HashSet<TripleDisplayFormat>();	
		for (RMapTriple triple : triples) { 
    		boolean objectIsLiteral = (triple.getObject() instanceof RMapLiteral);
    		boolean subjMatchesResource = triple.getSubject().toString().equals(resourceUri.toString());
    		boolean objMatchesResource = !objectIsLiteral && triple.getObject().toString().equals(resourceUri.toString());
    		boolean predIsRdfType = triple.getPredicate().toString().equals(RDF.TYPE.toString());
    		
    		if (!predIsRdfType && (subjMatchesResource || (objMatchesResource && bothDirections))) {
	    		//only include this if subj or object matches resource
		    	TripleDisplayFormat tripleDF = tripleDisplayFactory.newTripleDisplayFormat(triple);
		    	tripleDfs.add(tripleDF);		
	    	}
    	}
		return tripleDfs;
	}
	
		
	private Map<String,String> generateTypeMapForTriples(List<RMapTriple> triples, RMapSearchParams params) throws Exception {
		Map<String,String> nodeTypesMap = new HashMap<String,String>();
		Set<String> uniqueUris = extractUniqueUris(triples);
		for (String uniqueUri : uniqueUris) {
			List <URI> sourceRdfTypes = this.getResourceRDFTypes(new URI(uniqueUri), params);
			String type = WebappUtils.getNodeType(sourceRdfTypes);
			nodeTypesMap.put(uniqueUri,type);
		}
		return nodeTypesMap;
	}
	
	
	private Set<String> extractUniqueUris(List<RMapTriple> triples) {
		Set<String> uniqueUris = triples.stream()
				.map(triple -> triple.getSubject().getStringValue()).collect(Collectors.toSet());
		
		uniqueUris.addAll(triples.stream()
				.filter(t -> t.getObject() instanceof RMapIri)
				.map(triple -> triple.getObject().getStringValue()).collect(Collectors.toSet()));
		
		return uniqueUris;
	}
	

	
	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#getResourceLabel(java.lang.String,info.rmapproject.core.model.request.RMapStatusFilter)
	 */
	@Override
	public String getResourceLabel(String resourceUri, RMapSearchParams params) throws Exception{
		String preferredLabel = null;
		Integer highestCount = 0;
		Map<String,Integer> countLabel = new HashMap<String,Integer>();
		
		int NUM_MATCHES = 1000;		
		Pageable pageable = PageRequest.of(0, NUM_MATCHES);
		List<String> labels = searchService.getLabelListForResource(resourceUri, params, pageable);
		
		for(String label:labels) {
			int count = countLabel.containsKey(label) ? countLabel.get(label)+1 : 1;
			countLabel.put(label, count);
			if (highestCount<count) {
				highestCount=count;
				preferredLabel=label;
			}
			log.debug("Label count for {} is {}", label, count);
		}

		log.debug("Preferred label for resource uri {} is {}", resourceUri, preferredLabel);
		
		return preferredLabel;
	}	
	
	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#getResourceLiterals(java.net.URI,info.rmapproject.core.model.request.RMapStatusFilter)
	 */
	@Override
	public List<URI> getResourceRDFTypes(URI resourceUri, RMapSearchParams params) throws Exception{
		List<URI> rdfTypes = new ArrayList<URI>();
		
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
	    	RMapIri uri = deletionEvent.getDeletedObjectId();
    		String type = getRMapTypeDisplayName(uri);
    		resourcesAffected.put(uri.toString(), "Deleted " + type);
	    }
	    else if (eventType == RMapEventType.TOMBSTONE)	{
	    	RMapEventTombstone tombstoneEvent = (RMapEventTombstone) event;
	    	RMapIri uri = tombstoneEvent.getTombstonedObjectId();
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
			
		log.debug("Checking type for URI {}", ((resourceUri==null) ? "" : resourceUri.toString()));
					
		if (rmapService.isDiSCOId(resourceUri)) {
			log.debug("Type identified as a rmap:DiSCO");
			return Terms.RMAP_DISCO;			
		}
		
		if (rmapService.isAgentId(resourceUri)) {
			log.debug("Type identified as a rmap:Agent");
			return Terms.RMAP_AGENT;			
		}
		if (rmapService.isEventId(resourceUri)) {
			log.debug("Type identified as an rmap:Event");
			return Terms.RMAP_EVENT;			
		}		
		
		log.debug("Type not identified, it's a non-RMap resource");
		//otherwise
		return "";
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.webapp.service.DataDisplayService#isResourceInRMap(java.lang.String,info.rmapproject.core.model.request.RMapSearchParams)
	 */
	@Override
	public boolean isResourceInRMap(String resource, RMapSearchParams params) throws Exception {
		if (WebappUtils.isUri(resource)) {
			return rmapService.getResourceRelatedTriples(new URI(resource), params).size()>0;
		}
		return false;
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
