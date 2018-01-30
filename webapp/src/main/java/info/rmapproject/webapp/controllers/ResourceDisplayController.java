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
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

import org.jsoup.Jsoup;
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
import info.rmapproject.core.model.request.RMapSearchParams;
import info.rmapproject.core.model.request.RMapSearchParamsFactory;
import info.rmapproject.core.model.request.RMapStatusFilter;
import info.rmapproject.core.model.request.ResultBatch;
import info.rmapproject.webapp.domain.Graph;
import info.rmapproject.webapp.domain.PageStatus;
import info.rmapproject.webapp.domain.PaginatorType;
import info.rmapproject.webapp.domain.ResourceDescription;
import info.rmapproject.webapp.exception.ErrorCode;
import info.rmapproject.webapp.exception.RMapWebException;
import info.rmapproject.webapp.service.DataDisplayService;

/**
 * Handles requests for the Resource data visualization pages.
 * @author khanson
 */

@Controller
@SessionAttributes({"user","account"})
public class ResourceDisplayController {

	/** The log. */
	private static final Logger LOG = LoggerFactory.getLogger(ResourceDisplayController.class);
	
	/**  term for standard view, used in VIEWMODE. */
	private static final String STANDARD_VIEW = "standard";
	
	/** Service for managing RMap data display. */
	private DataDisplayService dataDisplayService;
	
	/**used to get instances of RMapSearchParams which passes search properties to rmap**/
	private RMapSearchParamsFactory paramsFactory;
	
	@Autowired
	public ResourceDisplayController(DataDisplayService dataDisplayService, RMapSearchParamsFactory paramsFactory) {
		this.dataDisplayService = dataDisplayService;
		this.paramsFactory = paramsFactory;
	}
	
	
	/**
	 * GET details of a resource.
	 *
	 * @param sResourceUri the resource uri
	 * @param resview this is for when a URI is passed in that may be an RMap object URI (Agent, DiSCO, Event).
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
				@RequestParam(value="status", required=false) String status,
				Model model, RedirectAttributes redirectAttributes) throws Exception {
		
		LOG.info("Resource requested {}", sResourceUri);
		
		if (resview == null) {resview = 0;}
		sResourceUri = URLDecoder.decode(sResourceUri, "UTF-8");
	
		if (resview==0) {
			String rmapType = dataDisplayService.getRMapTypeDisplayName(new URI(sResourceUri));
			LOG.debug("rmapType identified as {}", rmapType);
			if (rmapType.length()>0){
				String redirectPath = "redirect:/" + rmapType.toLowerCase() + "s/" + URLEncoder.encode(sResourceUri, "UTF-8");
				LOG.debug("Redirecting resource path to {}", redirectPath);
				return redirectPath;
			}				
		}
		RMapSearchParams params = generateSearchParams(status, 0);
		
		List<URI> resourceTypes = dataDisplayService.getResourceRDFTypes(new URI(sResourceUri),params);
		String resourceLabel = dataDisplayService.getResourceLabel(sResourceUri,params);
		if (resourceLabel!=null){
			resourceLabel = Jsoup.parse(resourceLabel).text();
		}
	    model.addAttribute("RESOURCELABEL", resourceLabel); 
	    model.addAttribute("RESOURCEURI", sResourceUri); 
	    model.addAttribute("RESOURCE_TYPES", resourceTypes);
	    model.addAttribute("PAGEPATH", "resources");
	 	    
		return "resources";
	}
	
	
	/**
	 * Some platforms (e.g. PowerPoint) do not like that we have encoded URIs embedded as a REST parameter. This is a back door to 
	 * accessing a webpage by defining the resource URI as a request param
	 *
	 * @param sResourceUri the resource uri
	 * @param resview this is for when a URI is passed in that may be an RMap object URI (Agent, DiSCO, Event).
	 * 					When resview==0, it will check for an RMap type, and where one is found the appropriate 
	 * 					RMap object page will be displayed instead of the generic resources page. When resview==1, 
	 * 					the /resources page will be displayed by default. Default is 0
	 * @return redirect to the appropriate Resource path
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
	 * @param resview this is for when a URI is passed in that may be an RMap object URI (Agent, DiSCO, Event).
	 * 					When resview==0, it will check for an RMap type, and where one is found the appropriate 
	 * 					RMap object page will be displayed instead of the generic resources page. When resview==1, 
	 * 					the /resources page will be displayed by default.
	 * @param offset starting position within matching triple records from which to start.  Default is 0.
	 * @param model the Spring model
	 * @return the resources page
	 * @throws Exception the exception
	 */
	@RequestMapping(value="/resources/{uri}/visual", method = RequestMethod.GET)
	public String resourceVisualView(@PathVariable(value="uri") String reqUri, 
				@RequestParam(value="resview", required=false) Integer resview, 
				Model model) throws Exception {
		LOG.info("Resource requested {}", reqUri);
		
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
				
	    model.addAttribute("RESOURCEURI", reqUri);

	    return "resourcevisual";
	}
	
	/**
	 * GET details of a resource and return data relevant to RMap widget
	 *
	 * @param reqUri the resource uri
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
				@RequestParam(value="offset", required=false) Integer offset,
				@RequestParam(value="status", required=false) String status,
				Model model) throws Exception {
		LOG.info("Resource requested {}", reqUri);

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

		model.addAttribute("RESOURCEURI", decodedUri);
		model.addAttribute("status", status);
				
		RMapSearchParams params = generateSearchParams(status, offset);
		ResultBatch<RMapTriple> triplebatch = dataDisplayService.getResourceBatch(decodedUri, params, PaginatorType.RESOURCE_GRAPH);
		if (triplebatch==null || triplebatch.size()==0) {
			return "resourcenotfoundembedded";
		}
		Graph resourceGraph = dataDisplayService.getResourceGraph(triplebatch,params);
		model.addAttribute("GRAPH", resourceGraph);
	    
	    return "resourcewidget";
	}
	

	/**
	 * GET table data for resource
	 *
	 * @param reqUri the resource URI
	 * @param offset the start position for the table data (when paginating). Default is 0.
	 * @param model the Spring model
	 * @return the resources page
	 * @throws Exception the exception
	 */
	@RequestMapping(value="/resources/{uri}/tabledata", method = RequestMethod.GET)
	public String resourceTableView(@PathVariable(value="uri") String reqUri, 
				@RequestParam(value="offset", required=false) Integer offset,
				@RequestParam(value="status", required=false) String status,
				Model model) throws Exception {
		LOG.info("Resource requested {}", reqUri);
		String decodedUri = URLDecoder.decode(reqUri, "UTF-8");
		try {

			model.addAttribute("RESOURCEURI", decodedUri);
			model.addAttribute("status", status);
			
			RMapSearchParams params = generateSearchParams(status, offset);
			ResultBatch<RMapTriple> triplebatch = dataDisplayService.getResourceBatch(decodedUri, params, PaginatorType.RESOURCE_TABLE);
			if (triplebatch==null || triplebatch.size()==0) {
				return "resourcenotfoundembedded";
			}
			params.setOffset(0); //
			ResourceDescription rd = dataDisplayService.getResourceTableData(decodedUri, triplebatch, params, true);
			PageStatus pageStatus = dataDisplayService.getPageStatus(triplebatch, PaginatorType.RESOURCE_TABLE);
	
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
	 * @param reqUri the resource URI
	 * @param offset the start position for the graph data (when paginating). Default is 0.
	 * @param view - determines the kind of view that will be returned - widget or visualize
	 * @param model the Spring model
	 * @return the resources page
	 * @throws Exception the exception
	 */
	@RequestMapping(value="/resources/{uri}/graphdata", method = RequestMethod.GET)
	public String resourceGraphData(@PathVariable(value="uri") String reqUri, 
				@RequestParam(value="offset", required=false) Integer offset,
				@RequestParam(value="status", required=false) String status,
				@RequestParam(value="view", required=false) String view,
				Model model) throws Exception {
		LOG.info("Resource requested {}", reqUri);
		if (view==null || view.length()==0){
			view = STANDARD_VIEW;
		}
		String decodedUri = URLDecoder.decode(reqUri, "UTF-8");
		try {
			model.addAttribute("RESOURCEURI", decodedUri);
			model.addAttribute("status", status);
			RMapSearchParams params = generateSearchParams(status,offset);
			
			ResultBatch<RMapTriple> triplebatch = dataDisplayService.getResourceBatch(decodedUri, params, PaginatorType.RESOURCE_GRAPH);
			if (triplebatch==null || triplebatch.size()==0) {
				return "resourcenotfoundembedded";
			}
			
			Graph resourceGraph = dataDisplayService.getResourceGraph(triplebatch, params);
		    PageStatus pageStatus = dataDisplayService.getPageStatus(triplebatch, PaginatorType.RESOURCE_GRAPH);
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
	 * @param reqUri the resource URI
	 * @param offset the start position for the list of URIs (when paginating). Default is 0.
	 * @param model the Spring model
	 * @return the resources page
	 * @throws Exception the exception
	 */
	@RequestMapping(value="/resources/{uri}/discos", method = RequestMethod.GET)
	public String resourceRelatedDiscos(@PathVariable(value="uri") String reqUri, 
				@RequestParam(value="offset", required=false) Integer offset,
				@RequestParam(value="status", required=false) String status,
				Model model) throws Exception {
		LOG.info("Resource requested {}", reqUri);
		try {
			String decodedUri = URLDecoder.decode(reqUri, "UTF-8");
			
			RMapSearchParams params = generateSearchParams(status, offset);

			ResultBatch<URI> resourceDiscos = dataDisplayService.getResourceRelatedDiSCOs(decodedUri, params);
		    PageStatus resDiscoPageStatus = dataDisplayService.getPageStatus(resourceDiscos, PaginatorType.RESOURCE_DISCOS);
	
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
	 * @param resourceUri a resource URI
	 * @param offset the start position for the node data (when paginating). Default is 0.
	 * @param view the current page view (visual, widget or standard)
	 * @param model the Spring model
	 * @param referer - the page that called the popup
	 * @return the resource literals popup box
	 * @throws Exception the exception
	 */
	@RequestMapping(value="/resources/{resource}/nodeinfo", method = RequestMethod.GET)
	public String resourceLiterals(@PathVariable(value="resource") String resourceUri, 
				@RequestParam(value="offset", required=false) Integer offset,
				@RequestParam(value="status", required=false) String status,
				@RequestParam(value="view", required=false) String view, Model model,
				@RequestParam(value="referer", required=false) String referer) throws Exception {
		if (offset==null){offset=0;}
		if (view==null || view.length()==0){
			view=STANDARD_VIEW;
		}
		try {
			resourceUri = URLDecoder.decode(resourceUri, "UTF-8");
			
			RMapSearchParams params = generateSearchParams(status,offset);
			ResultBatch<RMapTriple> triplebatch = dataDisplayService.getResourceLiterals(resourceUri, params);
			ResourceDescription resourceDescription = dataDisplayService.getResourceTableData(resourceUri, triplebatch, params, true);
			PageStatus pageStatus = dataDisplayService.getPageStatus(triplebatch, PaginatorType.NODE_INFO);
			String rmapType = dataDisplayService.getRMapTypeDisplayName(new URI(resourceUri));
			
			model.addAttribute("RESDES",resourceDescription);	
		    model.addAttribute("VIEWMODE", view);	 
		    model.addAttribute("REFERER",referer);
		    model.addAttribute("RMAPTYPE",rmapType);
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
	 * @param resourceUri a resource URI
	 * @param contextUri a context URI - URI of graph to limit results by
	 * @param model the Spring model
	 * @param view the current page view (visual, widget or standard)
	 * @param offset the start position for the node data (when paginating). Default is 0.
	 * @param referer - the page that called the popup
	 * @return the resource literals popup box
	 * @throws Exception the exception
	 */
	@RequestMapping(value="/resources/{resource}/nodeinfo/{context}", method = RequestMethod.GET)
	public String resourceLiteralsInContext(@PathVariable(value="resource") String resourceUri, 
			@PathVariable(value="context") String contextUri, Model model, 
			@RequestParam(value="view", required=false) String view,
			@RequestParam(value="offset", required=false) Integer offset,
			@RequestHeader("referer") String referer) throws Exception {
		if (offset==null){offset=0;}
		if (view==null || view.length()==0){
			view=STANDARD_VIEW;
		}
		
		try {
			resourceUri = URLDecoder.decode(resourceUri, "UTF-8");
			contextUri = URLDecoder.decode(contextUri, "UTF-8");

			RMapSearchParams params = generateSearchParams(RMapStatusFilter.ALL, offset);
			
			ResultBatch<RMapTriple> triplebatch = dataDisplayService.getResourceLiteralsInContext(resourceUri, contextUri, params);
			ResourceDescription resourceDescription = dataDisplayService.getResourceTableDataInContext(resourceUri, triplebatch, contextUri, false);
			PageStatus pageStatus = dataDisplayService.getPageStatus(triplebatch, PaginatorType.NODE_INFO);
			String rmapType = dataDisplayService.getRMapTypeDisplayName(new URI(resourceUri));
			
			model.addAttribute("RESDES",resourceDescription);	
		    model.addAttribute("VIEWMODE", view);	 
		    model.addAttribute("REFERER",referer);
		    model.addAttribute("RMAPTYPE",rmapType);
		    model.addAttribute("PAGINATOR", pageStatus);
		    
		} catch (Exception e) {
			//we need to mark this as a nodeinfo error - it needs to load a smaller custom exception page
			throw new RMapWebException(e,ErrorCode.ER_PROBLEM_LOADING_NODEINFO);
		}  
	    
		return "nodeinfo";
	}		
	
	private RMapSearchParams generateSearchParams(String status, Integer offset) {
		RMapStatusFilter statusFilter = RMapStatusFilter.getStatusFromTerm(status);
		return generateSearchParams(statusFilter,offset);
	}
	
	private RMapSearchParams generateSearchParams(RMapStatusFilter statusFilter, Integer offset) {
		RMapSearchParams params = paramsFactory.newInstance();
		statusFilter = (statusFilter==null) ? RMapStatusFilter.ACTIVE : statusFilter;	
		offset = (offset==null) ?  0 : offset;
		params.setStatusCode(statusFilter);
		params.setOffset(offset);
		return params;
	}
	
	
}
