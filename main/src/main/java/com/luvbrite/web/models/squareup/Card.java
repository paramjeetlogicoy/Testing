package com.luvbrite.web.models.squareup;

import org.mongodb.morphia.annotations.Embedded;

@Embedded
public class Card {

	private String card_brand;
	private String last_4;
	
	public String getCard_brand() {
		return card_brand;
	}
	public void setCard_brand(String card_brand) {
		this.card_brand = card_brand;
	}
	public String getLast_4() {
		return last_4;
	}
	public void setLast_4(String last_4) {
		this.last_4 = last_4;
	}		
}
