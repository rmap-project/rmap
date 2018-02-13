/*******************************************************************************
 * Copyright 2018 Johns Hopkins University
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
import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import info.rmapproject.core.model.request.RMapSearchParams;
import info.rmapproject.core.model.request.RMapSearchParamsFactory;
import info.rmapproject.core.model.request.RMapStatusFilter;
import info.rmapproject.core.model.request.ResultBatch;
import info.rmapproject.webapp.domain.Graph;
import info.rmapproject.webapp.domain.PageStatus;
import info.rmapproject.webapp.domain.PaginatorType;
import info.rmapproject.webapp.exception.ErrorCode;
import info.rmapproject.webapp.exception.RMapWebException;
import info.rmapproject.webapp.service.DataDisplayService;
import info.rmapproject.webapp.service.dto.AgentDTO;

/**
 * Handles requests for the Agent data visualization pages.
 * @author khanson
 */

@Controller
@SessionAttributes({"user","account"})
public class AgentDisplayController {

	/** The log. */
	private static final Logger LOG = LoggerFactory.getLogger(AgentDisplayController.class);

	/** Service for managing RMap data display. */
	private DataDisplayService dataDisplayService;
		
	/**used to get instances of RMapSearchParams which passes search properties to rmap**/
	private RMapSearchParamsFactory paramsFactory;
	
	/**  term for standard view, used in VIEWMODE. */
	private static final String STANDARD_VIEW = "standard";

	@Autowired
	public AgentDisplayController(DataDisplayService dataDisplayService, RMapSearchParamsFactory paramsFactory) {
		this.dataDisplayService = dataDisplayService;
		this.paramsFactory = paramsFactory;
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
		LOG.info("Agent requested: {}", agentUri);	
		AgentDTO agentDTO = dataDisplayService.getAgentDTO(agentUri);
	    model.addAttribute("AGENT", agentDTO);	    
	    model.addAttribute("RESOURCEURI", agentDTO.getUri());
	    
		return "agents";
	}	
	

	/**
	 * Some platforms (e.g. PowerPoint) do not like that we have encoded URIs embedded as a REST parameter. This is a back door to 
	 * accessing a webpage by defining the Agent URI as a request param
	 *
	 * @param sAgentUri the Agent URI
	 * @return the resources page or redirect back to search on error
	 * @throws Exception the exception
	 */
	@RequestMapping(value="/agents", method = RequestMethod.GET)
	public String resourceByReqParam(@RequestParam(value="uri") String sAgentUri) throws Exception {
		return "redirect:/agents/" + URLEncoder.encode(sAgentUri, "UTF-8");
	}
	
	
	/**
	 * GET details of a Agent for the visual view.
	 *
	 * @param agentUri the agent uri
	 * @param model the Spring model
	 * @return the agents visual page
	 * @throws Exception the exception
	 */
	@RequestMapping(value="/agents/{uri}/visual", method = RequestMethod.GET)
	public String agentDataVisual(@PathVariable(value="uri") String agentUri, Model model) throws Exception {
		LOG.info("Agent requested: {}", agentUri);	

		AgentDTO agentDTO = dataDisplayService.getAgentDTO(agentUri);
	    model.addAttribute("AGENT", agentDTO);	    
	    model.addAttribute("RESOURCEURI", agentDTO.getUri());
	    
    	return "agentvisual";

	}

	/**
	 * GET details of a Agent for the widget view.
	 *
	 * @param agentUri the agent uri
	 * @param model the Spring model
	 * @return the agents widget page
	 * @throws Exception the exception
	 */
	@RequestMapping(value="/agents/{uri}/widget", method = RequestMethod.GET)
	public String agentDataWidget(@PathVariable(value="uri") String agentUri, Model model) throws Exception {
		LOG.info("Agent requested: {}", agentUri);	

		AgentDTO agentDTO = dataDisplayService.getAgentDTO(agentUri);
	    model.addAttribute("AGENT", agentDTO);	    
	    model.addAttribute("RESOURCEURI", agentDTO.getUri());
	    
	    return "agentwidget";
	}
	
	/**
	 * GET details of a Agent for the tabular view.
	 *
	 * @param agentUri the agent uri
	 * @param model the Spring model
	 * @return the Agent table data page
	 * @throws Exception the exception
	 */
	@RequestMapping(value="/agents/{uri}/tabledata", method = RequestMethod.GET)
	public String agentTableData(@PathVariable(value="uri") String agentUri, Model model) throws Exception {
		LOG.info("Agent requested: {}", agentUri);	

		try {
			AgentDTO agentDTO = dataDisplayService.getAgentDTO(agentUri);
		    model.addAttribute("AGENT", agentDTO);	    
		    model.addAttribute("RESOURCEURI", agentDTO.getUri());
		} catch (Exception e){
			throw new RMapWebException(e, ErrorCode.ER_PROBLEM_LOADING_AGENTTABLE);
		}
    	return "agenttable";
	}
	

	/**
	 * GET the graph data for the Agent
	 *
	 * @param agentUri the agent uri
	 * @param view the current page view (visual, widget or standard)
	 * @param model the Spring model
	 * @return the Agent graph data 
	 * @throws Exception The exception
	 */
	@RequestMapping(value="/agents/{uri}/graphdata", method = RequestMethod.GET)
	public String agentGraphData(@PathVariable(value="uri") String agentUri, 
			@RequestParam(value="view", required=false) String view, Model model) throws Exception {
		LOG.info("Agent requested: {}", agentUri);	
		if (view==null || view.length()==0){
			view=STANDARD_VIEW;
		}
		try {
			AgentDTO agentDTO = dataDisplayService.getAgentDTO(agentUri);
			Graph agentGraph = dataDisplayService.getAgentGraph(agentDTO);
	
		    model.addAttribute("GRAPH", agentGraph);
		    model.addAttribute("RESOURCEURI", agentDTO.getUri());
		    model.addAttribute("VIEWMODE", view);
		} catch (Exception e){
			throw new RMapWebException(e, ErrorCode.ER_PROBLEM_LOADING_AGENTGRAPH);
		}
    	return "agentgraph";

	}

	/**
	 * GET the list of DiSCOs created by the Agent
	 * 
	 * @param agentUri the Agent URI
	 * @param model the Spring model
	 * @param offset the record offset for the URI list
	 * @return the Agent DiSCO list page
	 * @throws Exception
	 */
	@RequestMapping(value="/agents/{uri}/discos", method = RequestMethod.GET)
	public String agentRelatedDiSCOs(@PathVariable(value="uri") String agentUri, Model model,
			@RequestParam(value="offset", required=false) Integer offset,
			@RequestParam(value="status", required=false) String status) throws Exception {
		LOG.info("Agent requested: {}", agentUri);	
		if (offset==null){
			offset=0;
		}
		try {
			RMapStatusFilter statusFilter = RMapStatusFilter.getStatusFromTerm(status);
			statusFilter = (statusFilter==null) ? RMapStatusFilter.ACTIVE : statusFilter;	
			RMapSearchParams params = paramsFactory.newInstance();
			params.setStatusCode(statusFilter);
			params.setOffset(offset);
			
			ResultBatch<URI> agentDiSCOs = dataDisplayService.getAgentDiSCOs(agentUri, params);
			PageStatus pageStatus = dataDisplayService.getPageStatus(agentDiSCOs, PaginatorType.AGENT_DISCOS);
		    model.addAttribute("PAGINATOR", pageStatus);
		    model.addAttribute("AGENT_DISCOS", agentDiSCOs.getResultList());
		} catch (Exception e){
			throw new RMapWebException(e, ErrorCode.ER_PROBLEM_LOADING_AGENTDISCOS);
		}
	    return "agentdiscos";    
	}
}
