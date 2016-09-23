package com.luvbrite.web.models;

import java.util.Date;
import org.mongodb.morphia.annotations.Embedded;

@Embedded
public class OrderDispatchInfo {
	
	private String driver = "";
	private Date dateFinished;
	private String comments = "";
	private long salesId = 0l;
	private String lockStatus = "";
	
	public String getDriver() {
		return driver;
	}
	public void setDriver(String driver) {
		this.driver = driver;
	}
	public Date getDateFinished() {
		return dateFinished;
	}
	public void setDateFinished(Date dateFinished) {
		this.dateFinished = dateFinished;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public long getSalesId() {
		return salesId;
	}
	public void setSalesId(long salesId) {
		this.salesId = salesId;
	}
	public String getLockStatus() {
		return lockStatus;
	}
	public void setLockStatus(String lockStatus) {
		this.lockStatus = lockStatus;
	}
}
