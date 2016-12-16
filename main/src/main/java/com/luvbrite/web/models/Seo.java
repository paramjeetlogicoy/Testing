package com.luvbrite.web.models;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity("seo")
public class Seo {
	
	@Id
	private ObjectId _id;
	
	private String url = "";
	private String pageType = ""; //product, category, etc
	private SeoElem seoElem;
	
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
	public String getPageType() {
		return pageType;
	}
	public void setPageType(String pageType) {
		this.pageType = pageType;
	}
	public SeoElem getSeoElem() {
		return seoElem;
	}
	public void setSeoElem(SeoElem seoElem) {
		this.seoElem = seoElem;
	}
}
