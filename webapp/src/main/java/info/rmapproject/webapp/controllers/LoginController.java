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

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.github.scribejava.core.model.Token;

import info.rmapproject.auth.model.User;
import info.rmapproject.webapp.auth.GoogleOAuthProvider;
import info.rmapproject.webapp.auth.OAuthProviderAccount;
import info.rmapproject.webapp.auth.OAuthProviderName;
import info.rmapproject.webapp.auth.OrcidOAuthProvider;
import info.rmapproject.webapp.auth.TwitterOAuthProvider;
import info.rmapproject.webapp.service.UserMgtService;
import info.rmapproject.webapp.utils.SiteProperties;

/**
 * Handles requests related to user management and sign in.
 *
 * @author khanson
 */
@Controller
@SessionAttributes({"user","account"})
public class LoginController {

	/** The log. */
	private static final Logger LOG = LoggerFactory.getLogger(LoginController.class);
	
	/** Service for user management. */
	private UserMgtService userMgtService;
		
	/** The OAuth provider google. */
	private GoogleOAuthProvider oAuthProviderGoogle;

	/** The OAuth provider twitter. */
	private TwitterOAuthProvider oAuthProviderTwitter;

	/** The OAuth provider orcid. */
	private OrcidOAuthProvider oAuthProviderOrcid;
	
	/** Page properties used in each page*/
	private SiteProperties siteProperties;

	@Autowired
	public LoginController(UserMgtService userMgtService, 
				@Qualifier("oAuthProviderGoogle") GoogleOAuthProvider oAuthProviderGoogle,
				@Qualifier("oAuthProviderTwitter") TwitterOAuthProvider oAuthProviderTwitter,
				@Qualifier("oAuthProviderOrcid") OrcidOAuthProvider oAuthProviderOrcid,
				SiteProperties siteProperties) {
		this.userMgtService = userMgtService;
		this.oAuthProviderGoogle = oAuthProviderGoogle;
		this.oAuthProviderTwitter = oAuthProviderTwitter;
		this.oAuthProviderOrcid = oAuthProviderOrcid;
		this.siteProperties = siteProperties;
	}
			
	/**
	 * Login using Google.
	 *
	 * @param session the HTTP session
	 * @return the welcome page
	 */
	@RequestMapping(value={"/user/login/google"}, method = RequestMethod.GET)
	public String logingoogle(HttpSession session) {
		if (!siteProperties.isGoogleEnabled()) {
			LOG.debug("Google OAuth unavailable, redirecting to home page");
			return "redirect:/home";
		}
		
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
		if (!siteProperties.isGoogleEnabled()) {
			LOG.debug("Google OAuth unavailable, redirecting to home page");
			return "redirect:/home";
		}
		
		Token accessToken = oAuthProviderGoogle.createAccessToken(null, oauthVerifier);

		//load profile from request to service
		OAuthProviderAccount account = 
				oAuthProviderGoogle.loadOAuthProviderAccount(accessToken, OAuthProviderName.GOOGLE);
		
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
		if (!siteProperties.isOrcidEnabled()) {
			LOG.debug("ORCID OAuth unavailable, redirecting to home page");
			return "redirect:/home";
		}
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
	 * ORCID callback page.
	 *
	 * @param oauthVerifier the oauth verifier
	 * @param session the HTTP session
	 * @param model the Spring model
	 * @return the signup or welcome page depending on whether account exists.
	 */
	@RequestMapping(value={"/user/orcidcallback"}, method = RequestMethod.GET)
	public String orcidcallback(@RequestParam(value="code", required=false) String oauthVerifier, 
			@RequestParam(value="error", required=false) String error, @RequestParam(value="error_description", required=false) String errorDescription, 
			RedirectAttributes redirectAttributes, HttpSession session, Model model) {
		if (!siteProperties.isOrcidEnabled()) {
			LOG.debug("ORCID OAuth unavailable, redirecting to home page");
			return "redirect:/home";
		}
		if (error!=null && error.equals("access_denied")){
			//this only happens with ORCID
			redirectAttributes.addFlashAttribute("notice", "The ORCID Login failed with the following error: " + errorDescription);
			return "redirect:/user/orcidlogininfo";
		}
		
		Token accessToken = oAuthProviderOrcid.createAccessToken(null, oauthVerifier);

		//load profile from request to service
		OAuthProviderAccount account = 
				oAuthProviderOrcid.loadOAuthProviderAccount(accessToken, OAuthProviderName.ORCID);
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
	 * Return information page about orcid login - this primarily seen if user
	 * denies oauth access, it will redirect here to provide more information.
	 *
	 * @param model the Spring model
	 * @param session the HTTP session
	 * @return Login options page
	 */
	@RequestMapping(value="/user/orcidlogininfo", method=RequestMethod.GET)
	public String orcidDeniedPage(Model model, HttpSession session) {
		if (!siteProperties.isOauthEnabled()) {
			LOG.debug("No OAuth option available, redirecting to home page");
			return "redirect:/home";
		}		
		return "user/orcidlogininfo";
	}
	
	/**
	 * Login using Twitter.
	 *
	 * @param session the HTTP session
	 * @return the welcome page
	 */
	@RequestMapping(value={"/user/login/twitter"}, method = RequestMethod.GET)
	public String logintwitter(HttpSession session) {
		if (!siteProperties.isTwitterEnabled()) {
			LOG.debug("Twitter OAuth unavailable, redirecting to home page");
			return "redirect:/home";
		}
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
		if (!siteProperties.isTwitterEnabled()) {
			LOG.debug("Twitter OAuth unavailable, redirecting to home page");
			return "redirect:/home";
		}
		
		Token requestToken = (Token) session.getAttribute("requesttoken");
		if (requestToken == null){
			return "redirect:/user/login";
		}
		
		Token accessToken = 
				oAuthProviderTwitter.createAccessToken(requestToken,oauthVerifier);

		//load profile from request to service
		OAuthProviderAccount account = 
				oAuthProviderTwitter.loadOAuthProviderAccount(accessToken, OAuthProviderName.TWITTER);
		
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
		if (!siteProperties.isOauthEnabled()) {
			LOG.debug("No OAuth option available, redirecting to home page");
			return "redirect:/home";
		}		
		return "user/login";
	}
		
}
