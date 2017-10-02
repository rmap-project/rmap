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
package info.rmapproject.webapp.auth;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import info.rmapproject.auth.model.User;
import info.rmapproject.webapp.utils.Constants;

/**
 * Authentication interceptor to check oauth user/password during each interaction
 */
public class AuthenticationInterceptor implements HandlerInterceptor {

	@Value("${rmapweb.admin-tool-enabled}")
	private String adminToolEnabled;
	
	/** Session attribute for "user". */
	private static final String USER_SESSION_ATTRIBUTE = "user";
	
	/** Session attribute for "account". */
	private static final String ACCOUNT_SESSION_ATTRIBUTE = "account";
	
	/** Method used for signup form. */
	private static final String SIGNUPFORM_METHOD = "signupForm";
	
	/** Method used to add user. */
	private static final String ADDUSER_METHOD = "addUser";
	
	/** Path for user login. */
	private static final String USER_LOGIN_PATH = "/user/login";
	
	/** Path for user signup. */
	private static final String USER_SIGNUP_PATH = "/user/signup";

	/** Path for home page. */
	private static final String HOMEPAGE_PATH = "/home";
	
	/** Path for admin user login. */
	private static final String ADMIN_LOGIN_PATH = "/admin/login";

	/** Path for admin users list. */
	private static final String ADMIN_USERS_PATH = "/admin/users";
	
	

	private static final Logger LOG = LoggerFactory.getLogger(AuthenticationInterceptor.class);
	
	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.HandlerInterceptor#preHandle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object)
	 */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
   	    	
    	HandlerMethod hm=(HandlerMethod)handler; 
    	Method method=hm.getMethod(); 
    	
    	//don't need to do anything if it's not a controller class
    	if(!method.getDeclaringClass().isAnnotationPresent(Controller.class)) {
       		return true;
    	}

		//if trying to access an admin path but admin tool is disabled, redirect to home
		Boolean blAdminToolEnabled = (adminToolEnabled!=null && adminToolEnabled.equals("true")) ? true : false;
		LOG.debug("Admin tool enabled status is {}", blAdminToolEnabled);
		if (request.getServletPath().contains("/admin") && !blAdminToolEnabled) {
			response.sendRedirect(request.getContextPath() + HOMEPAGE_PATH);
			return false;
		}
		
		if	(method.isAnnotationPresent(LoginRequired.class) || method.isAnnotationPresent(AdminLoginRequired.class)) { 
 
			Boolean adminLoggedIn = (Boolean) request.getSession().getAttribute(Constants.ADMIN_LOGGEDIN_SESSATTRIB);
			adminLoggedIn = (adminLoggedIn==null) ? false : adminLoggedIn;
			
			LOG.debug("Admin logged in status is {}", adminLoggedIn);
			
			if(method.isAnnotationPresent(LoginRequired.class)) { 
	    		//these are all of the cases in which you would not go to the requested page, but instead be redirected somewhere else.
				
	    		User user = (User) request.getSession().getAttribute(USER_SESSION_ATTRIBUTE);
	    		
	    		//The LoginRequired annotation is on all user management pages. If admin is logged in but no user is loaded, redirect to users list to pick one
				if (adminLoggedIn && user==null) { 
	    			response.sendRedirect(request.getContextPath() + ADMIN_USERS_PATH);
	    			return false;
				}

				if (!adminLoggedIn) {

	    	        //if admin user isn't logged in and no oauth account is loaded redirect to oauth login
	    	        OAuthProviderAccount account = (OAuthProviderAccount) request.getSession().getAttribute(ACCOUNT_SESSION_ATTRIBUTE);
		    		if(account == null) {
		    			response.sendRedirect(request.getContextPath() + USER_LOGIN_PATH);
		    			return false;
		    		}   
	    	        
	    	        //if admin user isn't logged in and there is an oauth account signed in, but no user to go with it, redirect to the sign up form
		    		if((!method.getName().equals(SIGNUPFORM_METHOD)
		    				&&!method.getName().equals(ADDUSER_METHOD)) 
		    				&& (user == null || user.getUserId()==0)) {
		    			//new user, get them signed up!
		    			response.sendRedirect(request.getContextPath() + USER_SIGNUP_PATH);
		    			return false;
		    		}   				
				}
    	    } else if (method.isAnnotationPresent(AdminLoginRequired.class)) {
    	    	if (!adminLoggedIn) {
	    			response.sendRedirect(request.getContextPath() + ADMIN_LOGIN_PATH);
	    			return false;
    	    	}    	    	
    	    }
    		
        }
        return true;
        
    }

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.HandlerInterceptor#afterCompletion(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, java.lang.Exception)
	 */
	@Override
	public void afterCompletion(HttpServletRequest arg0,
			HttpServletResponse arg1, Object arg2, Exception arg3)
			throws Exception {
		//do nothing
		
	}

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.HandlerInterceptor#postHandle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.web.servlet.ModelAndView)
	 */
	@Override
	public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1,
			Object arg2, ModelAndView arg3) throws Exception {
		// do nothing
		
	}

}
