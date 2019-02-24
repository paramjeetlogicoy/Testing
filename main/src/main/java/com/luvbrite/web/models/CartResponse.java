package com.luvbrite.web.models;

import java.util.List;
import java.util.Map;

public class CartResponse {
	
	private CartOrder order;
	private User user;
	private ControlOptions config;
	private List<DoubleDownProducts> ddPrds;
	private Address prevOrderAddress;
	private Map<String, Boolean> availableDeals;
	private List<?> commonList;

	private List<DoubleDownProducts> bIAGIBPrds;
	private List<DoubleDownProducts> b2IG1IPrds;
	
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
	public List<DoubleDownProducts> getDdPrds() {
		return ddPrds;
	}
	public void setDdPrds(List<DoubleDownProducts> ddPrds) {
		this.ddPrds = ddPrds;
	}
	public Address getPrevOrderAddress() {
		return prevOrderAddress;
	}
	public void setPrevOrderAddress(Address prevOrderAddress) {
		this.prevOrderAddress = prevOrderAddress;
	}
	public Map<String, Boolean> getAvailableDeals() {
		return availableDeals;
	}
	public void setAvailableDeals(Map<String, Boolean> availableDeals) {
		this.availableDeals = availableDeals;
	}
	public List<?> getCommonList() {
		return commonList;
	}
	public void setCommonList(List<?> commonList) {
		this.commonList = commonList;
	}
	public List<DoubleDownProducts> getbIAGIBPrds() {
		return bIAGIBPrds;
	}
	public void setbIAGIBPrds(List<DoubleDownProducts> bIAGIBPrds) {
		this.bIAGIBPrds = bIAGIBPrds;
	}
	public List<DoubleDownProducts> getB2IG1IPrds() {
		return b2IG1IPrds;
	}
	public void setB2IG1IPrds(List<DoubleDownProducts> b2ig1iPrds) {
		b2IG1IPrds = b2ig1iPrds;
	}
}
