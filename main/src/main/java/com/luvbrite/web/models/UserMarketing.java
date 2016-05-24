package com.luvbrite.web.models;

import org.mongodb.morphia.annotations.Embedded;

@Embedded
public class UserMarketing {
	
	private String hearAboutUs;

	public String getHearAboutUs() {
		return hearAboutUs;
	}

	public void setHearAboutUs(String hearAboutUs) {
		this.hearAboutUs = hearAboutUs;
	}
}
