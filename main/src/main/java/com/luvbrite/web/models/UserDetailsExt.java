package com.luvbrite.web.models;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class UserDetailsExt extends User {

	private static final long serialVersionUID = 1313840688691601158L;

	private long id;
	
	public UserDetailsExt(String username, String password, boolean enabled,
			boolean accountNonExpired, boolean credentialsNonExpired,
			boolean accountNonLocked,
			Collection<? extends GrantedAuthority> authorities) {
		super(username, password, enabled, accountNonExpired, credentialsNonExpired,
				accountNonLocked, authorities);
	}
	
	public UserDetailsExt(String username, long id,
			Collection<? extends GrantedAuthority> authorities) {
		
		super(username, "", authorities);
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
}
