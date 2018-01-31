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
package info.rmapproject.webapp.auth;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Admin login model for use with Admin login form
 * @author khanson
 *
 */
public class AdminLogin {
	
	@NotEmpty(message="Name required")
	private String username;
	
	@NotEmpty(message="Password required")
	private String password;	
	
	public AdminLogin() {}
	
	@Autowired
	public AdminLogin(String username, String password) {
		setUsername(username);
		setPassword(password);
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AdminLogin) {
			AdminLogin adminLogin = (AdminLogin) obj;
			if (this.username.equals(adminLogin.username) && this.password.equals(adminLogin.password)) {
				return true;
			}
		}
		return false;
	}


	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (username != null ? username.hashCode() : 0);
		result = 31 * result + (password != null ? password.hashCode() : 0);
		return result;
	}
	
	
}
