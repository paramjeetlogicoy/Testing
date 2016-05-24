package com.luvbrite.web.models;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

@Embedded
public class OrderNotes {
	
	@Property("additional")
	private String additonalNotes = "";
	
	@Property("delivery")
	private String deliveryNotes = "";
	
	public String getAdditonalNotes() {
		return additonalNotes;
	}
	public void setAdditonalNotes(String additonalNotes) {
		this.additonalNotes = additonalNotes;
	}
	public String getDeliveryNotes() {
		return deliveryNotes;
	}
	public void setDeliveryNotes(String deliveryNotes) {
		this.deliveryNotes = deliveryNotes;
	}
}
