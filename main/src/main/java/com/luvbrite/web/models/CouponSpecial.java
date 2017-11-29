package com.luvbrite.web.models;

import java.util.List;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity("coupon_specials")
public class CouponSpecial {

	@Id
	private String couponCode;
	private OrderLineItemCart olic;
	private List<Long> validProductIds;
	
	public String getCouponCode() {
		return couponCode;
	}
	public void setCouponCode(String couponCode) {
		this.couponCode = couponCode;
	}
	public OrderLineItemCart getOlic() {
		return olic;
	}
	public void setOlic(OrderLineItemCart olic) {
		this.olic = olic;
	}
	public List<Long> getValidProductIds() {
		return validProductIds;
	}
	public void setValidProductIds(List<Long> validProductIds) {
		this.validProductIds = validProductIds;
	}
}
