package com.luvbrite.web.models;

import org.mongodb.morphia.annotations.Embedded;

@Embedded
public class PaymentMethod {
	private String ip = "";
	private String method = "";
	private String type = "";
	private CardData cardData;
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public CardData getCardData() {
		return cardData;
	}
	public void setCardData(CardData cardData) {
		this.cardData = cardData;
	}
}
