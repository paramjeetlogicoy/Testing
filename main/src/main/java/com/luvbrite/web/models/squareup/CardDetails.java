package com.luvbrite.web.models.squareup;

import org.mongodb.morphia.annotations.Embedded;

@Embedded
public class CardDetails {

	private String status;
	private String entry_method;
	private Card card;
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getEntry_method() {
		return entry_method;
	}
	public void setEntry_method(String entry_method) {
		this.entry_method = entry_method;
	}
	public Card getCard() {
		return card;
	}
	public void setCard(Card card) {
		this.card = card;
	}
}
