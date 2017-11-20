package com.luvbrite.web.models;

import org.mongodb.morphia.annotations.Embedded;

@Embedded
public class SliderInfo {
	
	private String name;  //name of the slider
	private int order;    //the order in which slides should be displayed
	private boolean modal; //If the slider click action shows a modal
	private String modalHtml; //The HTML content for the modal, if above field is true
	private String linkUrl; //The URL to which the click should link, if it's not a modal.
	private String sliderImg; //Image that goes as the background of the slider
	private String sliderImgSM; //Image that will go as background for bigger view ports
	private String backgroundColor; //bg color for the slider
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	public boolean isModal() {
		return modal;
	}
	public void setModal(boolean modal) {
		this.modal = modal;
	}
	public String getModalHtml() {
		return modalHtml;
	}
	public void setModalHtml(String modalHtml) {
		this.modalHtml = modalHtml;
	}
	public String getLinkUrl() {
		return linkUrl;
	}
	public void setLinkUrl(String linkUrl) {
		this.linkUrl = linkUrl;
	}
	public String getSliderImg() {
		return sliderImg;
	}
	public void setSliderImg(String sliderImg) {
		this.sliderImg = sliderImg;
	}
	public String getSliderImgSM() {
		return sliderImgSM;
	}
	public void setSliderImgSM(String sliderImgSM) {
		this.sliderImgSM = sliderImgSM;
	}
	public String getBackgroundColor() {
		return backgroundColor;
	}
	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
}
