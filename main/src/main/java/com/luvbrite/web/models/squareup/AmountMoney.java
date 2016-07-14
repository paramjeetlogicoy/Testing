package com.luvbrite.web.models.squareup;

public class AmountMoney {
	
	private long amount;
	private String currency = "USD";
	
	public AmountMoney() {
		super();
	}	
	public AmountMoney(long amount) {
		super();
		this.amount = amount;
	}
	public long getAmount() {
		return amount;
	}
	public void setAmount(long amount) {
		this.amount = amount;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
}
