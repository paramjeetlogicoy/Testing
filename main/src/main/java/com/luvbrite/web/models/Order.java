package com.luvbrite.web.models;

import java.util.Date;
import java.util.List;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity("orders")
public class Order {

	private Billing billing;
	private Shipping shipping;
	private OrderTax orderTax;

	private List<OrderLineItem> lineItems;
	private OrderNotes notes;
	private OrderCustomer customer;
	
	@Id
	private long _id = 0l;
	private long orderNumber = 0l;
	
	private double subTotal = 0d;
	private double total = 0d;
	private double tax = 0d; //Applied tax (may be different from calculated tax)
	
	private String status = "";
	private String source = "";
	
	private Date date;
	
	public long get_id() {
		return _id;
	}
	public void set_id(long _id) {
		this._id = _id;
	}
	public Billing getBilling() {
		return billing;
	}
	public void setBilling(Billing billing) {
		this.billing = billing;
	}
	public List<OrderLineItem> getLineItems() {
		return lineItems;
	}
	public void setLineItems(List<OrderLineItem> lineItems) {
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
	public Shipping getShipping() {
		return shipping;
	}
	public void setShipping(Shipping shipping) {
		this.shipping = shipping;
	}
	public OrderTax getOrderTax() {
		return orderTax;
	}
	public void setOrderTax(OrderTax orderTax) {
		this.orderTax = orderTax;
	}
	public double getTax() {
		return tax;
	}
	public void setTax(double tax) {
		this.tax = tax;
	}
}
