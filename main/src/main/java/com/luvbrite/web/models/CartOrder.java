package com.luvbrite.web.models;

import java.util.Date;
import java.util.List;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity("cartorders")
public class CartOrder {

	private Address billing;
	private PaymentMethod pmtMethod;

	private List<OrderLineItemCart> lineItems;
	private OrderNotes notes;
	private OrderCustomer customer;
	
	@Id
	private long _id = 0l;
	private long orderNumber = 0l;
	
	private double subTotal = 0d;
	private double total = 0d;
	
	private String status = "";
	private String source = "";
	
	private Date date;
	
	public long get_id() {
		return _id;
	}
	public void set_id(long _id) {
		this._id = _id;
	}
	public Address getBilling() {
		return billing;
	}
	public void setBilling(Address billing) {
		this.billing = billing;
	}
	public PaymentMethod getPmtMethod() {
		return pmtMethod;
	}
	public void setPmtMethod(PaymentMethod pmtMethod) {
		this.pmtMethod = pmtMethod;
	}
	public List<OrderLineItemCart> getLineItems() {
		return lineItems;
	}
	public void setLineItems(List<OrderLineItemCart> lineItems) {
		this.lineItems = lineItems;
	}
	public OrderNotes getNotes() {
		return notes;
	}
	public void setNotes(OrderNotes notes) {
		this.notes = notes;
	}
	public long getOrderNumber() {
		return orderNumber;
	}
	public void setOrderNumber(long orderNumber) {
		this.orderNumber = orderNumber;
	}
	public double getSubTotal() {
		return subTotal;
	}
	public void setSubTotal(double subTotal) {
		this.subTotal = subTotal;
	}
	public double getTotal() {
		return total;
	}
	public void setTotal(double total) {
		this.total = total;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public OrderCustomer getCustomer() {
		return customer;
	}
	public void setCustomer(OrderCustomer customer) {
		this.customer = customer;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
}
