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

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Java representation of ApiKeys database table.
 * Api Keys are associated with a user that logged into the GUI
 * The keys can be used for write access to the RMap API.
 * @author khanson
 *
 */

@Entity
@Table(name="ApiKeys")
public class ApiKey {
	
	/** Unique id column for API Key table*. */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int apiKeyId;
	
	/** String of base64 characters that are used as a key to write to RMap API*. */
	private String accessKey;
	
	/**String of base64 characters, used with the accessKey as a kind of password, 
	 * the accessKey/secret combo is for API write access.*/
	private String secret;
	
	/** Key URI is a university unique identifier to identify the specific api key used for access
	 * there is an option to associate this key with the Event record in RMap so that you can see which
	 * key was used to create the record.	 */
	private String keyUri;
	
	/** A label associated with the key to tag or describe it. */
	@NotEmpty
	private String label;
	
	/** A longer description of the key. */
	private String note;
	
	/**The status of the key (e.g. inactive, revoked)*/
	@Enumerated(EnumType.STRING)
	private KeyStatus keyStatus;
	
	/**The start date from which the key is valid. If the key is used before this date it will not work */
	@DateTimeFormat(pattern = "MM/dd/yyyy")
	private Date startDate;
	
	/**The end date for the key after which the key is no longer valid.  If the key is used after this date it will not work*/
	@DateTimeFormat(pattern = "MM/dd/yyyy")
	private Date endDate;
	
	/** The date the key was created*. */
	private Date createdDate=new Date();
	
	/** The date the key was last modified. */
	private Date lastModifiedDate=new Date();
	
	/** The date the key was revoked (null if empty)*. */
	private Date revokedDate;
	
	/** Flag to include the key in the Event information of RMap so that you can identify which key created the DiSCO. */
	private boolean includeInEvent = false;
	
	/** The user that the key is associated with. */
	private int userId;
	
	/**
	 * Gets the api key id.
	 *
	 * @return the api key id
	 */
	public int getApiKeyId() {
		return apiKeyId;
	}
	
	/**
	 * Sets the api key id.
	 *
	 * @param apiKeyId the new API key id
	 */
	public void setApiKeyId(int apiKeyId) {
		this.apiKeyId = apiKeyId;
	}
	
	/**
	 * Gets the access key.
	 *
	 * @return the access key
	 */
	public String getAccessKey() {
		return accessKey;
	}
	
	/**
	 * Sets the access key.
	 *
	 * @param accessKey the new access key
	 */
	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}
	
	/**
	 * Gets the secret.
	 *
	 * @return the secret
	 */
	public String getSecret() {
		return secret;
	}
	
	/**
	 * Sets the secret.
	 *
	 * @param secret the new secret
	 */
	public void setSecret(String secret) {
		this.secret = secret;
	}
	
	/**
	 * Gets the key uri.
	 *
	 * @return the key uri
	 */
	public String getKeyUri() {
		return keyUri;
	}
	
	/**
	 * Sets the key uri.
	 *
	 * @param keyUri the new key uri
	 */
	public void setKeyUri(String keyUri) {
		this.keyUri = keyUri;
	}
	
	/**
	 * Gets the label.
	 *
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}
	
	/**
	 * Sets the label.
	 *
	 * @param label the new label
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	
	/**
	 * Gets the note.
	 *
	 * @return the note
	 */
	public String getNote() {
		return note;
	}
	
	/**
	 * Sets the note.
	 *
	 * @param note the new note
	 */
	public void setNote(String note) {
		this.note = note;
	}
	
	/**
	 * Gets the key status.
	 *
	 * @return the key status
	 */
	public KeyStatus getKeyStatus() {
		return keyStatus;
	}
	
	/**
	 * Sets the key status.
	 *
	 * @param keyStatus the new key status
	 */
	public void setKeyStatus(KeyStatus keyStatus) {
		this.keyStatus = keyStatus;
	}
	
	/**
	 * Gets the start date.
	 *
	 * @return the start date
	 */
	public Date getStartDate() {
		return startDate;
	}
	
	/**
	 * Sets the start date.
	 *
	 * @param startDate the new start date
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	/**
	 * Gets the end date.
	 *
	 * @return the end date
	 */
	public Date getEndDate() {
		return endDate;
	}
	
	/**
	 * Sets the end date.
	 *
	 * @param endDate the new end date
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
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
	 * Gets the last modified date.
	 *
	 * @return the last modified date
	 */
	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}
	
	/**
	 * Sets the last modified date.
	 *
	 * @param lastModifiedDate the new last modified date
	 */
	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
	
	/**
	 * Gets the revoked date.
	 *
	 * @return the revoked date
	 */
	public Date getRevokedDate() {
		return revokedDate;
	}
	
	/**
	 * Sets the revoked date.
	 *
	 * @param revokedDate the new revoked date
	 */
	public void setRevokedDate(Date revokedDate) {
		this.revokedDate = revokedDate;
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
	 * Checks if is include in event.
	 *
	 * @return true, if is include in event
	 */
	public boolean isIncludeInEvent() {
		return includeInEvent;
	}
	
	/**
	 * Sets the include in event.
	 *
	 * @param includeInEvent the new include in event
	 */
	public void setIncludeInEvent(boolean includeInEvent) {
		this.includeInEvent = includeInEvent;
	}
	
	
	
}
