package com.luvbrite.web.models;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity("seo")
public class Seo {
	
	@Id
	private ObjectId _id;
	
	private String url = "";
	private String title = "";
	private String description = "";	
	private List<String> keywords;
	private boolean nobots = false;
	
	public String get_id() {
		return _id.toString();
	}
	public void set_id(ObjectId _id) {
		this._id = _id;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public List<String> getKeywords() {
		return keywords;
	}
	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}
	public boolean isNobots() {
		return nobots;
	}
	public void setNobots(boolean nobots) {
		this.nobots = nobots;
	}
}
