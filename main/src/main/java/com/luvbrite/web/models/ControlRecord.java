package com.luvbrite.web.models;

import java.util.List;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity("controlrecords")
public class ControlRecord {

	@Id
	private String _id;
	private String details;
	private List<AttrValue> params;
	
	public String get_id() {
		return _id;
	}
	public void set_id(String _id) {
		this._id = _id;
	}
	public String getDetails() {
		return details;
	}
	public void setDetails(String details) {
		this.details = details;
	}
	public List<AttrValue> getParams() {
		return params;
	}
	public void setParams(List<AttrValue> params) {
		this.params = params;
	}
}
