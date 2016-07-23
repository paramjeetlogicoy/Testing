package com.luvbrite.web.models;

import java.util.List;

public class CartResponse {
	
	private CartOrder order;
	private User user;
	private ControlOptions config;
	private List<Product> ddPrds;
	
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
	public List<Product> getDdPrds() {
		return ddPrds;
	}
	public void setDdPrds(List<Product> ddPrds) {
		this.ddPrds = ddPrds;
	}
}
