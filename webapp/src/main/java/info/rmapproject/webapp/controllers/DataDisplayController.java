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
package info.rmapproject.webapp.controllers;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import info.rmapproject.core.utils.Terms;
import info.rmapproject.webapp.domain.ResourceDescription;
import info.rmapproject.webapp.domain.SearchCommand;
import info.rmapproject.webapp.exception.ErrorCode;
import info.rmapproject.webapp.exception.RMapWebException;
import info.rmapproject.webapp.service.DataDisplayService;
import info.rmapproject.webapp.service.dto.AgentDTO;
import info.rmapproject.webapp.service.dto.DiSCODTO;
import info.rmapproject.webapp.service.dto.EventDTO;
import info.rmapproject.webapp.service.dto.ResourceDTO;

/**
 * Handles requests for the data visualization pages.
 *
 * @author khanson
 */

@Controller
@SessionAttributes({"user","account"})
public class DataDisplayController {

	/** Service for managing RMap data display. */
	@Autowired
	private DataDisplayService dataDisplayService;

	/** The log. */
	private static final Logger log = LoggerFactory.getLogger(DataDisplayController.class);
	
	/**  path parameter for large visualization view. */
	private static final String VISUAL_VIEW = "visual";
	
	/**  path parameter for widget view. */
	private static final String WIDGET_VIEW = "widget";

	/**  term for standard view, used in VIEWMODE. */
	private static final String STANDARD_VIEW = "standard";

	/**  term for table data view, used in VIEWMODE. */
	private static final String TABLEDATA_VIEW = "tabledata";
		
	/**
	 * path parameter for the edit view 
	 * currently partially works for DiSCOs only... is part of proof of concept for DiSCO edit.
	 */
	private static final String EDIT_VIEW = "edit";
	
	
	
	/**
	 * GET details of a DiSCO.
	 *
	 * @param discoUri the disco uri
	 * @param model the Spring model
	 * @return the discos page
	 * @throws Exception the exception
	 */
	@RequestMapping(value="/discos/{uri}", method = RequestMethod.GET)
	public String disco(@PathVariable(value="uri") String discoUri, 
				Model model) throws Exception {
		log.info("DiSCO requested: " + discoUri);
		DiSCODTO discoDTO = dataDisplayService.getDiSCODTO(discoUri);
	    model.addAttribute("DISCO",discoDTO);	    
	    model.addAttribute("OBJECT_NODES", discoDTO.getGraph().getNodes());
	    model.addAttribute("OBJECT_EDGES", discoDTO.getGraph().getEdges());
	    model.addAttribute("OBJECT_NODETYPES", discoDTO.getGraph().getNodeTypes());
	    model.addAttribute("RESOURCEURI", discoDTO.getUri());
	    model.addAttribute("PAGEPATH", "discos");
	    model.addAttribute("VIEWMODE", STANDARD_VIEW);
	    
		return "discos";
	}	
	
	/**
	 * GET details of a DiSCO in non-default view.
	 *
	 * @param discoUri the disco uri
	 * @param view the view
	 * @param model the Spring model
	 * @return the discos page
	 * @throws Exception the exception
	 */
	@RequestMapping(value="/discos/{uri}/{view}", method = RequestMethod.GET)
	public String discoAltView(@PathVariable(value="uri") String discoUri, 
			@PathVariable(value="view") String view, Model model) throws Exception {
		log.info("DiSCO visualization requested: " + discoUri);
		if (view==null || view.length()==0){
			view=STANDARD_VIEW;
		}
		DiSCODTO discoDTO = dataDisplayService.getDiSCODTO(discoUri);
	    model.addAttribute("DISCO",discoDTO);	    
	    model.addAttribute("OBJECT_NODES", discoDTO.getGraph().getNodes());
	    model.addAttribute("OBJECT_EDGES", discoDTO.getGraph().getEdges());
	    model.addAttribute("OBJECT_NODETYPES", discoDTO.getGraph().getNodeTypes());   
	    model.addAttribute("RESOURCEURI", discoDTO.getUri());
	    model.addAttribute("VIEWMODE", view);
	    if (view.equals(VISUAL_VIEW)){
	    	return "discovisual";
	    } else if (view.equals(WIDGET_VIEW)) {
	    	return "discowidget";	    	
	    } else if (view.equals(EDIT_VIEW)) {
			return "discoedit";    	
	    } else if (view.equals(TABLEDATA_VIEW)) {
			return "discotable";
	    } else {
	    	return "discos";
	    }	    
	}	
	
	/**
	 * GET details of an Agent.
	 *
	 * @param agentUri the agent uri
	 * @param model the Spring model
	 * @return the agents page
	 * @throws Exception the exception
	 */
	@RequestMapping(value="/agents/{uri}", method = RequestMethod.GET)
	public String agent(@PathVariable(value="uri") String agentUri, Model model) throws Exception {
		log.info("Agent requested: " + agentUri);	
		AgentDTO agentDTO = dataDisplayService.getAgentDTO(agentUri);
	    model.addAttribute("AGENT",agentDTO);	    
	    model.addAttribute("OBJECT_NODES", agentDTO.getGraph().getNodes());
	    model.addAttribute("OBJECT_EDGES", agentDTO.getGraph().getEdges());
	    model.addAttribute("OBJECT_NODETYPES", agentDTO.getGraph().getNodeTypes()); 
	    model.addAttribute("RESOURCEURI", agentDTO.getUri());
	    model.addAttribute("PAGEPATH", "agents");
	    model.addAttribute("VIEWMODE", STANDARD_VIEW);
	    
		return "agents";
	}	
	
	
	/**
	 * GET details of a Agent in non-default view.
	 *
	 * @param agentUri the agent uri
	 * @param view the view
	 * @param model the Spring model
	 * @return the agents page
	 * @throws Exception the exception
	 */
	@RequestMapping(value="/agents/{uri}/{view}", method = RequestMethod.GET)
	public String agentAltView(@PathVariable(value="uri") String agentUri, 
			@PathVariable(value="view") String view, Model model) throws Exception {
		log.info("Agent requested: " + agentUri);	
		if (view==null || view.length()==0){
			view=STANDARD_VIEW;
		}
		AgentDTO agentDTO = dataDisplayService.getAgentDTO(agentUri);
	    model.addAttribute("AGENT",agentDTO);	    
	    model.addAttribute("OBJECT_NODES", agentDTO.getGraph().getNodes());
	    model.addAttribute("OBJECT_EDGES", agentDTO.getGraph().getEdges());
	    model.addAttribute("OBJECT_NODETYPES", agentDTO.getGraph().getNodeTypes());
	    model.addAttribute("RESOURCEURI", agentDTO.getUri());
	    model.addAttribute("VIEWMODE", view);
	    
	    if (view.equals(VISUAL_VIEW)){
	    	return "agentvisual";
	    } else if (view.equals(WIDGET_VIEW)) {
	    	return "agentwidget";	
	    } else if (view.equals(TABLEDATA_VIEW)){
	    	return "agenttable";
	    } else {
			return "agents";
	    }	    
	}
	
	
	/**
	 * GET details of a resource.
	 *
	 * @param sResourceUri the resource uri
	 * @param resview - This is for when a URI is passed in that may be an RMap object URI (Agent, DiSCO, Event).
	 * 					When resview==0, it will check for an RMap type, and where one is found the appropriate 
	 * 					RMap object page will be displayed instead of the generic resources page. When resview==1, 
	 * 					the /resources page will be displayed by default. Default is 0
	 * @param model the Spring model
	 * @param redirectAttributes holds flash attributes passed from a redirect
	 * @return the resources page or redirect back to search on error
	 * @throws Exception the exception
	 */
	@RequestMapping(value="/resources/{uri}", method = RequestMethod.GET)
	public String resource(@PathVariable(value="uri") String sResourceUri, 
				@RequestParam(value="resview", required=false) Integer resview, 
				Model model, RedirectAttributes redirectAttributes) throws Exception {
		
		log.info("Resource requested " + sResourceUri);

		if (resview == null) {
			resview = 0;
		}
		boolean isUri = true;
		
		try {
			if (resview==0) {
				String rmapType = dataDisplayService.getRMapTypeDisplayName(new URI(sResourceUri));
				if (rmapType.length()>0){
					return "redirect:/" + rmapType.toLowerCase() + "s/" + URLEncoder.encode(sResourceUri, "UTF-8");
				}				
			}
				
			ResourceDTO resourceDTO = dataDisplayService.getResourceDTO(sResourceUri);
		    model.addAttribute("RESOURCE",resourceDTO);	    
		    model.addAttribute("OBJECT_NODES", resourceDTO.getGraph().getNodes());
		    model.addAttribute("OBJECT_EDGES", resourceDTO.getGraph().getEdges());
		    model.addAttribute("OBJECT_NODETYPES", resourceDTO.getGraph().getNodeTypes());
		    model.addAttribute("RESOURCEURI", resourceDTO.getUri());
		    model.addAttribute("PAGEPATH", "resources");
		    model.addAttribute("VIEWMODE", STANDARD_VIEW);

		} catch (URISyntaxException|IllegalArgumentException ex){
			isUri = false;
			log.warn(ex.getMessage() + ". Submitted value: " + sResourceUri + ".");
		}
		if (!isUri){
			SearchCommand search = new SearchCommand();
			search.setSearch(sResourceUri);
			redirectAttributes.addFlashAttribute("search", search);
			redirectAttributes.addFlashAttribute("notice", "<strong>" + sResourceUri + "</strong> is not a valid URI. Currently only URI searches are supported.");	
			return "redirect:/search";
		}		
	    
		return "resources";
	}
	

	/**
	 * GET details of a resource and return in specific view format.
	 *
	 * @param resourceUri the resource uri
	 * @param view - determines the kind of view that will be returned - widget or visualize
	 * @param resview - This is for when a URI is passed in that may be an RMap object URI (Agent, DiSCO, Event).
	 * 					When resview==0, it will check for an RMap type, and where one is found the appropriate 
	 * 					RMap object page will be displayed instead of the generic resources page. When resview==1, 
	 * 					the /resources page will be displayed by default.
	 * @param model the Spring model
	 * @return the resources page
	 * @throws Exception the exception
	 */
	@RequestMapping(value="/resources/{uri}/{view}", method = RequestMethod.GET)
	public String resourceAltView(@PathVariable(value="uri") String resourceUri, 
				@PathVariable(value="view") String view, 
				@RequestParam(value="resview", required=false) Integer resview, 
				Model model) throws Exception {
		log.info("Resource requested " + resourceUri);
		if (view==null || view.length()==0){
			view=STANDARD_VIEW;
		}

		//by default the system will redirect to the object type 
		//e.g. a disco uri will redirect to DiSCO view.
		if (resview == null) {
			resview = 0;
		}

		if (resview==0) {
			//decode http first
			resourceUri = URLDecoder.decode(resourceUri, "UTF-8");
			//TODO: need to handle exception properly
			String rmapType = dataDisplayService.getRMapTypeDisplayName(new URI(resourceUri));
			if (rmapType.length()>0){
				return "redirect:/" + rmapType.toLowerCase() + "s/" + URLEncoder.encode(resourceUri, "UTF-8") + "/" + view;
			}
		}
		ResourceDTO resourceDTO = dataDisplayService.getResourceDTO(resourceUri);
	    model.addAttribute("RESOURCE",resourceDTO);	    
	    model.addAttribute("OBJECT_NODES", resourceDTO.getGraph().getNodes());
	    model.addAttribute("OBJECT_EDGES", resourceDTO.getGraph().getEdges());
	    model.addAttribute("OBJECT_NODETYPES", resourceDTO.getGraph().getNodeTypes());
	    model.addAttribute("RESOURCEURI", resourceDTO.getUri());
	    model.addAttribute("VIEWMODE", view);
	    
	    if (view.equals(WIDGET_VIEW)){
			return "resourcewidget";	    	
	    } else if (view.equals(VISUAL_VIEW)){
	    	return "resourcevisual";
	    } else if (view.equals(TABLEDATA_VIEW)){
	    	return "resourcetable";
	    } else {
	    	return "resources";
	    }
	}
	
			
	/**
	 * GET details of an Event.
	 *
	 * @param eventUri the event uri
	 * @param model the Spring model
	 * @return the events page
	 * @throws Exception the exception
	 */
	@RequestMapping(value="/events/{uri}", method = RequestMethod.GET)
	public String event(@PathVariable(value="uri") String eventUri, Model model) throws Exception {
		log.info("Event requested " + eventUri);
				
		EventDTO eventDTO = dataDisplayService.getEventDTO(eventUri);
		model.addAttribute("EVENT", eventDTO);
	    model.addAttribute("RESOURCEURI", eventDTO.getUri());
	    model.addAttribute("VIEWMODE", STANDARD_VIEW);
		
		return "events";
	}	
	

	/**
	 * Retrieves information about the node to be formatted in a popup
	 *
	 * @param resourceUri a resource uri
	 * @param model the Spring model
	 * @param view the current page view
	 * @param referer - the page that called the popup
	 * @return the resource literals popup box
	 * @throws Exception the exception
	 */
	@RequestMapping(value="/nodeinfo/{resource}", method = RequestMethod.GET)
	public String resourceLiterals(@PathVariable(value="resource") String resourceUri, 
				Model model, @RequestParam(value="viewmode", required=false) String view,
				@RequestHeader("referer") String referer) throws Exception {
		if (view==null || view.length()==0){
			view=STANDARD_VIEW;
		}
		try {
			ResourceDescription resourceLiterals = dataDisplayService.getResourceLiterals(resourceUri);
			boolean isRMapType = resourceLiterals.getResourceTypes().containsKey(Terms.RMAP_DISCO_PATH)
									|| resourceLiterals.getResourceTypes().containsKey(Terms.RMAP_AGENT_PATH)
									|| resourceLiterals.getResourceTypes().containsKey(Terms.RMAP_EVENT_PATH);
			model.addAttribute("ISRMAPTYPE",isRMapType);	  
			model.addAttribute("RESDES",resourceLiterals);	  
		    model.addAttribute("VIEWMODE", view); 
		    model.addAttribute("REFERER",referer);
		} catch (Exception e) {
			//we need to mark this as a nodeinfo error - it needs to load a smaller custom exception page
			throw new RMapWebException(e,ErrorCode.ER_PROBLEM_LOADING_NODEINFO);
		}
	    
		return "nodeinfo";
	}	
		
	/**
	 * Retrieves information about the node to be formatted in a popup
	 *
	 * @param resourceUri a resource uri
	 * @param contextUri a context uri - uri of graph to limit results by
	 * @param model the Spring model
	 * @param view the current page view
	 * @param referer - the page that called the popup
	 * @return the resource literals popup box
	 * @throws Exception the exception
	 */
	@RequestMapping(value="/nodeinfo/{resource}/{context}", method = RequestMethod.GET)
	public String resourceLiteralsInContext(@PathVariable(value="resource") String resourceUri, 
			@PathVariable(value="context") String contextUri, Model model, 
			@RequestParam(value="viewmode", required=false) String view,
			@RequestHeader("referer") String referer) throws Exception {
		if (view==null || view.length()==0){
			view=STANDARD_VIEW;
		}
		
		try {
			ResourceDescription resourceLiterals = dataDisplayService.getResourceLiteralsInContext(resourceUri, contextUri);

			boolean isRMapType = resourceLiterals.getResourceTypes().containsKey(Terms.RMAP_DISCO_PATH)
									|| resourceLiterals.getResourceTypes().containsKey(Terms.RMAP_AGENT_PATH)
									|| resourceLiterals.getResourceTypes().containsKey(Terms.RMAP_EVENT_PATH);
			
			model.addAttribute("ISRMAPTYPE",isRMapType);	  
			model.addAttribute("RESDES",resourceLiterals);	
		    model.addAttribute("VIEWMODE", view);	 
		    model.addAttribute("REFERER",referer);
		} catch (Exception e) {
			//we need to mark this as a nodeinfo error - it needs to load a smaller custom exception page
			throw new RMapWebException(e,ErrorCode.ER_PROBLEM_LOADING_NODEINFO);
		}  
	    
		return "nodeinfo";
	}	
	
	/**
	 * Experiment for proof of concept - this doesn't allow you to save the DiSCOs you make.
	 *
	 * @param model the Spring model
	 * @return the new disco page
	 * @throws Exception the exception
	 */
	@RequestMapping(value="/discos/new", method = RequestMethod.GET)
	public String disconew(Model model) throws Exception {
	    model.addAttribute("NEWDISCO",true);	        
		return "disconew";
	}		
	
	
	
}
