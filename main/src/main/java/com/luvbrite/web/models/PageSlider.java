package com.luvbrite.web.models;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity("pagesliders")
public class PageSlider {

	@Id
	private ObjectId _id;

	private String sliderName = "homepage";
	private boolean active = false;
	private SliderInfo sliderInfo;
	
	public ObjectId get_id() {
		return _id;
	}
	public void set_id(ObjectId _id) {
		this._id = _id;
	}
	public String getSliderName() {
		return sliderName;
	}
	public void setSliderName(String sliderName) {
		this.sliderName = sliderName;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public SliderInfo getSliderInfo() {
		return sliderInfo;
	}
	public void setSliderInfo(SliderInfo sliderInfo) {
		this.sliderInfo = sliderInfo;
	}
}
