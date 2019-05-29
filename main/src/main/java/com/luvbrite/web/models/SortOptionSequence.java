package com.luvbrite.web.models;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Embedded
public class SortOptionSequence {

	
	private Integer _id;
	
	private String sort_option;
	
	private Integer sequenceNumber;

	public Integer get_id() {
		return _id;
	}

	public void set_id(Integer _id) {
		this._id = _id;
	}

	public String getSort_option() {
		return sort_option;
	}

	public void setSort_option(String sort_option) {
		this.sort_option = sort_option;
	}

	public Integer getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(Integer sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}
	
	
}
