/*
 * Copyright 2018 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.rmapproject.core.idservice;

import edu.ucsb.nceas.ezid.EZIDClient;
import edu.ucsb.nceas.ezid.EZIDException;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class HttpArkIdService implements IdService {

    /** The log. */
    private static final Logger log = LoggerFactory.getLogger(HttpArkIdService.class);

    /**  Wait time to retry when ID retrieval unsuccessful (5 seconds). */
    private static final int RETRY_WAIT_TIME = 5000;

    /** The HTTP ID service url. */
    private String serviceUrl = "";

    /** The ID prefix to be added. */
    private String idPrefix = "";

    /** Set a maximum retry attempts value */
    private int maxRetryAttempts = 5;

    /** User name if using Basic Auth. */
    private String userName = "";

    /** User pwd if using Basic Auth. */
    private String userPassword = "";

    /** Length of ID to validate against. */
    private int idLength = -1;

    /** String regex to validate an ID against. */
    private String idRegex = "";

    /** File for persistent ID cache storage */
    private String idStoreFile;

    /** the string name for the cache map **/
    private static final String DATA = "ezidData";

    /** the MVMap for storing the reserved EZIDs **/
    private MVMap<Integer, String> ezids;

    /** the maximum number of IDs we would like to have on hand */
    private int maxStoreSize=200;

    /** indicates whether we have a process running which is replenishing the cache **/
    private boolean replenishingCache = false;


    /**
     * Instantiates a new ARK ID service.
     */
    public HttpArkIdService() {

    }

    /* (non-Javadoc)
     * @see info.rmapproject.core.idservice.IdService#isValidId(java.net.URI)
     */
    @Override
    public boolean isValidId(URI id) throws Exception {
        return isValidId(id.toASCIIString());
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

    /* (non-Javadoc)
     * @see info.rmapproject.core.idservice.IdService#createId()
     */
    public synchronized URI createId() throws Exception {
        MVStore mvs = MVStore.open(idStoreFile);
        ezids = mvs.openMap(DATA);

        try {
            return new URI(getEzid());
        } catch (Exception e) {
            throw new Exception("Failed to create a new ID.", e);
        } finally {
            mvs.close();
        }
    }

    private synchronized String getEzid() throws Exception {
        //if we are out of IDs, let's get some more from the EZID service endpoint
        if(ezids.size() < 1){
           if(!replenishingCache) {
               getMoreEzids();
           }
        }

        //let's make sure we have one to hand out. we can wait just a bit to let the previous
        //call fill up the cache a bit
        boolean shouldRetry;
        int retryCounter = 0;
        do{
            retryCounter++;
            shouldRetry = (retryCounter < maxRetryAttempts && ezids.size() == 0);
			//WAIT FOR 5 SECS BEFORE RE-TRYING TO OVERCOME TEMPORARY NETWORK FAILURES
			//OR THE EZID SERVER BEING BUSY SERVICING ANOTHER REQUEST.
			if(shouldRetry) {
                try {
                    Thread.sleep(RETRY_WAIT_TIME);
                } catch (InterruptedException ie) {
                    log.error("Wait interrupted in retry loop", ie);
                }
            }
        } while (shouldRetry);


        //find the first key for which we have an EZID
        Integer i = 1;
        while(!ezids.containsKey(i) && i <= maxStoreSize) {
            i++;
        }

        //failsafe - if we still don't have any, we throw an exception
        if(i>maxStoreSize){
            throw new RuntimeException("EZID cache is empty, unable to provide one");
        }

        //we take this id off of our cache and return it
        return ezids.remove(i);
    }

    private void getMoreEzids() {
        replenishingCache=true;
        EZIDClient client = new EZIDClient(serviceUrl);
        boolean shouldRetry;
        int retryCounter = 0;
		do {
            retryCounter++;
            log.debug("Minting ids from " + serviceUrl);
            try {
                client.login(userName, userPassword);
                for (Integer i = 1; !ezids.containsKey(i) && i <= maxStoreSize; i++) {
                    ezids.put(i, client.mintIdentifier(idPrefix,null));
                }
            } catch (EZIDException e) {//thrown by client.mintIdentifier()
                log.error("Could not mint EZID for shoulder " + idPrefix, e.getMessage());
            } finally {
                client.shutdown();
                replenishingCache = false;
            }

            shouldRetry = ((retryCounter < maxRetryAttempts) && (ezids.size() == 0));
			//WAIT FOR 5 SECS BEFORE RE-TRYING TO OVERCOME TEMPORARY NETWORK FAILURES
			//OR THE EZID SERVER BEING BUSY SERVICING ANOTHER REQUEST.
			if(shouldRetry){
				try{
					Thread.sleep(RETRY_WAIT_TIME);
				}catch(InterruptedException ie){
					log.error("Wait interrupted in retry loop", ie);
				}
            }

        } while (shouldRetry);
    }

    /**
     * Specifies the url for minting identifiers.  Can be configured using the {@code idservice.idMinterUrl}
     * property.  Example values:
     * <ul>
     *     <li>https://ezid.cdlib.org</li>
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
     *     <li>https://ezid.cdlib.org</li>
     * </ul>
     *
     * @param serviceUrl the url used to mint new identifiers
     */
    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    /**
     * Prefix for identifiers. Can be configured using the {@code idservice.idPrefix} property.  Example values:
     * <ul>
     *     <li>rmap:</li>
     *     <li>ark:/12345/</li>
     *     <li>ark:/12345/ab</li>
     * </ul>
     * <p>
     * If you are using an ARK ID service, for example, this would be {@code ark:/} followed by the Name Assigning
     * Authority Number (NAAN) e.g. "ark:/12345/" plus any shoulder for which the NAAN is authorized to create ids.
     * For ARK, see http://www.cdlib.org/uc3/naan_table.html for a registry of NAAN.
     * </p>
     *
     * @return the prefix added to generated identifiers, may be empty or {@code null}
     */
    public String getIdPrefix() {
        return idPrefix;
    }

    /**
     * Prefixe for identifiers. Can be configured using the {@code idservice.idPrefix} property.  Example values:
     * <ul>
     *     <li>rmap:</li>
     *     <li>ark:/12345/</li>
     *      <li>ark:/12345/ab</li>
     * </ul>
     * <p>
     * If you are using an ARK ID service, for example, this would be {@code ark:/} followed by the Name Assigning
     * Authority Number (NAAN) e.g. "ark:/12345/" plus any shoulder for which the NAAN is authorized to create ids.
     * For ARK, see http://www.cdlib.org/uc3/naan_table.html for a registry of NAAN.
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
     * #getIdPrefix() prefix} operations have been performed.
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
     * #getIdPrefix() prefix} operations have been performed.
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
     * #getIdPrefix() prefix} operations have been performed.
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
     * #getIdPrefix() prefix} operations have been performed.
     * This class uses the matching regex to verify that the identifier is viable.  Can be configured using the {@code
     * idservice.idRegex} property.
     *
     * @param idRegex the regex used to match the identifier, may be empty or {@code null}
     */
    public void setIdRegex(String idRegex) {
        this.idRegex = idRegex;
    }

    /**
     * An integer which gets the size of the cache for reserved EZIDs
     * @return MAX_STORE_SIZE
     */
    public int getMaxStoreSize() { return maxStoreSize; }

    /**
     * Sets the maximum size of the cache for reserved EZIDs
     * @param maxStoreSize
     */

    public void setMaxStoreSize(String maxStoreSize) { this.maxStoreSize = Integer.parseInt(maxStoreSize); }

    /**
     * A string which names the location of the id cache storage file
     */
    public String getIdStoreFile(){ return idStoreFile;}

    /**
     * Sets the location of the ID cache storage file
     */
    public void setIdStoreFile(String idStoreFile) { this.idStoreFile = idStoreFile;}
}
