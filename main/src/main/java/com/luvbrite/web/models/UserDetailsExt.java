package com.luvbrite.web.models;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class UserDetailsExt extends User {

	private static final long serialVersionUID = 1313840688691601158L;

	private long id;
	private int invOpsId;
	
	public UserDetailsExt(String username, String password, boolean enabled,
			boolean accountNonExpired, boolean credentialsNonExpired,
			boolean accountNonLocked,
			Collection<? extends GrantedAuthority> authorities) {
		super(username, password, enabled, accountNonExpired, credentialsNonExpired,
				accountNonLocked, authorities);
	}
	
	public UserDetailsExt(String username, long id, int invOpsId, boolean enabled, 
			Collection<? extends GrantedAuthority> authorities) {
		
		super(username, "", enabled, true, true, true, authorities);
		this.id = id;
		this.invOpsId = invOpsId;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getInvOpsId() {
		return invOpsId;
	}

	public void setInvOpsId(int invOpsId) {
		this.invOpsId = invOpsId;
	}
}
