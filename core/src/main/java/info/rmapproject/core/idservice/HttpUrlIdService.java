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
package info.rmapproject.core.idservice;

import info.rmapproject.core.utils.ConfigUtils;
import info.rmapproject.core.utils.Constants;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	/**  The property key for the ARK service URL. */
	private static final String URL_PROPERTY = "idservice.idMinterUrl";
	
	/**  The property key for prefix. */
	private static final String PREFIX_PROPERTY = "idservice.idPrefix";

	/**  The property key that determines how many retries are attempted after a failed service call. */
	private static final String MAX_RETRY_PROPERTY = "idservice.maxRetries";
	
	/**  The property key that defines any string of characters to be removed from the id generator output. */
	private static final String REPLACE_STRING_PROPERTY = "idservice.replaceString";

	/**  The property key that defines user name required for basic auth access. */
	private static final String USER_NAME_PROPERTY = "idservice.userName";
	
	/**  The property key that defines password required for basic auth access. */
	private static final String USER_PASSWORD_PROPERTY = "idservice.userPassword";
	
	/**  The property key to retrieve the ID length for validation. */
	private static final String ID_LENGTH_PROPERTY = "idservice.idLength";
	
	/**  The property key to retrieve a regex to validate the ID against validation. */
	private static final String ID_REGEX_PROPERTY = "idservice.idRegex";
	
	
	/**  Default ID prefix. */
	private static final String DEFAULT_PREFIX = "rmap:";
	
	/**  Default number of retries attempted after a failed service call. */
	private static final String DEFAULT_MAX_RETRY = "2";

	/**  Default ID length (-1 means no length defined). */
	private static final String DEFAULT_ID_LENGTH = "-1";
	
	/**  An instance of the HttpIdService. */
	private static HttpUrlIdService instance = new HttpUrlIdService();

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
		this(Constants.RMAPCORE_PROPFILE);
	}

	/**
	 * Instantiates a new ID service with properties.
	 *
	 * @param propertyFileName the property file name
	 */
	public HttpUrlIdService(String propertyFileName) {		
		Map<String, String> properties = new HashMap<String, String>();
		properties = ConfigUtils.getPropertyValues(propertyFileName);
		serviceUrl = properties.get(URL_PROPERTY);
		idPrefix = properties.getOrDefault(PREFIX_PROPERTY, DEFAULT_PREFIX);
		maxRetryAttempts = Integer.parseInt(properties.getOrDefault(MAX_RETRY_PROPERTY,DEFAULT_MAX_RETRY));
		replaceString = properties.get(REPLACE_STRING_PROPERTY);
		userName = properties.get(USER_NAME_PROPERTY);
		userPassword = properties.get(USER_PASSWORD_PROPERTY);
		idLength = Integer.parseInt(properties.getOrDefault(ID_LENGTH_PROPERTY, DEFAULT_ID_LENGTH));
		idRegex = properties.get(ID_REGEX_PROPERTY);
		
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
						log.error("UNSUCCESSFUL HTTP REQUEST TO NOID SERVICE and  HTTP RETURN CODE is : " + noidCon.getResponseCode());
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
			//WAIT FOR 10 SECS BEFORE RE-TRYING TO OVERCOME TEMPORARY NETWORK FAILURES
			//OR THE NOID SERVER BEING BUSY SERVICING ANOTHER REQUEST.
			if(shouldRetry){
				try{
					wait(10000);
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

	/**
	 * Gets the current instance of HttpUrlIdService.
	 *
	 * @return instance of HttpUrlIdService
	 */
	public static HttpUrlIdService getInstance() {
		return instance;
	}

	/**
	 * Sets the current HttpUrlIdService instance.
	 *
	 * @param instance the new HTTP URL service instance
	 */
	public static void setInstance(HttpUrlIdService instance) {
		HttpUrlIdService.instance = instance;
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.core.idservice.IdService#createId()
	 */
	public URI createId() throws Exception {
			try {
				return new URI(HttpUrlIdService.getInstance().getNoidId());
				// need to instead return configured per Component.getInstance("noidServiceImpl")
			} catch (Exception e) {
				throw new Exception("failed to get id from IdServiceImpl, caught exception", e);
			}
	}
}