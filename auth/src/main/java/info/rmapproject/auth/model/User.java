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
package info.rmapproject.auth.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * Java representation of Users database table.
 * Captures details of a user that has signed in through the GUI
 * This tables stores relevant user information
 * @author khanson
 *
 */
@Entity
@Table(name="Users")
public class User {
	
	/** Primary key for Users database, incrementing integer. */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int userId;

	/** Name of user*. */
	@NotEmpty(message="Name cannot be empty")
	private String name;

	/** User email*. */
	@NotEmpty(message="Email cannot be empty")
	@Email(message="Invalid email")
	private String email;
	
	/** Determines whether user is active or not. */
	private Boolean isActive = true;
	
	/** Unique URI representing Agent - generated from RMap Core ID creator. */
	private String rmapAgentUri = null;	
	
	/**URI of a DiSCO associate directly with the Agent. Not implemented yet, but
	 * eventually will be able to generate a DiSCO about the Agent through the GUI*/
	private String rmapDiSCOUri = null;	
	
	/**Unique URI representing the auth key, generated from RMap Core ID generator.*/
	private String authKeyUri = null;
	
	/** Date user record created*. */
	private Date createdDate = new Date();
	
	/** Date user last accessed the the database*. */
	private Date lastAccessedDate = new Date();
	
	/** Date the user cancelled account. */
	private Date cancellationDate = null;
	
	/** True if RMap agent should be synchronised with RMap database. */
	private boolean doRMapAgentSync = false;
	
	/**List of ID provider records corresponding to the user.*/
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	@JoinColumn(name="userId")
	private Set<UserIdentityProvider> userIdentityProviders = new HashSet<UserIdentityProvider>();

	/** List of API Keys corresponding to the User*. */
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	@JoinColumn(name="userId")
	private Set<ApiKey> apiKeys = new HashSet<ApiKey>();
	
	/**
	 * Instantiates a new user.
	 */
	public User(){}
	
	/**
	 * Instantiates a new user.
	 *
	 * @param name the name
	 * @param email the email
	 */
	public User (String name, String email){
		this.setName(name);	
		this.setEmail(email);	
	}
	
	/**
	 * Instantiates a new user.
	 *
	 * @param name the name
	 */
	public User (String name) {
		this.setName(name);
	}
	
	/**
	 * Gets the user id.
	 *
	 * @return the user id
	 */
	public int getUserId() {
		return userId;
	}
	
	/**
	 * Sets the user id.
	 *
	 * @param userId the new user id
	 */
	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Gets the email.
	 *
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}
	
	/**
	 * Sets the email.
	 *
	 * @param email the new email
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	
	/**
	 * True if User is active
	 *
	 * @return true if the User is active 
	 */
	public Boolean getIsActive() {
		return isActive;
	}
	
	/**
	 * Sets the checks if is active.
	 *
	 * @param isActive set to true if the User is active, false if inactive
	 */
	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}
	
	/**
	 * Gets the RMap Agent URI.
	 *
	 * @return the RMap Agent URI
	 */
	public String getRmapAgentUri() {
		return rmapAgentUri;
	}
	
	/**
	 * Sets the RMap Agent URI.
	 *
	 * @param rmapAgentUri the new RMap Agent URI
	 */
	public void setRmapAgentUri(String rmapAgentUri) {
		this.rmapAgentUri = rmapAgentUri;
	}
	
	/**
	 * Gets the RMap DiSCO URI for the User.
	 *
	 * @return the RMap DiSCO URI
	 */
	public String getRmapDiSCOUri() {
		return rmapDiSCOUri;
	}
	
	/**
	 * Sets the RMap DiSCO URI
	 *
	 * @param rmapDiSCOUri the new RMap DiSCO URI
	 */
	public void setRmapDiSCOUri(String rmapDiSCOUri) {
		this.rmapDiSCOUri = rmapDiSCOUri;
	}
	
	/**
	 * Gets the auth key URI.
	 *
	 * @return the auth key URI
	 */
	public String getAuthKeyUri() {
		return authKeyUri;
	}
	
	/**
	 * Sets the auth key URI.
	 *
	 * @param authKeyUri the new auth key URI
	 */
	public void setAuthKeyUri(String authKeyUri) {
		this.authKeyUri = authKeyUri;
	}
	
	/**
	 * Gets the created date.
	 *
	 * @return the created date
	 */
	public Date getCreatedDate() {
		return createdDate;
	}
	
	/**
	 * Sets the created date.
	 *
	 * @param createdDate the new created date
	 */
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	
	/**
	 * Gets the last accessed date.
	 *
	 * @return the last accessed date
	 */
	public Date getLastAccessedDate() {
		return lastAccessedDate;
	}
	
	/**
	 * Sets the last accessed date.
	 *
	 * @param lastAccessedDate the new last accessed date
	 */
	public void setLastAccessedDate(Date lastAccessedDate) {
		this.lastAccessedDate = lastAccessedDate;
	}
	
	/**
	 * Gets the cancellation date.
	 *
	 * @return the cancellation date
	 */
	public Date getCancellationDate() {
		return cancellationDate;
	}
	
	/**
	 * Sets the cancellation date.
	 *
	 * @param cancellationDate the new cancellation date
	 */
	public void setCancellationDate(Date cancellationDate) {
		this.cancellationDate = cancellationDate;
	}
	
	/**
	 * Gets the user identity providers.
	 *
	 * @return the user identity providers
	 */
	public Set<UserIdentityProvider> getUserIdentityProviders() {
		return userIdentityProviders;
	}
	
	/**
	 * Sets the user identity providers.
	 *
	 * @param userIdentityProviders the new user identity providers
	 */
	public void setUserIdentityProviders(Set<UserIdentityProvider> userIdentityProviders) {
		this.userIdentityProviders = userIdentityProviders;
	}

	/**
	 * Checks if the user is configured for the RMap Agent to be synchronized with the RMap database.
	 *
	 * @return true if RMap Agent should be synchronized with RMap database
	 */
	public boolean isDoRMapAgentSync() {
		return doRMapAgentSync;
	}
	
	/**
	 * Sets to true if RMap Agent should be synchronized with the RMap database
	 *
	 * @param doRMapAgentSync the new do RMap Agent sync setting
	 */
	public void setDoRMapAgentSync(boolean doRMapAgentSync) {
		this.doRMapAgentSync = doRMapAgentSync;
	}

	/**
	 * Checks whether the User has a corresponding RMap Agent.
	 *
	 * @return the boolean
	 */
	public Boolean hasRMapAgent() {
		return (this.rmapAgentUri!=null&&this.rmapAgentUri.length()>0);
	}	

	/**
	 * Checks whether the User has a corresponding RMap DiSCO 
	 *
	 * @return the boolean
	 */
	public Boolean hasRMapDiSCO() {
		return (this.rmapDiSCOUri!=null&&this.rmapDiSCOUri.length()>0);
	}

	@Override
	public int hashCode() {
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + (email != null ? email.hashCode() : 0);
		return result;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (name != null ? !name.equals(user.name) : user.name != null) return false;
        return email != null ? email.equals(user.email) : user.email == null;
    }

    /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
	@Override
    public String toString() {
		return "id=" + userId + ", name=" + name + ", email=" + email + ";";
    }
	
}
