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

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

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

import info.rmapproject.webapp.domain.Graph;
import info.rmapproject.webapp.domain.PageStatus;
import info.rmapproject.webapp.domain.ResourceDescription;
import info.rmapproject.webapp.exception.ErrorCode;
import info.rmapproject.webapp.exception.RMapWebException;
import info.rmapproject.webapp.service.DataDisplayService;
import info.rmapproject.webapp.service.dto.DiSCODTO;

/**
 * Handles requests for the DiSCO data visualization pages.
 * @author khanson
 */

@Controller
@SessionAttributes({"user","account"})
public class DiSCODataController {

	/** Service for managing RMap data display. */
	private DataDisplayService dataDisplayService;

	/** The log. */
	private static final Logger LOG = LoggerFactory.getLogger(DiSCODataController.class);

	/**  term for standard view, used in VIEWMODE. */
	private static final String STANDARD_VIEW = "standard";
	

	@Autowired
	public DiSCODataController(DataDisplayService dataDisplayService) {
		this.dataDisplayService = dataDisplayService;
	}
	
	/**
	 * GET details of a DiSCO.
	 *
	 * @param discoUri the DiSCO uri	 
	 * @param model the Spring model
	 * @return the DiSCO summary page
	 * @throws Exception the exception
	 */
	@RequestMapping(value="/discos/{uri}", method = RequestMethod.GET)
	public String disco(@PathVariable(value="uri") String discoUri, Model model) throws Exception {
		LOG.info("DiSCO requested: {}", discoUri);

		discoUri = URLDecoder.decode(discoUri, "UTF-8");

		DiSCODTO discoDTO = dataDisplayService.getDiSCODTO(discoUri);
	    
		model.addAttribute("DISCO",discoDTO);	    
	    model.addAttribute("RESOURCEURI", discoDTO.getUri());
	    model.addAttribute("PAGEPATH", "discos");

		return "discos";
	}	
	
	/**
	 * Some platforms (e.g. PowerPoint) do not like that we have encoded URIs embedded as a REST parameter. This is a back door to 
	 * accessing a webpage by defining the DiSCO URI as a request param
	 *
	 * @param sDiSCOUri the DiSCO URI
	 * @return redirect to the appropriate DiSCO path
	 * @throws Exception the exception
	 */
	@RequestMapping(value="/discos", method = RequestMethod.GET)
	public String resourceByReqParam(@RequestParam(value="uri") String sDiSCOUri) throws Exception {
		return "redirect:/discos/" + URLEncoder.encode(sDiSCOUri, "UTF-8");
	}
	
	/**
	 * GET the DiSCO visual page format
	 *
	 * @param discoUri the DiSCO URI
	 * @param model the Spring model
	 * @return the DiSCO visual page
	 * @throws Exception the exception
	 */
	@RequestMapping(value="/discos/{uri}/visual", method = RequestMethod.GET)
	public String discoGraphVisual(@PathVariable(value="uri") String discoUri,  Model model) throws Exception {
		LOG.info("DiSCO visualization requested: {}", discoUri);

		discoUri = URLDecoder.decode(discoUri, "UTF-8");
		
		DiSCODTO discoDTO = dataDisplayService.getDiSCODTO(discoUri);
	    
		model.addAttribute("DISCO", discoDTO);	
	    model.addAttribute("RESOURCEURI", discoDTO.getUri());
	    
    	return "discovisual";
	}

	/**
	 * GET the DiSCO widget page format
	 *
	 * @param discoUri the DiSCO URI
	 * @param model the Spring model
	 * @return the DiSCO widget page
	 * @throws Exception the exception
	 */
	@RequestMapping(value="/discos/{uri}/widget", method = RequestMethod.GET)
	public String discoGraphWidget(@PathVariable(value="uri") String discoUri, Model model) throws Exception {
		LOG.info("DiSCO visualization requested: {}", discoUri);

		discoUri = URLDecoder.decode(discoUri, "UTF-8");
		
		DiSCODTO discoDTO = dataDisplayService.getDiSCODTO(discoUri);
	    
		model.addAttribute("DISCO", discoDTO);	
	    model.addAttribute("RESOURCEURI", discoDTO.getUri());
	    
    	return "discowidget";
	}

	/**
	 * GET the graphdata for a DiSCO
	 *
	 * @param discoUri the DiSCO URI
	 * @param view the current page view (visual, widget or standard)
	 * @param model the Spring model
	 * @return the DiSCO graph data page
	 * @throws Exception the exception
	 */
	@RequestMapping(value="/discos/{uri}/graphdata", method = RequestMethod.GET)
	public String discoGraphData(@PathVariable(value="uri") String discoUri, 
			@RequestParam(value="view", required=false) String view, Model model) throws Exception {
		LOG.info("DiSCO visualization requested: {}", discoUri);
		if (view==null || view.length()==0){
			view=STANDARD_VIEW;
		}
		try {
			discoUri = URLDecoder.decode(discoUri, "UTF-8");
			
			DiSCODTO discoDTO = dataDisplayService.getDiSCODTO(discoUri);
		    
			model.addAttribute("DISCO", discoDTO);	
		    model.addAttribute("RESOURCEURI", discoDTO.getUri());

		    Graph discoGraph = dataDisplayService.getDiSCOGraph(discoDTO);  
		    model.addAttribute("GRAPH", discoGraph);
		    model.addAttribute("VIEWMODE", view);
			
		} catch (Exception e){
			throw new RMapWebException(e, ErrorCode.ER_PROBLEM_LOADING_DISCOGRAPH);
		}
		return "discograph";		
	}	
	
	/**
	 * GET table data for DiSCO
	 *
	 * @param discoUri the DiSCO URI
	 * @param view the current page view (visual, widget or standard)
	 * @param model the Spring model
	 * @return the DiSCO table data page
	 * @throws Exception the exception
	 */
	@RequestMapping(value="/discos/{uri}/tabledata", method = RequestMethod.GET)
	public String discoTableData(@PathVariable(value="uri") String discoUri, 
			@RequestParam(value="view", required=false) String view, Model model, 
			@RequestParam(value="offset", required=false) Integer offset) throws Exception {
		LOG.info("DiSCO visualization requested: {}", discoUri);
		if (view==null || view.length()==0){
			view=STANDARD_VIEW;
		}
		try {
			discoUri = URLDecoder.decode(discoUri, "UTF-8");
			
			DiSCODTO discoDTO = dataDisplayService.getDiSCODTO(discoUri);
		    
			model.addAttribute("DISCO", discoDTO);	
		    model.addAttribute("RESOURCEURI", discoDTO.getUri());
		    
	    	List<ResourceDescription> discoTableData = dataDisplayService.getDiSCOTableData(discoDTO, offset);
		    model.addAttribute("TABLEDATA", discoTableData);
		    PageStatus pageStatus = dataDisplayService.getDiSCOPageStatus(discoDTO.getRelatedStatements(), offset);
		    model.addAttribute("PAGINATOR", pageStatus);
			
		} catch (Exception e){
			throw new RMapWebException(e, ErrorCode.ER_PROBLEM_LOADING_DISCOGRAPH);
		}
		return "discotable";
	}	

	
}
