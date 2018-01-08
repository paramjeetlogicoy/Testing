package com.luvbrite.web.models;

import org.mongodb.morphia.annotations.Embedded;

@Embedded
public class Shipping {
	
	private String deliveryMethod;
	private double shippingCharge = 0d;
	private Address address;
	private boolean rushDelivery = false;
	private double rushFee = 0d;
	
	public String getDeliveryMethod() {
		return deliveryMethod;
	}
	public void setDeliveryMethod(String deliveryMethod) {
		this.deliveryMethod = deliveryMethod;
	}
	public double getShippingCharge() {
		return shippingCharge;
	}
	public void setShippingCharge(double shippingCharge) {
		this.shippingCharge = shippingCharge;
	}
	public Address getAddress() {
		return address;
	}
	public void setAddress(Address address) {
		this.address = address;
	}
	public boolean isRushDelivery() {
		return rushDelivery;
	}
	public void setRushDelivery(boolean rushDelivery) {
		this.rushDelivery = rushDelivery;
	}
	public double getRushFee() {
		return rushFee;
	}
	public void setRushFee(double rushFee) {
		this.rushFee = rushFee;
	}
}
