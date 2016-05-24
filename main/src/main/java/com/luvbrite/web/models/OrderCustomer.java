package com.luvbrite.web.models;

import org.mongodb.morphia.annotations.Embedded;


@Embedded
public class OrderCustomer {
	
	private long _id = 0;
	private String email = "";
	private String name = "";
	
	public long get_id() {
		return _id;
	}
	
	public void set_id(long _id) {
		this._id = _id;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

}
