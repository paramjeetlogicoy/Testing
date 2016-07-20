package com.luvbrite.web.models;

public class CartResponse {
	
	private CartOrder order;
	private User user;
	private ControlOptions config;
	
	public CartOrder getOrder() {
		return order;
	}
	public void setOrder(CartOrder order) {
		this.order = order;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public ControlOptions getConfig() {
		return config;
	}
	public void setConfig(ControlOptions config) {
		this.config = config;
	}
}
