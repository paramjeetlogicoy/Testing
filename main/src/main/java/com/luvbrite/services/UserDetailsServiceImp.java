package com.luvbrite.services;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.luvbrite.dao.UserDAO;
import com.luvbrite.web.models.User;
import com.luvbrite.web.models.UserDetailsExt;

@Service
public class UserDetailsServiceImp implements UserDetailsService {

	@Autowired
	private UserDAO dao;
	
	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException {

		User user = dao.findOne("username", username);
		
		String userRole = user.getRole();
		
		//System.out.println("UserDetailsServiceImp called");
		boolean enabled = false;
		if(user.isActive())
			enabled  = true;
		
		UserDetails userD = null;
		if(userRole!=null && userRole.equals("admin")){
			SimpleGrantedAuthority sa =  new SimpleGrantedAuthority("ROLE_ADMIN");
			userD = new UserDetailsExt(username, user.get_id(), user.getInvOpsId(), enabled, Arrays.asList(sa));
			
		} else if(userRole!=null && userRole.equals("customer")){
			SimpleGrantedAuthority sa =  new SimpleGrantedAuthority("ROLE_CUSTOMER");
			userD = new UserDetailsExt(username, user.get_id(), user.getInvOpsId(), enabled, Arrays.asList(sa));
			
		}
		
		return userD;
	}

}
