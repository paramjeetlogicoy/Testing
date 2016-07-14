package com.luvbrite.web.models;

import org.mongodb.morphia.annotations.Embedded;

@Embedded
public class CardData {

	private String card_brand = "";
	private String last_4 = "";
	private String exp_month = "";
	private String exp_year = "";
	private String billing_postal_code = "";
	private String nonce = "";
	
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
	public String getExp_month() {
		return exp_month;
	}
	public void setExp_month(String exp_month) {
		this.exp_month = exp_month;
	}
	public String getExp_year() {
		return exp_year;
	}
	public void setExp_year(String exp_year) {
		this.exp_year = exp_year;
	}
	public String getBilling_postal_code() {
		return billing_postal_code;
	}
	public void setBilling_postal_code(String billing_postal_code) {
		this.billing_postal_code = billing_postal_code;
	}
	public String getNonce() {
		return nonce;
	}
	public void setNonce(String nonce) {
		this.nonce = nonce;
	}
}
