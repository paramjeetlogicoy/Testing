package com.luvbrite.web.models;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity("surveyresponses")
public class SurveyResponse {

	@Id
	private ObjectId _id;
	private long sid;
	private Date date; //surveyDate
	private String customer;
	private long orderNumber;
	private String otherRef;
	private List<SurveyDetails> details;
	
	public ObjectId get_id() {
		return _id;
	}
	public void set_id(ObjectId _id) {
		this._id = _id;
	}
	public long getSid() {
		return sid;
	}
	public void setSid(long sid) {
		this.sid = sid;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getCustomer() {
		return customer;
	}
	public void setCustomer(String customer) {
		this.customer = customer;
	}
	public long getOrderNumber() {
		return orderNumber;
	}
	public void setOrderNumber(long orderNumber) {
		this.orderNumber = orderNumber;
	}
	public String getOtherRef() {
		return otherRef;
	}
	public void setOtherRef(String otherRef) {
		this.otherRef = otherRef;
	}
	public List<SurveyDetails> getDetails() {
		return details;
	}
	public void setDetails(List<SurveyDetails> details) {
		this.details = details;
	}
}
