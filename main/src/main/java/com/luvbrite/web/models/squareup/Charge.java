package com.luvbrite.web.models.squareup;

public class Charge {

	private String idempotency_key;
	private AmountMoney amount_money;
	private String card_nonce;
	private boolean delay_capture;
	private String reference_id;
	public String getIdempotency_key() {
		return idempotency_key;
	}
	public void setIdempotency_key(String idempotency_key) {
		this.idempotency_key = idempotency_key;
	}
	public AmountMoney getAmount_money() {
		return amount_money;
	}
	public void setAmount_money(AmountMoney amount_money) {
		this.amount_money = amount_money;
	}
	public String getCard_nonce() {
		return card_nonce;
	}
	public void setCard_nonce(String card_nonce) {
		this.card_nonce = card_nonce;
	}
	public boolean isDelay_capture() {
		return delay_capture;
	}
	public void setDelay_capture(boolean delay_capture) {
		this.delay_capture = delay_capture;
	}
	public String getReference_id() {
		return reference_id;
	}
	public void setReference_id(String reference_id) {
		this.reference_id = reference_id;
	}
}
