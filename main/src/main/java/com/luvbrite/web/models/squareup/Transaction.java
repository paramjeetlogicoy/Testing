package com.luvbrite.web.models.squareup;

import java.util.List;

import org.mongodb.morphia.annotations.Embedded;

@Embedded
public class Transaction {
	
	private String id;
	private String location_id;
	private String created_at;
	private String reference_id;
	private String product;
	private List<Tender> tenders;
	
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
	public String getCreated_at() {
		return created_at;
	}
	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}
	public String getReference_id() {
		return reference_id;
	}
	public void setReference_id(String reference_id) {
		this.reference_id = reference_id;
	}
	public String getProduct() {
		return product;
	}
	public void setProduct(String product) {
		this.product = product;
	}
	public List<Tender> getTenders() {
		return tenders;
	}
	public void setTenders(List<Tender> tenders) {
		this.tenders = tenders;
	}
}
