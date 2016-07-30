package com.luvbrite.web.models.ordermeta;

import java.util.List;

public class Order {
	private BillingAddress billing_address = null;
	private List<LineItem> line_items = null;
	
	private String completed_at = "";
	private String total = "";
	private String total_discount = "";
	private String note = "";
	private String delivery_notes = "";
	private String status = "";
	private String paymentMethod = "";
	private int order_number = 0;
	
	public BillingAddress getBilling_address() {
		return billing_address;
	}
	public void setBilling_address(BillingAddress billing_address) {
		this.billing_address = billing_address;
	}
	public List<LineItem> getLine_items() {
		return line_items;
	}
	public void setLine_items(List<LineItem> line_items) {
		this.line_items = line_items;
	}
	public String getCompleted_at() {
		return completed_at;
	}
	public void setCompleted_at(String completed_at) {
		this.completed_at = completed_at;
	}
	public String getTotal() {
		return total;
	}
	public void setTotal(String total) {
		this.total = total;
	}
	public String getTotal_discount() {
		return total_discount;
	}
	public void setTotal_discount(String total_discount) {
		this.total_discount = total_discount;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public String getDelivery_notes() {
		return delivery_notes;
	}
	public void setDelivery_notes(String delivery_notes) {
		this.delivery_notes = delivery_notes;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public int getOrder_number() {
		return order_number;
	}
	public void setOrder_number(int order_number) {
		this.order_number = order_number;
	}
	public String getPaymentMethod() {
		return paymentMethod;
	}
	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}	
}
