package com.luvbrite.web.models;

import java.util.List;

import org.mongodb.morphia.annotations.Embedded;

@Embedded
public class SeoElem {
	
	private String title = "";
	private String description = "";	
	private List<String> keywords;
	private boolean nobots = false;
	private String mainImgAlt = "";
	
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
	public String getMainImgAlt() {
		return mainImgAlt;
	}
	public void setMainImgAlt(String mainImgAlt) {
		this.mainImgAlt = mainImgAlt;
	}
}
