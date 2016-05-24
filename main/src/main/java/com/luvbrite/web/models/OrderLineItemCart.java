package com.luvbrite.web.models;

import org.mongodb.morphia.annotations.Embedded;

@Embedded
public class OrderLineItemCart extends OrderLineItem {
	
	private boolean instock = true;

	public boolean isInstock() {
		return instock;
	}

	public void setInstock(boolean instock) {
		this.instock = instock;
	}
}
