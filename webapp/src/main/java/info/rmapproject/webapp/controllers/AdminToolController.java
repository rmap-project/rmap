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

import static info.rmapproject.webapp.utils.Constants.ADMIN_LOGGEDIN_SESSATTRIB;

import java.util.List;

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
import org.springframework.web.bind.annotation.RequestParam;

import info.rmapproject.auth.model.User;
import info.rmapproject.webapp.auth.AdminLogin;
import info.rmapproject.webapp.auth.AdminLoginRequired;
import info.rmapproject.webapp.service.UserMgtService;

/**
 * Handles requests related to the RMap Administrator tool
 *
 * @author khanson
 */
@Controller
public class AdminToolController {

	@Autowired
	private UserMgtService userMgtService;	
	
	/**The admin login object created from properties for comparison against entry from user*/
	@Autowired
	private AdminLogin correctAdminLogin;
	
	/** The log. */
	//private static final Logger log = LoggerFactory.getLogger(AdminToolController.class);
		
	/**
	 * Login using Google.
	 *
	 * @param session the HTTP session
	 * @return the welcome page
	 */
	@RequestMapping(value={"/admin/login"}, method = RequestMethod.GET)
	public String loginAdminUser(Model model, HttpSession session) {
		Boolean adminLoggedIn = (Boolean)session.getAttribute(ADMIN_LOGGEDIN_SESSATTRIB);
		if (adminLoggedIn!=null && adminLoggedIn==true) {
			return "redirect:/admin/welcome"; //already logged in
		}
		AdminLogin adminLogin = new AdminLogin();
		model.addAttribute("adminLogin", adminLogin);
		return "admin/login";
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
	@RequestMapping(value="/admin/login", method=RequestMethod.POST)
	public String loginAdminUser(@Valid AdminLogin adminLogin, BindingResult result, HttpSession session, Model model) throws Exception {
        if (result.hasErrors()) {
    		model.addAttribute("notice", "Errors found, could not login");	
            return "admin/login";
        }
       
        if (adminLogin.equals(correctAdminLogin)) {
        	//kick out of current oauth login and login as admin
        	session.setAttribute("user", null);
        	session.setAttribute("account", null);
        	session.setAttribute(ADMIN_LOGGEDIN_SESSATTRIB, true);        
    		return "redirect:/admin/welcome"; 
        } else {
    		model.addAttribute("notice", "Invalid login information. Please try again.");	
            return "admin/login";
        }
		
	}		

	/**
	 * Get the admin welcome page after sign in
	 * @param model the Spring Model
	 * @param session the HTTP session
	 * @return Welcome page
	 */
	@AdminLoginRequired
	@RequestMapping(value="/admin/welcome", method=RequestMethod.GET)
	public String welcomePage(Model model, HttpSession session) {
		return "admin/welcome";
	}

	/**
	 * Logs out the user by completing the session.
	 *
	 * @param session
	 * @return the home page
	 * @throws Exception the exception
	 */
	@AdminLoginRequired
	@RequestMapping(value="/admin/logout", method=RequestMethod.GET)
	public String logout(HttpSession session) throws Exception {
    	session.setAttribute("user", null);
    	session.setAttribute("account", null);
    	session.setAttribute(ADMIN_LOGGEDIN_SESSATTRIB, false);        
		return "redirect:/home"; 		
	}		

	/**
	 * Page to manage all users
	 *
	 * @param status the session status
	 * @return the home page
	 * @throws Exception the exception
	 */
	@AdminLoginRequired
	@RequestMapping(value="/admin/users", method=RequestMethod.GET)
	public String manageUsers(Model model, HttpSession session, @RequestParam(value="filter", required = false) String filter, 
			@RequestParam(value="offset", required = false) Integer offset, @RequestParam(value="notice", required=false) String notice) throws Exception {
		if (offset==null){offset=0;}
		if (filter==null){filter="";}
		if (notice!=null){
			model.addAttribute("notice", notice);	
		}
		
		model.addAttribute("filter", filter);
		List<User> users = userMgtService.getUsers(filter);
		model.addAttribute("userList", users);
		model.addAttribute("offset", offset);
		
		return "admin/users"; 		
	}			

	/**
	 * Page to redirect admin user to the user settings page
	 *
	 * @param status the session status
	 * @return the home page
	 * @throws Exception the exception
	 */
	@AdminLoginRequired
	@RequestMapping(value="/admin/user", method=RequestMethod.GET)
	public String editUser(Model model, HttpSession session, @RequestParam(value="userid", required = true) Integer userId) throws Exception {
		try {
			User user = userMgtService.getUserById(userId);
			session.setAttribute("user", user);
		} catch (Exception ex){
    		model.addAttribute("notice", "Could not retrieve user. Please select a user to edit.");	
			return "redirect:/admin/users";
		}
				
		return "redirect:/admin/user/settings"; 		
	}	
	
	/**
	 * Retrieves new user form for Admin tool account.
	 *
	 * @param user the RMap User
	 * @param result the form result
	 * @param model the Spring model
	 * @return the user settings page
	 * @throws Exception the exception
	 */
	@AdminLoginRequired
	@RequestMapping(value={"/admin/user/new"}, method=RequestMethod.GET)
	public String newUserFromForAdmin(ModelMap model, HttpSession session) throws Exception {
        User user = new User();
		//refresh session record and attribute
		session.setAttribute("user",null);
		model.addAttribute("userSettings",user);
		return "user/settings"; 	
	}

	/**
	 * Creates a new user using the admin tool
	 *
	 * @param user the RMap User
	 * @param result the form result
	 * @param model the Spring model
	 * @return the user settings page
	 * @throws Exception the exception
	 */
	@AdminLoginRequired
	@RequestMapping(value={"/admin/user/new"}, method=RequestMethod.POST)
	public String createNewUserAsAdmin(@ModelAttribute("userSettings") @Valid User user, BindingResult result, ModelMap model, HttpSession session) throws Exception {
        if (result.hasErrors()) {
    		model.addAttribute("notice", "Errors found, user could not be saved");	
            return "user/settings";
        }
		int userId = this.userMgtService.addUser(user, null);
		user = this.userMgtService.getUserById(userId);
		model.addAttribute("notice", "Account successfully created!");	
		model.addAttribute("userSettings",user);
		session.setAttribute("user",user);

		return "user/settings"; 	
	}

	/**
	 * Page to redirect admin user to the user settings page
	 *
	 * @param status the session status
	 * @return the home page
	 * @throws Exception the exception
	 */
	@AdminLoginRequired
	@RequestMapping(value="/admin/keys", method=RequestMethod.GET)
	public String manageUserKeys(Model model, HttpSession session, @RequestParam(value="userid", required = true) Integer userId) throws Exception {
		try {
			User user = userMgtService.getUserById(userId);
			session.setAttribute("user", user);
		} catch (Exception ex){
    		model.addAttribute("notice", "Could not retrieve user keys. Please select a user to edit.");	
			return "redirect:/admin/users";
		}
				
		return "redirect:/admin/user/keys"; 		
	}		
		
}
