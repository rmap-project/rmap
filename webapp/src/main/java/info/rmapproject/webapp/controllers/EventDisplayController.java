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

import java.net.URLDecoder;
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

import info.rmapproject.webapp.service.DataDisplayService;
import info.rmapproject.webapp.service.dto.EventDTO;

/**
 * Handles requests for the Event data pages.
 * @author khanson
 */

@Controller
@SessionAttributes({"user","account"})
public class EventDisplayController {

	/** Service for managing RMap data display. */
	@Autowired
	private DataDisplayService dataDisplayService;

	/** The log. */
	private static final Logger log = LoggerFactory.getLogger(EventDisplayController.class);

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

		eventUri = URLDecoder.decode(eventUri, "UTF-8");
		
		EventDTO eventDTO = dataDisplayService.getEventDTO(eventUri);
		model.addAttribute("EVENT", eventDTO);
	    model.addAttribute("RESOURCEURI", eventDTO.getUri());
		
		return "events";
	}	


	/**
	 * Some platforms (e.g. PowerPoint) do not like that we have encoded URIs embedded as a REST parameter. This is a back door to 
	 * accessing a webpage by defining the Event URI as a request param
	 *
	 * @param sEventUri the Event URI
	 * @return redirect to appropriate event path
	 * @throws Exception the exception
	 */
	@RequestMapping(value="/events", method = RequestMethod.GET)
	public String resourceByReqParam(@RequestParam(value="uri") String sEventUri) throws Exception {
		return "redirect:/events/" + URLEncoder.encode(sEventUri, "UTF-8");
	}
	
	
	
	
}
