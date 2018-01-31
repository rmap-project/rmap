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
package info.rmapproject.auth.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import info.rmapproject.auth.exception.RMapAuthException;
import info.rmapproject.auth.model.ApiKey;
import info.rmapproject.auth.model.User;

/**
 * Implementation of UserDao used to interact with data in the User table
 * @author khanson
 *
 */

@Repository("userDao")
public class UserDaoImpl implements UserDao {

	private static final Logger LOG = LoggerFactory.getLogger(UserDaoImpl.class);

	/**
	 * Data base session factory instance
	 */
    @Autowired
    private SessionFactory sessionFactory;
 	
	/* (non-Javadoc)
	 * @see info.rmapproject.auth.dao.UserDao#addUser(User)
	 */
    @Override
	public int addUser(User user) throws RMapAuthException {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(user);
        LOG.info("User record saved successfully, User Details={}", user);	
        return user.getUserId();
	}
    
	/* (non-Javadoc)
	 * @see info.rmapproject.auth.dao.UserDao#updateUser(User)
	 */
    @Override
	public void updateUser(User user) throws RMapAuthException {
        Session session = this.sessionFactory.getCurrentSession();
        session.update(user);
        LOG.info("User record updated successfully, User Details={}", user);
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.auth.dao.UserDao#getUserById(int)
	 */
    @Override
	public User getUserById(int userId) throws RMapAuthException {
        Session session = this.sessionFactory.getCurrentSession(); 
        User user = (User) session.get(User.class, userId);
        LOG.info("User record loaded, User Details={}", user);
        return user;
	}
    
	/* (non-Javadoc)
	 * @see info.rmapproject.auth.dao.UserDao#getUsers(String)
	 */
    @SuppressWarnings("unchecked")
	@Override
	public List<User> getUsers(String filter) throws RMapAuthException {
        Session session = this.sessionFactory.getCurrentSession();   
        Query<User> query;
        
        if (filter==null || filter.length()==0) {
        	query = session.createQuery("from User");
        } else {
        	//filter by userId, name, email, rmapAgentUri, or authKeyUri
    	    query = session.createQuery("from User "
											+ "where userId=:idFilter or lower(name) like :filter "
    	    								+ "or lower(email) like :filter "
    	    								+ "or lower(rmapAgentUri) like :filter "
    	    								+ "or lower(authKeyUri) like :filter");
    	    //convert to integer or null for userId filter
    	    Integer idFilter = CheckIfInt.tryParseInt(filter);
			query.setParameter("idFilter",idFilter);
			filter = "%" + filter.toLowerCase() + "%"; //surround with wildcards
			query.setParameter("filter",filter);
        }
        
		List <User> users = query.list();
		LOG.info("Users list loaded successfully");
        return users;
	}
    
	/* (non-Javadoc)
	 * @see info.rmapproject.auth.dao.UserDao#getUserByProviderAccount(String,String)
	 */
    @Override
	@SuppressWarnings("unchecked")
	public User getUserByProviderAccount(String idProvider, String providerAccountId) throws RMapAuthException{
		Session session = this.sessionFactory.getCurrentSession();   
	    Query<User> query = session.createNativeQuery("select Users.* from Users "
	        									+ "inner join UserIdentityProviders on UserIdentityProviders.userId = Users.userId "
	        									+ "where identityProvider=:idProvider and providerAccountId=:providerAccountId");
	    query.setParameter("idProvider",idProvider);
        query.setParameter("providerAccountId",providerAccountId);
    	
		List<User> users = query.list();
		if (users != null && !users.isEmpty()) {
			LOG.info("User list loaded successfully");
	        return users.get(0);
		}
		else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see info.rmapproject.auth.dao.UserDao#getUserByAuthKeyUri(String)
	 */
    @Override
    @SuppressWarnings("unchecked")
	public User getUserByAuthKeyUri(String authKeyUri) throws RMapAuthException {
        Session session = this.sessionFactory.getCurrentSession();   
        Query<User> query = session.createQuery("from User where authKeyUri=:authKeyUri");
        query.setParameter("authKeyUri",authKeyUri);
		List<User> users = query.list();
		if (users != null && !users.isEmpty()) {
			LOG.info("User loaded successfully");
			return users.get(0);
		}
		else	{
			return null;
		}
	}
    
	/* (non-Javadoc)
	 * @see info.rmapproject.auth.dao.UserDao#getUserByKeySecret(String,String)
	 */
    @Override
    @SuppressWarnings("unchecked")
	public User getUserByKeySecret(String key, String secret) throws RMapAuthException {
        Session session = this.sessionFactory.getCurrentSession();   
        Query<ApiKey> query = session.createQuery("from ApiKey where accessKey=:key and secret=:secret");
	    query.setParameter("key",key);
        query.setParameter("secret",secret);
        List<ApiKey> apiKeys = query.list();
        ApiKey apiKey = null;
		if (apiKeys != null && !apiKeys.isEmpty()) {
			LOG.info("Api key list loaded successfully");
			apiKey = apiKeys.get(0);
		}
		else	{
			return null;
		}		
		User user = null;
		if (apiKey!=null){
			int userId = apiKey.getUserId();
			user = this.getUserById(userId);
		}
		return user;		
	}

	private static class CheckIfInt {
		private static Integer tryParseInt(String text) {
			try { 
				return Integer.parseInt(text);
			} catch (NumberFormatException e) {
				return null;
			}
		}
	}
}
