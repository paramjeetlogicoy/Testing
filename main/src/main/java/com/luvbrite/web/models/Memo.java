package com.luvbrite.web.models;

import java.util.Date;

import org.mongodb.morphia.annotations.Embedded;

@Embedded
public class Memo {

	private String memo;
	private Date date;
	private String user;
	
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
}
