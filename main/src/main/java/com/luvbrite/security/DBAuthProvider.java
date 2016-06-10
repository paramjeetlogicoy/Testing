package com.luvbrite.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.luvbrite.dao.UserDAO;
import com.luvbrite.utils.OldHashEncoder;
import com.luvbrite.web.models.User;
import com.luvbrite.web.models.UserDetailsExt;

@Service
public class DBAuthProvider extends AbstractUserDetailsAuthenticationProvider {

	@Autowired
	private UserDAO dao;
	
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
			 
			 dbUser = dao.findOne("username", username);
			 
			 if(dbUser != null){					
					
					String rawPassword = (String) authentication.getCredentials();
					String encodedPwd = dbUser.getPassword();
					
					if(encodedPwd.indexOf("$P$B")==0){
						
						OldHashEncoder ohe = new OldHashEncoder();
						if(ohe.isValid(username, rawPassword)){							
							dbUser.setPassword(encoder.encode(rawPassword));
							dao.save(dbUser);
							
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
					
					

					boolean enabled = false;
					if(dbUser.isActive()) enabled = true;
					
					String userRole = dbUser.getRole();		
					List<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>();
					
					if(userRole!=null && enabled){
						if(userRole.equals("admin")){
							authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
							authorities.add(new SimpleGrantedAuthority("ROLE_EDIT"));
						}
						else if(userRole.equals("customer"))
							authorities.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
						
						else
							authorities.add(new SimpleGrantedAuthority("ROLE_NONE"));							
						
					}
					else
						authorities.add(new SimpleGrantedAuthority("ROLE_NONE"));
					
					currUser = new UserDetailsExt(username, dbUser.get_id(), enabled, authorities);
					
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
