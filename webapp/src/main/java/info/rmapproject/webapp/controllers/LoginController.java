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
import info.rmapproject.webapp.auth.OAuthProviderAccount;
import info.rmapproject.webapp.auth.GoogleOAuthProvider;
import info.rmapproject.webapp.auth.OAuthProviderName;
import info.rmapproject.webapp.auth.TwitterOAuthProvider;
import info.rmapproject.webapp.auth.OrcidOAuthProvider;
import info.rmapproject.webapp.service.UserMgtService;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.github.scribejava.core.model.Token;

/**
 * Handles requests related to user management and sign in.
 *
 * @author khanson
 */
@Controller
@SessionAttributes({"user","account"})
public class LoginController {

	/** Service for user management. */
	@Autowired
	private UserMgtService userMgtService;
		
	/** The OAuth provider google. */
	@Autowired
	@Qualifier("oAuthProviderGoogle")
	private GoogleOAuthProvider oAuthProviderGoogle;

	/** The OAuth provider twitter. */
	@Autowired
	@Qualifier("oAuthProviderTwitter")
	private TwitterOAuthProvider oAuthProviderTwitter;

	/** The OAuth provider orcid. */
	@Autowired
	@Qualifier("oAuthProviderOrcid")
	private OrcidOAuthProvider oAuthProviderOrcid;
	
		
	/**
	 * Login using Google.
	 *
	 * @param session the HTTP session
	 * @return the welcome page
	 */
	@RequestMapping(value={"/user/login/google"}, method = RequestMethod.GET)
	public String logingoogle(HttpSession session) {
		//see if we are already logged in
		OAuthProviderAccount account = (OAuthProviderAccount) session.getAttribute("account");
		if(account == null) {
			//not logged in create service and redirect to google login
			return "redirect:" + oAuthProviderGoogle.getAuthorizationUrl(null);
		}
		//already logged in goto welcome page
		return "redirect:/user/welcome";
	}
	
	/**
	 * Googlecallback.
	 *
	 * @param oauthVerifier the oauth verifier
	 * @param session the HTTP session
	 * @param model the Spring model
	 * @return the signup or welcome page depending on whether account exists.
	 */
	@RequestMapping(value={"/user/googlecallback"}, method = RequestMethod.GET)
	public String googlecallback(@RequestParam(value="code", required=false) String oauthVerifier, HttpSession session, Model model) {
				
		OAuthProviderName idProvider = OAuthProviderName.GOOGLE;
		Token accessToken = oAuthProviderGoogle.createAccessToken(null, oauthVerifier);

		//load profile from request to service
		OAuthProviderAccount account = 
				oAuthProviderGoogle.loadOAuthProviderAccount(accessToken, idProvider);
		
		// store access token as a session attribute
		session.setAttribute("account", account);
		
		User user = userMgtService.loadUserFromOAuthAccount(account);
				
		if (user==null){
			String name = account.getDisplayName();	
			String email = account.getAccountPublicId();
			session.setAttribute("user", new User(name, email));
			return "redirect:/user/signup";
		}
		else {
			session.setAttribute("user", user);		
			return "redirect:/user/welcome";
		}
	}
	

	/**
	 * Login using ORCID.
	 *
	 * @param session the HTTP session
	 * @return the user welcome page
	 */
	@RequestMapping(value={"/user/login/orcid"}, method = RequestMethod.GET)
	public String loginorcid(HttpSession session) {
		//see if we are already logged in
		OAuthProviderAccount account = (OAuthProviderAccount) session.getAttribute("account");
		if(account == null) {
			//not logged in create service and redirect to orcid login
			return "redirect:" + oAuthProviderOrcid.getAuthorizationUrl(null);
		}
		//already logged in goto welcome page
		return "redirect:/user/welcome";
	}
	
	/**
	 * Orcid callback page.
	 *
	 * @param oauthVerifier the oauth verifier
	 * @param session the HTTP session
	 * @param model the Spring model
	 * @return the signup or welcome page depending on whether account exists.
	 */
	@RequestMapping(value={"/user/orcidcallback"}, method = RequestMethod.GET)
	public String orcidcallback(@RequestParam(value="code", required=false) String oauthVerifier, HttpSession session, Model model) {
		
		OAuthProviderName idProvider = OAuthProviderName.ORCID;
		
		Token accessToken = oAuthProviderOrcid.createAccessToken(null, oauthVerifier);

		//load profile from request to service
		OAuthProviderAccount account = 
				oAuthProviderOrcid.loadOAuthProviderAccount(accessToken, idProvider);
		// store account as a session attribute
		session.setAttribute("account", account);
		
		User user = userMgtService.loadUserFromOAuthAccount(account);
		if (user==null){
			session.setAttribute("user", new User(account.getDisplayName()));
			return "redirect:/user/signup";
		}
		else {
			session.setAttribute("user", user);		
			return "redirect:/user/welcome";
		}
	}
	

	/**
	 * Login using Twitter.
	 *
	 * @param session the HTTP session
	 * @return the welcome page
	 */
	@RequestMapping(value={"/user/login/twitter"}, method = RequestMethod.GET)
	public String logintwitter(HttpSession session) {
		//see if we are already logged in
		OAuthProviderAccount account = (OAuthProviderAccount) session.getAttribute("account");
		if(account == null) {
			Token requestToken = oAuthProviderTwitter.createRequestToken();
			session.setAttribute("requesttoken", requestToken);
			//not logged in create service and redirect to twitter login
			return "redirect:" + oAuthProviderTwitter.getAuthorizationUrl(requestToken);
		}
		//already logged in goto welcome page
		return "redirect:/user/welcome";
	}
	
	/**
	 * Twitter callback.
	 *
	 * @param oauthToken the oauth token
	 * @param oauthVerifier the oauth verifier
	 * @param session the HTTP session
	 * @param model the Spring model
	 * @return the signup or welcome page depending on whether account exists.
	 */
	@RequestMapping(value={"/user/twittercallback"}, method = RequestMethod.GET)
	public String twittercallback(@RequestParam(value="oauth_token", required=false) String oauthToken,
				@RequestParam(value="oauth_verifier", required=false) String oauthVerifier, HttpSession session, Model model) {

		Token requestToken = (Token) session.getAttribute("requesttoken");
		if (requestToken == null){
			return "redirect:/user/login";
		}
		
		OAuthProviderName idProvider = OAuthProviderName.TWITTER;
		Token accessToken = 
				oAuthProviderTwitter.createAccessToken(requestToken,oauthVerifier);

		//load profile from request to service
		OAuthProviderAccount account = 
				oAuthProviderTwitter.loadOAuthProviderAccount(accessToken, idProvider);
		
		// store access token as a session attribute
		session.setAttribute("account", account);
				
		User user = userMgtService.loadUserFromOAuthAccount(account);
		
		if (user==null){
			session.setAttribute("user", new User(account.getDisplayName()));
			return "redirect:/user/signup";
		}
		else {
			session.setAttribute("user", user);		
			return "redirect:/user/welcome";
		}
	}
	
		
	/**
	 * Return page that shows login options.
	 *
	 * @param model the Spring model
	 * @param session the HTTP session
	 * @return Login options page
	 */
	@RequestMapping(value="/user/login", method=RequestMethod.GET)
	public String loginPage(Model model, HttpSession session) {
		return "user/login";
	}
		
}
