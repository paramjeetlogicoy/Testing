package com.luvbrite.web.models;

import java.util.List;

public class RecoExpiry {
	
	private List<User> expiring;
	private List<User> expired;
	private List<User> invalid;
	
	public List<User> getExpiring() {
		return expiring;
	}
	public void setExpiring(List<User> expiring) {
		this.expiring = expiring;
	}
	public List<User> getExpired() {
		return expired;
	}
	public void setExpired(List<User> expired) {
		this.expired = expired;
	}
	public List<User> getInvalid() {
		return invalid;
	}
	public void setInvalid(List<User> invalid) {
		this.invalid = invalid;
	}
}
