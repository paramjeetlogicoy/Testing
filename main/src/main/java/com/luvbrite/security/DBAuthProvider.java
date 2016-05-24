package com.luvbrite.security;

import java.util.Arrays;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.luvbrite.utils.OldHashEncoder;
import com.luvbrite.web.models.User;
import com.luvbrite.web.models.UserDetailsExt;

@Service
public class DBAuthProvider extends AbstractUserDetailsAuthenticationProvider {

	@Autowired
	Datastore datastore;
	
	@Autowired
	PasswordEncoder encoder;

	@Override
	protected UserDetails retrieveUser(String username,
			UsernamePasswordAuthenticationToken authentication)
			throws AuthenticationException {
		
		UserDetails currUser;
		User dbUser = new User();
		
		 try {
				
			 //System.out.println("UserDetails - " + username + ":" + authentication.getCredentials());
				
			 Query<User> query = 
					 datastore.createQuery(User.class)
					 .field("username")
					 .equal(username)
					 .retrievedFields(true, "username", "password", "role");
			 
			 dbUser = query.get();
			 
			 if(dbUser != null){					
					
					String rawPassword = (String) authentication.getCredentials();
					String encodedPwd = dbUser.getPassword();
					
					if(encodedPwd.indexOf("$P$B")==0){
						OldHashEncoder ohe = new OldHashEncoder();
						if(ohe.isValid(username, rawPassword)){
							
							UpdateOperations<User> ops = 
									datastore.createUpdateOperations(User.class)
									.set("password", encoder.encode(rawPassword));
							
							datastore.update(query, ops);
							
						} else {
				            throw new 
				            InternalAuthenticationServiceException("Invalid username and/or password");							
						}						
					}
					else if(!encoder.matches(rawPassword, encodedPwd)){
			            
						throw new 
			            InternalAuthenticationServiceException("Invalid username and/or password");
						
						//password reset
					}

					String userRole = dbUser.getRole();
					if(userRole!=null && userRole.equals("admin")){
						SimpleGrantedAuthority sa =  new SimpleGrantedAuthority("ROLE_ADMIN");
						currUser = new UserDetailsExt(username, dbUser.get_id(), Arrays.asList(sa));
						
					} else if(userRole!=null && userRole.equals("customer")){
						SimpleGrantedAuthority sa =  new SimpleGrantedAuthority("ROLE_CUSTOMER");
						currUser = new UserDetailsExt(username, dbUser.get_id(), Arrays.asList(sa));
						
					} else {
			            throw new 
			            InternalAuthenticationServiceException("Not Authorized");							
					}
					
				}

				else {
		            throw new 
		            InternalAuthenticationServiceException("Not a valid user");
		        }
				
	        } catch (Exception repositoryProblem) {
	            throw new InternalAuthenticationServiceException(repositoryProblem.getMessage(), repositoryProblem);
	        }
		
		return currUser;
	}

	@Override
	protected void additionalAuthenticationChecks(UserDetails user,
			UsernamePasswordAuthenticationToken arg1)
			throws AuthenticationException {
	}

}
