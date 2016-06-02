package com.luvbrite.web.models;

import org.mongodb.morphia.annotations.Embedded;

@Embedded
public class UserMarketing {
	
	private String hearAboutUs;
	private boolean subscribe = false;

	public String getHearAboutUs() {
		return hearAboutUs;
	}

	public void setHearAboutUs(String hearAboutUs) {
		this.hearAboutUs = hearAboutUs;
	}

	public boolean isSubscribe() {
		return subscribe;
	}

	public void setSubscribe(boolean subscribe) {
		this.subscribe = subscribe;
	}
}
