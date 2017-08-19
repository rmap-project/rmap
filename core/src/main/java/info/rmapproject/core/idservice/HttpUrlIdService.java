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
package info.rmapproject.core.idservice;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is derived from an ARK ID generator, taken from Portico and generalized to use with
 * simple http url based ID generator services.  This ID generator can be pointed at a web-based
 * ID service.  Output must be plain text, with one ID per line.
 * Class reads from a web based noid-generator, then adds the prefixes as necessary
 * @author Nigel Kerr, khanson
 */
public class HttpUrlIdService implements IdService {
	
	/** The log. */
	private static final Logger log = LoggerFactory.getLogger(HttpUrlIdService.class);

	/**  Wait time to retry when ID retrieval unsuccessful (5 seconds). */
	private static final int RETRY_WAIT_TIME = 5000;

	/**  List of available noids. */
	private final List<String> noids = new ArrayList<String>();

	
	/** The HTTP ID service url. */
	private String serviceUrl = "";
	
	/** The ID prefix to be added. */
	private String idPrefix = "";
	
	/** Set a maximum retry attempts value if you want to cap the property setting. -1 means no maximum */
	private int maxRetryAttempts = -1;
	
	/** String of characters to be removed from the ID returned by the ID service. */
	private String replaceString = "";

	/** User name if using Basic Auth. */
	private String userName = "";

	/** User pwd if using Basic Auth. */
	private String userPassword = "";

	/** Length of ID to validate against. */
	private int idLength = -1;

	/** String regex to validate an ID against. */
	private String idRegex = "";
	
	/**
	 * Instantiates a new ARK ID service.
	 */
	public HttpUrlIdService() {

	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.idservice.IdService#isValidId(java.net.URI)
	 */
	@Override
	public boolean isValidId(URI id) throws Exception {
		boolean isValid = isValidId(id.toASCIIString());
		return isValid;
	}
	
	
	/**
	 * Check the string value of an ID is valid by checking it matches a regex and is the right length
	 *
	 * @param id the id
	 * @return boolean
	 * @throws Exception the exception
	 */
	private boolean isValidId(String id) throws Exception {
		boolean isValid = true;
		if (idRegex!=null && idRegex.length()>0){
			isValid = id.matches(idRegex);
		}
		if (isValid && idLength>0) {
			isValid = (id.length()==idLength);
		}
		return isValid;
	}
	

	/**
	 * Returns the noid id from the specific ArrayList tied to that Content Type
	 * in the Hashtable. If noidID list is zero, then it goes into a loop until
	 * the list is populated. List is populated by http request to noid service.
	 * Also, the noid ids obtained from the noid service is validated against
	 * the env prefix defined in LDAP. Also, the number of ids requested also
	 * defined in LDAP
	 *
	 * @return the noid id
	 * @throws Exception the exception
	 */

	public synchronized String getNoidId() throws Exception {
		log.debug("Getting noid id");

		if (noids.size() <= 0) {
			try {
				getMoreNoids();
			} catch (Exception e) {
				log.error("While trying to fill more noids, caught exception",
						e);
			}
		}
		if (noids.size() > 0) {
			return noids.remove(noids.size()-1);
		} else {
			throw new Exception(
					"Tried to fill noids and failed!  No Noids available!");
		}
	}

	/**
	 * Gets the number of NOIDs available in the list in memory.
	 *
	 * @return the number of noids available
	 */
	public int howManyAvailable() {
		return noids.size();
	}


	/**
	 * When the list of NOID IDs to use for the ARKs is empty, this method refills the list.
	 *
	 * @return the more noids
	 * @throws Exception the exception
	 */
	private synchronized void getMoreNoids() throws Exception {
		int retryCounter = 0;

		int HTTP_STATUS_OK = 200;

		BufferedReader reader = null;
		boolean shouldRetry = true;
		do {
			retryCounter++;
			log.debug("Minting ids from " + serviceUrl);
			URL noidUrl = null;
			HttpURLConnection noidCon = null;
			try {
				noidUrl = new URL(serviceUrl);
				noidCon = (HttpURLConnection) noidUrl.openConnection();
				
				if (userName!=null && userName.length()>0 && userPassword!=null){
					String userpass = userName + ":" + userPassword;
					String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
					noidCon.setRequestProperty ("Authorization", basicAuth);
				}
				
				noidCon.setDoInput(true);
				noidCon.setDoOutput(false);
				noidCon.connect();
				reader = new BufferedReader(new InputStreamReader(noidCon
						.getInputStream()));				
					String output = null;
					if (noidCon.getResponseCode() == HTTP_STATUS_OK) {
						while ((output = reader.readLine()) != null) {
							output = output.trim();
							if (!output.equals("")) {
								if (replaceString.length()>0){
									output = output.replaceAll(replaceString,"");
								}
								output = idPrefix + output;
								if (isValidId(output)) {
									noids.add(output);
								} else {
									log.warn("Invalid ID returned. This ID will be ignored: " + output);
								}
							}
						}
					} else {
						log.error("UNSUCCESSFUL HTTP REQUEST TO NOID SERVICE, HTTP RETURN CODE is : " + noidCon.getResponseCode());
					}
			} catch(Exception e){
				log.error("EXCEPTION CONNECTING TO NOID SERVER", e);

			} finally {

				try {
					if (reader != null){
						reader.close();
					}
				} catch (Exception e) {
					log.error("Exception while closing Buffered Reader", e);
				}
				try {
					if (noidCon != null){
						noidCon.disconnect();
					}
				} catch (Exception e) {
					log.error("Exception while closing http connection to noid service ", e);
				}
			}
			shouldRetry = (retryCounter < maxRetryAttempts && noids.size() == 0);
			//WAIT FOR 5 SECS BEFORE RE-TRYING TO OVERCOME TEMPORARY NETWORK FAILURES
			//OR THE NOID SERVER BEING BUSY SERVICING ANOTHER REQUEST.
			if(shouldRetry){
				try{
					wait(RETRY_WAIT_TIME);
				}catch(InterruptedException ie){
					log.error("Wait interrupted in retry loop", ie);
				}

			}

		} while (shouldRetry);

		log.debug("Extracted ids = |" + noids.size() + "|");
		if(noids.size() == 0){
			throw new Exception("Could not retrieve new IDs after retries. maxRetryAttempts:"+maxRetryAttempts);
		}
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.idservice.IdService#createId()
	 */
	public URI createId() throws Exception {
			try {
				return new URI(getNoidId());
				// need to instead return configured per Component.getInstance("noidServiceImpl")
			} catch (Exception e) {
				throw new Exception("Failed to create a new ID.", e);
			}
	}

	/**
	 * Specifies the url for minting identifiers.  Can be configured using the {@code idservice.idMinterUrl}
	 * property.  Example values:
	 * <ul>
	 *     <li>http://localhost:8080/noid/noid.sh?2</li>
	 * </ul>
	 *
	 * @return the url used to mint new identifiers
	 */
	public String getServiceUrl() {
		return serviceUrl;
	}

	/**
	 * Specifies the url for minting identifiers.  Can be configured using the {@code idservice.idMinterUrl}
	 * property.  Example values:
	 * <ul>
	 *     <li>http://localhost:8080/noid/noid.sh?2</li>
	 * </ul>
	 *
	 * @param serviceUrl the url used to mint new identifiers
	 */
	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	/**
	 * Prefixed added to identifiers returned by {@link #getServiceUrl() the underlying identifier service}.  Can be
	 * configured using the {@code idservice.idPrefix} property.  Example values:
	 * <ul>
	 *     <li>rmap:</li>
	 *     <li>ark:/12345/</li>
	 * </ul>
	 * <p>
	 * If you are using an ARK ID service, for example, this would be {@code ark:/} followed by the Name Assigning
	 * Authority Number (NAAN) e.g. "ark:/12345/".  For ARK, see http://www.cdlib.org/uc3/naan_table.html for a
	 * registry of NAAN.
	 * </p>
	 *
	 * @return the prefix added to generated identifiers, may be empty or {@code null}
	 */
	public String getIdPrefix() {
		return idPrefix;
	}

	/**
	 * Prefixed added to identifiers returned by {@link #getServiceUrl() the underlying identifier service}.  Can be
	 * configured using the {@code idservice.idPrefix} property.  Example values:
	 * <ul>
	 *     <li>rmap:</li>
	 *     <li>ark:/12345/</li>
	 * </ul>
	 * <p>
	 * If you are using an ARK ID service, for example, this would be {@code ark:/} followed by the Name Assigning
	 * Authority Number (NAAN) e.g. "ark:/12345/".  For ARK, see http://www.cdlib.org/uc3/naan_table.html for a
	 * registry of NAAN.
	 * </p>
	 *
	 * @param idPrefix the prefix added to generated identifiers, may be empty or {@code null}
	 */
	public void setIdPrefix(String idPrefix) {
		this.idPrefix = idPrefix;
	}

	/**
	 * The maximum number attempts to contact the {@link #getServiceUrl() underlying identifier service} when minting a
	 * new identifier.  Can be configured using the {@code idservice.maxRetries} property.
	 *
	 * @return the maximum number of attempts to contact the underlying identifier service
	 */
	public int getMaxRetryAttempts() {
		return maxRetryAttempts;
	}

	/**
	 * The maximum number attempts to contact the {@link #getServiceUrl() underlying identifier service} when minting a
	 * new identifier.  Can be configured using the {@code idservice.maxRetries} property.
	 *
	 * @param maxRetryAttempts the maximum number of attempts to contact the underlying identifier service
	 */
	public void setMaxRetryAttempts(int maxRetryAttempts) {
		this.maxRetryAttempts = maxRetryAttempts;
	}

	/**
	 * Identifiers returned by the {@link #getServiceUrl() underlying identifier service} may need to be modified by
	 * this class in order to be usable by a caller of this class.  The character string supplied by this method will
	 * be removed from every identifier supplied by the underlying identifier service.   Can be configured using the
	 * {@code idservice.replaceString} property.  Example values include (single quotes are <em>not</em> a part of the
	 * value, they are used to show whitespace):
	 * <ul>
	 *     <li>'id: '</li>
	 * </ul>
	 *
	 * @return the character string to remove from identifiers generated by the underlying identifier service, may be
	 *         empty or {@code null}
	 */
	public String getReplaceString() {
		return replaceString;
	}

	/**
	 * Identifiers returned by the {@link #getServiceUrl() underlying identifier service} may need to be modified by
	 * this class in order to be usable by a caller of this class.  The character string supplied to this method will
	 * be removed from every identifier supplied by the underlying identifier service.   Can be configured using the
	 * {@code idservice.replaceString} property.  Example values include (single quotes are <em>not</em> a part of the
	 * value, they are used to show whitespace):
	 * <ul>
	 *     <li>'id: '</li>
	 * </ul>
	 *
	 * @return the character string to remove from identifiers generated by the underlying identifier service, may be
	 *         empty or {@code null}
	 */
	public void setReplaceString(String replaceString) {
		this.replaceString = replaceString;
	}

	/**
	 * The user name used when performing HTTP basic authentication against the {@link #getServiceUrl() underlying
	 * identifer service}.  Can be configured using the {@code idservice.userName} property.
	 *
	 * @return the user name for HTTP basic authentication, may be empty or {@code null}
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * The user name used when performing HTTP basic authentication against the {@link #getServiceUrl() underlying
	 * identifer service}.  Can be configured using the {@code idservice.userName} property.
	 *
	 * @param userName the user name for HTTP basic authentication, may be empty or {@code null}
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * The password used when performing HTTP basic authentication against the {@link #getServiceUrl() underlying
	 * identifer service}.  Can be configured using the {@code idservice.userPassword} property.
	 *
	 * @return the password for HTTP basic authentication, may be empty or {@code null}
	 */
	public String getUserPassword() {
		return userPassword;
	}

	/**
	 * The password used when performing HTTP basic authentication against the {@link #getServiceUrl() underlying
	 * identifer service}.  Can be configured using the {@code idservice.userPassword} property.
	 *
	 * @param userPassword the password for HTTP basic authentication, may be empty or {@code null}
	 */
	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	/**
	 * The expected length in characters of a {@link #getServiceUrl() generated identifier} after any {@link
	 * #getIdPrefix() prefix} and {@link #getReplaceString() character replacement} operations have been performed.
	 * This class uses the expected length of the identifier to verify that it is viable.  Can be configured using the
	 * {@code idservice.idLength} property.
	 *
	 * @return the expected length of an identifier, or an integer less than 1 if there is no expectation of a
	 *         consistent length
	 */
	public int getIdLength() {
		return idLength;
	}

	/**
	 * The expected length in characters of a {@link #getServiceUrl() generated identifier} after any {@link
	 * #getIdPrefix() prefix} and {@link #getReplaceString() character replacement} operations have been performed.
	 * This class uses the expected length of the identifier to verify that it is viable.  Can be configured using the
	 * {@code idservice.idLength} property.
	 *
	 * @param idLength the expected length of an identifier, or an integer less than 1 if there is no expectation of a
	 *                 consistent length
	 */
	public void setIdLength(int idLength) {
		this.idLength = idLength;
	}

	/**
	 * A regular expression used to match a {@link #getServiceUrl() generated identifier} after any {@link
	 * #getIdPrefix() prefix} and {@link #getReplaceString() character replacement} operations have been performed.
	 * This class uses the matching regex to verify that the identifier is viable.  Can be configured using the {@code
	 * idservice.idRegex} property.
	 *
	 * @return the regex used to match the identifier, may be empty or {@code null}
	 */
	public String getIdRegex() {
		return idRegex;
	}

	/**
	 * A regular expression used to match a {@link #getServiceUrl() generated identifier} after any {@link
	 * #getIdPrefix() prefix} and {@link #getReplaceString() character replacement} operations have been performed.
	 * This class uses the matching regex to verify that the identifier is viable.  Can be configured using the {@code
	 * idservice.idRegex} property.
	 *
	 * @param idRegex the regex used to match the identifier, may be empty or {@code null}
	 */
	public void setIdRegex(String idRegex) {
		this.idRegex = idRegex;
	}
}