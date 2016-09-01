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
package info.rmapproject.webapp.utils;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.util.UrlPathHelper;

/**
 * This class resolves an issue where Spring automatically decodes the HTTP path.  In other words 
 * without this, /app/discos/ark%3A%2F22573%2Frmd18mdcxp becomes /appdev/discos/ark:/22573/rmd18mdcxp
 * and returns a "file not found" error.
 * @author khanson
 *
 */
public class UrlPathHelperNonDecoding extends UrlPathHelper {
	 
	/**
	 * Instantiates a new url path helper non decoding class
	 */
	public UrlPathHelperNonDecoding() {
		super.setUrlDecode(false);
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.web.util.UrlPathHelper#setUrlDecode(boolean)
	 */
	@Override
	public void setUrlDecode(boolean urlDecode) {
		if (urlDecode) {
			throw new IllegalArgumentException("Handler does not support URL decoding.");
		}
	}
 
	/* (non-Javadoc)
	 * @see org.springframework.web.util.UrlPathHelper#getServletPath(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public String getServletPath(HttpServletRequest request) {
		String servletPath = getOriginatingServletPath(request);
		return servletPath;
	}
	
 
	/* (non-Javadoc)
	 * @see org.springframework.web.util.UrlPathHelper#getOriginatingServletPath(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public String getOriginatingServletPath(HttpServletRequest request) {
		String servletPath = request.getRequestURI().substring(request.getContextPath().length());
		return servletPath;
	}
}