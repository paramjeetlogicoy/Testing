package com.luvbrite.web.models;

public class DoubleDownProducts {
	

	private long _id;
	private long vid;
	
	private String name;
	private String url;
	private String featuredImg;
	
	public long get_id() {
		return _id;
	}
	public void set_id(long _id) {
		this._id = _id;
	}
	public long getVid() {
		return vid;
	}
	public void setVid(long vid) {
		this.vid = vid;
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
	public String getFeaturedImg() {
		return featuredImg;
	}
	public void setFeaturedImg(String featuredImg) {
		this.featuredImg = featuredImg;
	}
}
