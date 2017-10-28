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

import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import info.rmapproject.webapp.domain.SearchForm;

/**
 * Handles display of the home and contact pages.
 *
 * @author khanson
 */
@Controller
@SessionAttributes({"user","account"})
public class HomeController {
		
	/**
	 * GETs the Home page.
	 *
	 * @param locale the locale
	 * @param model the Spring model
	 * @return the home page
	 */
	@RequestMapping(value={"/", "/home"}, method = RequestMethod.GET)
	public String home(Locale locale, Model model) {	
		if (!model.containsAttribute("search")){
			//otherwise initiate search form.
			SearchForm search = new SearchForm();
			model.addAttribute("search", search);			
		}
		return "home";
	}
		
	/**
	 * GETs the About page
	 *
	 * @param model the Spring model
	 * @return the about page
	 */
	@RequestMapping(value={"/about"}, method = RequestMethod.GET)
	public String about(Model model) {
		return "about";
	}
	
	@RequestMapping(value={"/about/glossary"}, method=RequestMethod.GET)
	public String glossary(Model model) {
		return "glossary";
	}	
	
}
