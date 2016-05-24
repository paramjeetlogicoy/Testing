package com.luvbrite.web.models;

import org.mongodb.morphia.annotations.Embedded;

@Embedded
public class AttrValue {
	
	private String attr;
	private String value;
	
	public String getAttr() {
		return attr;
	}
	public void setAttr(String attr) {
		this.attr = attr;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
