package com.luvbrite.web.models.ordermeta.itemmeta;

public class Meta {
	
	private String key = "";
	private String label = "";
	private String value = "";
	
	public Meta() {
		super();
	}	
	public Meta(String key, String label, String value) {
		super();
		this.key = key;
		this.label = label;
		this.value = value;
	}	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
