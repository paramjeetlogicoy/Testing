package com.luvbrite.web.models.ordermeta;

public class OrderMain {
	
	private Order order = null;
	private String source = "luvbrite";

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
}
