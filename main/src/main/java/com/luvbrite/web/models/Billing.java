package com.luvbrite.web.models;

import org.mongodb.morphia.annotations.Embedded;

@Embedded
public class Billing {

	private PaymentMethod pmtMethod;	
	private Address address;
	
	public PaymentMethod getPmtMethod() {
		return pmtMethod;
	}
	public void setPmtMethod(PaymentMethod pmtMethod) {
		this.pmtMethod = pmtMethod;
	}
	public Address getAddress() {
		return address;
	}
	public void setAddress(Address address) {
		this.address = address;
	}
}
