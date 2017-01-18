package com.luvbrite.web.models;

/**
 * @author gm
 * This object defined different visual aspects of a product. Mostly images and videos.
 */
public class ProductVisuals {

	private String type; //image, video etc
	private String url; //image URL
	private String alt; //Alt text for image
	private String videoEmbed; //Embed HTML for this video
	private String thumbnail; // 'relative' URL for video thumbnail
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getAlt() {
		return alt;
	}
	public void setAlt(String alt) {
		this.alt = alt;
	}
	public String getVideoEmbed() {
		return videoEmbed;
	}
	public void setVideoEmbed(String videoEmbed) {
		this.videoEmbed = videoEmbed;
	}
	public String getThumbnail() {
		return thumbnail;
	}
	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}
}
