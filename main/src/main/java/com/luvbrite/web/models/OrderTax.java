package com.luvbrite.web.models;

import java.util.List;

import org.mongodb.morphia.annotations.Embedded;

@Embedded
public class OrderTax {

	private double applicableTax = 0d;
	private List<TaxComponent> taxComponents;
	
	public double getApplicableTax() {
		return applicableTax;
	}
	public void setApplicableTax(double applicableTax) {
		this.applicableTax = applicableTax;
	}
	public List<TaxComponent> getTaxComponents() {
		return taxComponents;
	}
	public void setTaxComponents(List<TaxComponent> taxComponents) {
		this.taxComponents = taxComponents;
	}
}
