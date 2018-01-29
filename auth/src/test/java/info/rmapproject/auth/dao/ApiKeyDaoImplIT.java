package info.rmapproject.auth.dao;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import info.rmapproject.auth.model.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import info.rmapproject.auth.AuthDBAbstractIT;
import info.rmapproject.auth.model.ApiKey;
import info.rmapproject.auth.model.KeyStatus;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 * @author Karen Hanson
 */
public class ApiKeyDaoImplIT extends AuthDBAbstractIT {

	private final String USER_AUTH_KEY = "http://rmap-hub.org/authids/testauthid";

	@Autowired
    private ApiKeyDaoImpl apiKeyDao;

    @Autowired
	private UserDao userDao;

	@Test
    public void testAutowireApiKeyDao() throws Exception {
        assertNotNull(apiKeyDao);
    }
    
    @Test 
    public void testCreateApiKey() {
		User user = userDao.getUserByAuthKeyUri(USER_AUTH_KEY);

    	String accessKey = "abcd1234";
    	String secret = "efgh5678";
    	String keyUri = "rmap:testkey2";
    	String label = "test 'key'";
    	
    	ApiKey apiKey = new ApiKey();
    	apiKey.setAccessKey(accessKey);
    	apiKey.setSecret(secret);
    	apiKey.setIncludeInEvent(false);
    	apiKey.setKeyStatus(KeyStatus.ACTIVE);
    	apiKey.setKeyUri(keyUri);
    	apiKey.setLabel(label);
		apiKey.setUserId(user.getUserId());
    	
    	//create and retain key
    	int keyId = apiKeyDao.addApiKey(apiKey);
    	
    	//get new key record
    	ApiKey newApiKey = apiKeyDao.getApiKeyById(keyId);    	
    	assertEquals(newApiKey.getUserId(), user.getUserId());
    	assertEquals(newApiKey.getApiKeyId(),keyId);
    	assertEquals(newApiKey.getKeyUri(), keyUri);
    	assertEquals(newApiKey.getLabel(), label);
    	assertEquals(newApiKey.getAccessKey(),accessKey);
    	assertEquals(newApiKey.getSecret(), secret);
    	assertEquals(newApiKey.isIncludeInEvent(),false);
    	assertNotNull(newApiKey.getCreatedDate());
    	assertNotNull(newApiKey.getLastModifiedDate());
    }
    
}