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

import java.net.URLEncoder;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import info.rmapproject.webapp.domain.SearchCommand;

/**
 * Handles display of the search page .
 *
 * @author khanson
 */
@Controller
@SessionAttributes({"user","account"})
@RequestMapping(value="/search")
public class SearchController {

	/** The log. */
	private static final Logger logger = LoggerFactory.getLogger(SearchController.class);
	
	/**
	 * GETs the search form.
	 *
	 * @param model the Spring model
	 * @param redirectAttributes holds flash attributes passed from a redirect
	 * @return the search page
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String searchForm(Model model, RedirectAttributes redirectAttributes) {
		logger.info("Search page");
		
		if (redirectAttributes.containsAttribute("search")){
			//true if redirected back to form due to error, for example.
			model.addAttribute("search",redirectAttributes.getFlashAttributes().get("search"));
			model.addAttribute("notice",redirectAttributes.getFlashAttributes().get("notice"));
		} else {
			//otherwise initiate search command.
			SearchCommand search = new SearchCommand();
			model.addAttribute("search", search);			
		}
		return "search";
	}
	
	/**
	 * Processes the POSTed search form.
	 *
	 * @param search the search command
	 * @param result the form results
	 * @return the resource page
	 * @throws Exception the exception
	 */
	@RequestMapping(method=RequestMethod.POST)
	public String searchResults(@Valid @ModelAttribute("search") SearchCommand search,
									BindingResult result, Model model) throws Exception {
		if (result.hasErrors()){
			model.addAttribute("search", search);
    		model.addAttribute("notice", "There was an error in your search entry.");	
			return "search";
		}

		String resourceUri = search.getSearch().trim();
		return "redirect:/resources/" + URLEncoder.encode(resourceUri, "UTF-8");
	}
	
}
