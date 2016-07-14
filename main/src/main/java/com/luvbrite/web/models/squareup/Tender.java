package com.luvbrite.web.models.squareup;

import org.mongodb.morphia.annotations.Embedded;

@Embedded
public class Tender {

	private String id;
	private String location_id;
	private String transaction_id;
	private String created_at;
	private String note;
	private String type;
	private AmountMoney amount_money;
	private CardDetails card_details;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getLocation_id() {
		return location_id;
	}
	public void setLocation_id(String location_id) {
		this.location_id = location_id;
	}
	public String getTransaction_id() {
		return transaction_id;
	}
	public void setTransaction_id(String transaction_id) {
		this.transaction_id = transaction_id;
	}
	public String getCreated_at() {
		return created_at;
	}
	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public AmountMoney getAmount_money() {
		return amount_money;
	}
	public void setAmount_money(AmountMoney amount_money) {
		this.amount_money = amount_money;
	}
	public CardDetails getCard_details() {
		return card_details;
	}
	public void setCard_details(CardDetails card_details) {
		this.card_details = card_details;
	}
}
