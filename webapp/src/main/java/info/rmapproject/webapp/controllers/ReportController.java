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

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 * Handles display of reports.
 *
 * @author khanson
 */
@Controller
@SessionAttributes({"user","account"})
public class ReportController {
	
	//private static final Logger logger = LoggerFactory.getLogger(ReportController.class);
	
	/**
	 * GETs the report list.
	 *
	 * @param model the model
	 * @return the user reports list
	 * @throws Exception the exception
	 */
	@RequestMapping(value="/user/reports", method=RequestMethod.GET)
	public String showKeyList(Model model) throws Exception {
        return "user/reports";	
	}
}
