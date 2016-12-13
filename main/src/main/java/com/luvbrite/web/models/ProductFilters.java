package com.luvbrite.web.models;

import org.mongodb.morphia.annotations.Embedded;

@Embedded
public class ProductFilters {

	private double price = 0d;
	private double thc = 0d;
	private double cbd = 0d;
	
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public double getThc() {
		return thc;
	}
	public void setThc(double thc) {
		this.thc = thc;
	}
	public double getCbd() {
		return cbd;
	}
	public void setCbd(double cbd) {
		this.cbd = cbd;
	}
}
