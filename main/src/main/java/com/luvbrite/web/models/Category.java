package com.luvbrite.web.models;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity("categories")
public class Category {

	@Id
	private long _id;
	
	private String name;
	private String url;
	
	@Property("desc")
	private String description;

	private long parent;
	
	private SeoElem seoElem;

	public long get_id() {
		return _id;
	}
	public void set_id(long _id) {
		this._id = _id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public long getParent() {
		return parent;
	}
	public void setParent(long parent) {
		this.parent = parent;
	}
	public SeoElem getSeoElem() {
		return seoElem;
	}
	public void setSeoElem(SeoElem seoElem) {
		this.seoElem = seoElem;
	}
}
