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

import info.rmapproject.auth.model.User;
import info.rmapproject.webapp.auth.LoginRequired;
import info.rmapproject.webapp.auth.OAuthProviderAccount;
import info.rmapproject.webapp.service.UserMgtService;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

/**
 * Handles requests related to user management and sign in.
 *
 * @author khanson
 */
@Controller
@SessionAttributes({"user","account"})
public class AccountController {
	
	//private static final Logger logger = LoggerFactory.getLogger(AccountController.class);
	
	/** Service for user management. */
	@Autowired
	private UserMgtService userMgtService;
		
	
/*
 * *************************
 * 
 * 		Web pages
 * 
 * *************************.
 */

	/**
	 * Get the welcome page after sign in
	 * @param model the Spring Model
	 * @param session the HTTP session
	 * @return Welcome page
	 */
	@LoginRequired
	@RequestMapping(value="/user/welcome", method=RequestMethod.GET)
	public String welcomePage(Model model, HttpSession session) {
		return "/user/welcome";
	}
	
	/**
	 * Get the Sign Up Form page.
	 *
	 * @param model the Spring Model
	 * @param session the HTTP session
	 * @return the Sign Up Form page
	 */
	@LoginRequired
	@RequestMapping(value="/user/signup", method=RequestMethod.GET)
	public String signupForm(Model model, HttpSession session) {
		User user = (User) session.getAttribute("user");
		if (user != null && user.getUserId()>0){
			return "redirect:/user/welcome";
		}
		model.addAttribute("user", user);
		return "/user/signup";
	}
	
	/**
	 * Receives the POSTed Sign Up form to be processed. Returns any form errors.
	 *
	 * @param user the RMap User
	 * @param result the form Result
	 * @param session the HTTP session
	 * @param model the Spring Model
	 * @return the Welcome page
	 * @throws Exception the exception
	 */
	@LoginRequired
	@RequestMapping(value="/user/signup", method=RequestMethod.POST)
	public String addUser(@Valid User user, BindingResult result, HttpSession session, Model model) throws Exception {
        if (result.hasErrors()) {
    		model.addAttribute("notice", "Errors found, user could not be saved");	
            return "user/signup";
        }
		OAuthProviderAccount account = (OAuthProviderAccount) session.getAttribute("account");	
		int userId = this.userMgtService.addUser(user, account);
		user = this.userMgtService.getUserById(userId); //refresh record
		
		session.setAttribute("user", user); //save latest user details to session
		model.addAttribute("notice", "Signup successful!");	
		return "redirect:/user/welcome"; 		
	}	
	
	/**
	 * Get the User Settings form.
	 *
	 * @param session the HTTP session
	 * @param model the Spring model
	 * @return the User Settings page
	 */
	@LoginRequired
	@RequestMapping(value="/user/settings", method=RequestMethod.GET)
	public String settingsForm(HttpSession session, Model model) {
		User user = (User) session.getAttribute("user");
		if (user == null || user.getUserId()==0){
			return "redirect:/home";
		}
		user = this.userMgtService.getUserById(user.getUserId()); //refresh record to make sure editing latest
		model.addAttribute("userSettings",user);
        return "user/settings";	
	}
	
	/**
	 * Receives the POSTed Settings form to be processed. Returns any form errors.
	 *
	 * @param user the RMap User
	 * @param result the form result
	 * @param model the Spring model
	 * @return the user settings page
	 * @throws Exception the exception
	 */
	@LoginRequired
	@RequestMapping(value="/user/settings", method=RequestMethod.POST)
	public String updateUserSettings(@ModelAttribute("userSettings") @Valid User userSettings, BindingResult result, ModelMap model, HttpSession session) throws Exception {
        if (result.hasErrors()) {
    		model.addAttribute("notice", "Errors found, settings could not be saved");	
            return "user/settings";
        }
		this.userMgtService.updateUserSettings(userSettings);
		//refresh session record and attribute
		User currUser = this.userMgtService.getUserById(userSettings.getUserId()); 
		session.setAttribute("user",currUser);
		model.addAttribute("user", currUser); //save latest user details to session
		model.addAttribute("notice", "User settings have been saved.");
		return "user/settings"; 		
	}
		

	/**
	 * Logs out the user by completing the session.
	 *
	 * @param status the session status
	 * @return the home page
	 * @throws Exception the exception
	 */
	@RequestMapping(value="/user/logout", method=RequestMethod.GET)
	public String logout(SessionStatus status) throws Exception {
		status.setComplete();
		return "redirect:/home"; 		
	}		

	/**
	 * Page to create DiSCO (placeholder).
	 *
	 * @param status the status
	 * @return the string
	 * @throws Exception the exception
	 */
	@RequestMapping(value="/user/disco", method=RequestMethod.GET)
	public String createdisco(SessionStatus status) throws Exception {
		return "user/disco"; 		
	}		
	
	
}
