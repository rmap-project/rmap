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
package info.rmapproject.webapp.controllers;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

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

import info.rmapproject.core.model.RMapTriple;
import info.rmapproject.core.model.request.ResultBatch;
import info.rmapproject.webapp.domain.Graph;
import info.rmapproject.webapp.domain.PageStatus;
import info.rmapproject.webapp.domain.ResourceDescription;
import info.rmapproject.webapp.domain.SearchCommand;
import info.rmapproject.webapp.exception.ErrorCode;
import info.rmapproject.webapp.exception.RMapWebException;
import info.rmapproject.webapp.service.DataDisplayService;

/**
 * Handles requests for the resource data visualization pages.
 *
 * @author khanson
 */

@Controller
@SessionAttributes({"user","account"})
public class ResourceDisplayController {

	/** Service for managing RMap data display. */
	@Autowired
	private DataDisplayService dataDisplayService;

	/** The log. */
	private static final Logger log = LoggerFactory.getLogger(ResourceDisplayController.class);
	
	/**  term for standard view, used in VIEWMODE. */
	private static final String STANDARD_VIEW = "standard";
	
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
		
		if (resview == null) {resview = 0;}
		sResourceUri = URLDecoder.decode(sResourceUri, "UTF-8");
		
		try {
			if (resview==0) {
				String rmapType = dataDisplayService.getRMapTypeDisplayName(new URI(sResourceUri));
				if (rmapType.length()>0){
					return "redirect:/" + rmapType.toLowerCase() + "s/" + URLEncoder.encode(sResourceUri, "UTF-8");
				}				
			}
			
			//do this to trigger not found error
			dataDisplayService.getResourceBatch(sResourceUri, 0, "graph");
			
			List<URI> resourceTypes = dataDisplayService.getResourceRDFTypes(new URI(sResourceUri));
		    
		    model.addAttribute("RESOURCEURI", sResourceUri); 
		    model.addAttribute("RESOURCE_TYPES", resourceTypes);
		    model.addAttribute("PAGEPATH", "resources");
		    
		} catch (URISyntaxException|IllegalArgumentException ex){
			log.warn(ex.getMessage() + ". Submitted value: " + sResourceUri + ".");
			SearchCommand search = new SearchCommand();
			search.setSearch(sResourceUri);
			redirectAttributes.addFlashAttribute("search", search);
			redirectAttributes.addFlashAttribute("notice", "<strong>" + sResourceUri + "</strong> is not a valid URI. Currently only URI searches are supported.");	
			return "redirect:/search";
		}
	    
		return "resources";
	}
	
	
	/**
	 * Some platforms (e.g. PowerPoint) do not like that we have encoded URIs embedded as a REST parameter. This is a back door to 
	 * accessing a webpage by defining the resource URI as a request param
	 *
	 * @param sResourceUri the resource uri
	 * @return the resources page or redirect back to search on error
	 * @throws Exception the exception
	 */
	@RequestMapping(value="/resources", method = RequestMethod.GET)
	public String resourceByReqParam(@RequestParam(value="uri") String sResourceUri,
			@RequestParam(value="resview", required=false) Integer resview) throws Exception {
		if(resview==null){resview=0;}
		String resviewParam = "";
		if (resview==1) {resviewParam = "?resview=1";}
		return "redirect:/resources/" + URLEncoder.encode(sResourceUri, "UTF-8") + resviewParam;
	}
	
	
	/**
	 * GET details of a resource and return the visual view (full page visualization)
	 *
	 * @param resourceUri the resource uri
	 * @param resview - This is for when a URI is passed in that may be an RMap object URI (Agent, DiSCO, Event).
	 * 					When resview==0, it will check for an RMap type, and where one is found the appropriate 
	 * 					RMap object page will be displayed instead of the generic resources page. When resview==1, 
	 * 					the /resources page will be displayed by default.
	 * @param model the Spring model
	 * @return the resources page
	 * @throws Exception the exception
	 */
	@RequestMapping(value="/resources/{uri}/visual", method = RequestMethod.GET)
	public String resourceVisualView(@PathVariable(value="uri") String reqUri, 
				@RequestParam(value="resview", required=false) Integer resview, 
				@RequestParam(value="offset", required=false) String sOffset,
				Model model) throws Exception {
		log.info("Resource requested " + reqUri);
		
		String decodedUri = URLDecoder.decode(reqUri, "UTF-8");
	
		//by default the system will redirect to the object type 
		//e.g. a disco uri will redirect to DiSCO view.
		if (resview == null) {resview = 0;}
		if (resview==0) {
			String rmapType = dataDisplayService.getRMapTypeDisplayName(new URI(decodedUri));
			if (rmapType.length()>0){
				return "redirect:/" + rmapType.toLowerCase() + "s/" + reqUri + "/visual";
			}
		}  
		//do this to trigger not found error
		dataDisplayService.getResourceBatch(reqUri, 0, "graph");
				
	    model.addAttribute("RESOURCEURI", reqUri);

	    return "resourcevisual";
	}
	
	/**
	 * GET details of a resource and return data relevant to RMap widget
	 *
	 * @param resourceUri the resource uri
	 * @param resview - This is for when a URI is passed in that may be an RMap object URI (Agent, DiSCO, Event).
	 * 					When resview==0, it will check for an RMap type, and where one is found the appropriate 
	 * 					RMap object page will be displayed instead of the generic resources page. When resview==1, 
	 * 					the /resources page will be displayed by default.
	 * @param model the Spring model
	 * @return the resources widget page
	 * @throws Exception the exception
	 */
	@RequestMapping(value="/resources/{uri}/widget", method = RequestMethod.GET)
	public String resourceWidgetView(@PathVariable(value="uri") String reqUri, 
				@RequestParam(value="resview", required=false) Integer resview, 
				Model model) throws Exception {
		log.info("Resource requested " + reqUri);

		String decodedUri = URLDecoder.decode(reqUri, "UTF-8");
		
		//by default the system will redirect to the object type 
		//e.g. a disco uri will redirect to DiSCO view.
		if (resview == null) {resview = 0;}
		if (resview==0) {
			String rmapType = dataDisplayService.getRMapTypeDisplayName(new URI(decodedUri));
			if (rmapType.length()>0){
				return "redirect:/" + rmapType.toLowerCase() + "s/" + reqUri + "/widget";
			}
		}    	

		ResultBatch<RMapTriple> triplebatch = dataDisplayService.getResourceBatch(decodedUri, 0, "graph");
		Graph resourceGraph = dataDisplayService.getResourceGraph(triplebatch);
		
		model.addAttribute("RESOURCEURI", decodedUri);
		model.addAttribute("GRAPH", resourceGraph);
	    
	    return "resourcewidget";
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
	@RequestMapping(value="/resources/{uri}/tabledata", method = RequestMethod.GET)
	public String resourceTableView(@PathVariable(value="uri") String reqUri, 
				@RequestParam(value="offset", required=false) String sOffset,
				Model model) throws Exception {
		log.info("Resource requested " + reqUri);
		String decodedUri = URLDecoder.decode(reqUri, "UTF-8");
		Integer offset;
		try {
			offset = Integer.parseInt(sOffset);
		} catch (NumberFormatException e){
			offset = 0;
		}
		try {
			ResultBatch<RMapTriple> triplebatch = dataDisplayService.getResourceBatch(decodedUri, offset, "table");
			ResourceDescription rd = dataDisplayService.getResourceTableData(decodedUri, triplebatch);
			PageStatus pageStatus = dataDisplayService.getPageStatus(triplebatch, "resource_table");
	
			model.addAttribute("RESOURCEURI", decodedUri);
			model.addAttribute("TABLEDATA", rd);
		    model.addAttribute("PAGINATOR", pageStatus);
		} catch (Exception e){
			throw new RMapWebException(e,ErrorCode.ER_PROBLEM_LOADING_RESOURCETABLE);
		}
	    return "resourcetable";
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
	@RequestMapping(value="/resources/{uri}/graphdata", method = RequestMethod.GET)
	public String resourceGraphData(@PathVariable(value="uri") String reqUri, 
				@RequestParam(value="offset", required=false) String sOffset,
				@RequestParam(value="view", required=false) String view,
				Model model) throws Exception {
		log.info("Resource requested " + reqUri);
		if (view==null || view.length()==0){
			view = STANDARD_VIEW;
		}
		String decodedUri = URLDecoder.decode(reqUri, "UTF-8");
		Integer offset;
		try {
			offset = Integer.parseInt(sOffset);
		} catch (NumberFormatException e){
			offset = 0;
		}
		try {
			ResultBatch<RMapTriple> triplebatch = dataDisplayService.getResourceBatch(decodedUri, offset, "graph");
			Graph resourceGraph = dataDisplayService.getResourceGraph(triplebatch);
		    PageStatus pageStatus = dataDisplayService.getPageStatus(triplebatch, "resource_graph");
			model.addAttribute("RESOURCEURI", decodedUri);
		    model.addAttribute("GRAPH", resourceGraph);
		    model.addAttribute("PAGINATOR", pageStatus);
		    model.addAttribute("VIEWMODE", view);		    
		} catch (Exception e){
			throw new RMapWebException(e,ErrorCode.ER_PROBLEM_LOADING_RESOURCEGRAPH);
		}
	    return "resourcegraph";
	}

	/**
	 * GET details of a resource's related DiSCOs
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
	@RequestMapping(value="/resources/{uri}/discos", method = RequestMethod.GET)
	public String resourceRelatedDiscos(@PathVariable(value="uri") String reqUri, 
				@RequestParam(value="offset", required=false) String sOffset,
				Model model) throws Exception {
		log.info("Resource requested " + reqUri);
		try {
			String decodedUri = URLDecoder.decode(reqUri, "UTF-8");
			Integer offset;
			try {
				offset = Integer.parseInt(sOffset);
			} catch (NumberFormatException e){
				offset = 0;
			}
			ResultBatch<URI> resourceDiscos = dataDisplayService.getResourceRelatedDiSCOs(decodedUri, offset);
		    PageStatus resDiscoPageStatus = dataDisplayService.getPageStatus(resourceDiscos, "resource_discos");
	
		    model.addAttribute("RESOURCEURI", decodedUri);
		    model.addAttribute("URILIST", resourceDiscos.getResultList());
		    model.addAttribute("PAGINATOR", resDiscoPageStatus);
	
		} catch (Exception e) {
			throw new RMapWebException(e,ErrorCode.ER_PROBLEM_LOADING_RESOURCEDISCOS);
		}
    
	    
	    return "resourcediscos";
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
	@RequestMapping(value="/resources/{resource}/nodeinfo", method = RequestMethod.GET)
	public String resourceLiterals(@PathVariable(value="resource") String resourceUri, 
				@RequestParam(value="offset", required=false) Integer offset,
				@RequestParam(value="view", required=false) String view, Model model,
				@RequestParam(value="referer", required=false) String referer) throws Exception {
		if (offset==null){offset=0;}
		if (view==null || view.length()==0){
			view=STANDARD_VIEW;
		}
		try {
			resourceUri = URLDecoder.decode(resourceUri, "UTF-8");
						
			ResultBatch<RMapTriple> triplebatch = dataDisplayService.getResourceLiterals(resourceUri, offset);
			ResourceDescription resourceDescription = dataDisplayService.getResourceTableData(resourceUri, triplebatch);
			PageStatus pageStatus = dataDisplayService.getPageStatus(triplebatch, "node_info");
			
			model.addAttribute("RESDES",resourceDescription);	
		    model.addAttribute("VIEWMODE", view);	 
		    model.addAttribute("REFERER",referer);
		    model.addAttribute("PAGINATOR", pageStatus);
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
	@RequestMapping(value="/resources/{resource}/nodeinfo/{context}", method = RequestMethod.GET)
	public String resourceLiteralsInContext(@PathVariable(value="resource") String resourceUri, 
			@PathVariable(value="context") String contextUri, Model model, 
			@RequestParam(value="viewmode", required=false) String view,
			@RequestParam(value="offset", required=false) Integer offset,
			@RequestHeader("referer") String referer) throws Exception {
		if (offset==null){offset=0;}
		if (view==null || view.length()==0){
			view=STANDARD_VIEW;
		}
		
		try {
			resourceUri = URLDecoder.decode(resourceUri, "UTF-8");
			contextUri = URLDecoder.decode(contextUri, "UTF-8");
			
			ResultBatch<RMapTriple> triplebatch = dataDisplayService.getResourceLiteralsInContext(resourceUri, contextUri, offset);
			ResourceDescription resourceDescription = dataDisplayService.getResourceTableData(resourceUri, triplebatch);
			PageStatus pageStatus = dataDisplayService.getPageStatus(triplebatch, "node_info");
			
			model.addAttribute("RESDES",resourceDescription);	
		    model.addAttribute("VIEWMODE", view);	 
		    model.addAttribute("REFERER",referer);
		    model.addAttribute("PAGINATOR", pageStatus);
		    
		} catch (Exception e) {
			//we need to mark this as a nodeinfo error - it needs to load a smaller custom exception page
			throw new RMapWebException(e,ErrorCode.ER_PROBLEM_LOADING_NODEINFO);
		}  
	    
		return "nodeinfo";
	}	
	
	
}
