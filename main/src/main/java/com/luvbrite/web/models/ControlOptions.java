package com.luvbrite.web.models;

import java.util.List;
import java.util.Map;

public class ControlOptions {

	private double doubleDown = 0d;
	private double doubleDownOfferValue = 0d;
	private double doubleDownItemPrice = 0d;
	private double orderMinimum = 0d;
	private List<Integer> doubleDownEligibleProducts;
	private Map<String, SliderObject> sliderObjs;
	private double caCannabisExciseTax = 0d;
	private double rushDeliveryCharge = 0d;
	

	private double buyItemAGetItemB = 0d;
	private double buyItemAGetItemBItemPrice = 0d;
	private List<Integer> buyItemAGetItemBEligProducts;
	private List<Integer> buyItemAGetItemBOffrProducts;
	
	private double buy2ItemsGet1Item = 0d;
	private double buy2ItemsGet1ItemItemPrice = 0d;
	private List<Integer> buy2ItemsGet1ItemEligProducts;
	private List<Integer> buy2ItemsGet1ItemBOffrProducts;
	
	public double getDoubleDown() {
		return doubleDown;
	}
	public void setDoubleDown(double doubleDown) {
		this.doubleDown = doubleDown;
	}
	public double getDoubleDownOfferValue() {
		return doubleDownOfferValue;
	}
	public void setDoubleDownOfferValue(double doubleDownOfferValue) {
		this.doubleDownOfferValue = doubleDownOfferValue;
	}
	public double getDoubleDownItemPrice() {
		return doubleDownItemPrice;
	}
	public void setDoubleDownItemPrice(double doubleDownItemPrice) {
		this.doubleDownItemPrice = doubleDownItemPrice;
	}
	public double getOrderMinimum() {
		return orderMinimum;
	}
	public void setOrderMinimum(double orderMinimum) {
		this.orderMinimum = orderMinimum;
	}
	public List<Integer> getDoubleDownEligibleProducts() {
		return doubleDownEligibleProducts;
	}
	public void setDoubleDownEligibleProducts(
			List<Integer> doubleDownEligibleProducts) {
		this.doubleDownEligibleProducts = doubleDownEligibleProducts;
	}



	private List<Integer> localZipcodes;
	private List<Integer> shippingZipcodes;

	public List<Integer> getLocalZipcodes() {
		return localZipcodes;
	}
	public void setLocalZipcodes(List<Integer> localZipcodes) {
		this.localZipcodes = localZipcodes;
	}
	public List<Integer> getShippingZipcodes() {
		return shippingZipcodes;
	}
	public void setShippingZipcodes(List<Integer> shippingZipcodes) {
		this.shippingZipcodes = shippingZipcodes;
	}
	

	private boolean dev = true;
	public boolean isDev() {
		return dev;
	}
	public void setDev(boolean dev) {
		this.dev = dev;
	}
	public Map<String, SliderObject> getSliderObjs() {
		return sliderObjs;
	}
	public void setSliderObjs(Map<String, SliderObject> sliderObjs) {
		this.sliderObjs = sliderObjs;
	}
	public double getCaCannabisExciseTax() {
		return caCannabisExciseTax;
	}
	public void setCaCannabisExciseTax(double caCannabisExciseTax) {
		this.caCannabisExciseTax = caCannabisExciseTax;
	}
	public double getRushDeliveryCharge() {
		return rushDeliveryCharge;
	}
	public void setRushDeliveryCharge(double rushDeliveryCharge) {
		this.rushDeliveryCharge = rushDeliveryCharge;
	}
	
	public double getBuyItemAGetItemB() {
		return buyItemAGetItemB;
	}
	public void setBuyItemAGetItemB(double buyItemAGetItemB) {
		this.buyItemAGetItemB = buyItemAGetItemB;
	}
	public double getBuyItemAGetItemBItemPrice() {
		return buyItemAGetItemBItemPrice;
	}
	public void setBuyItemAGetItemBItemPrice(double buyItemAGetItemBItemPrice) {
		this.buyItemAGetItemBItemPrice = buyItemAGetItemBItemPrice;
	}
	public List<Integer> getBuyItemAGetItemBEligProducts() {
		return buyItemAGetItemBEligProducts;
	}
	public void setBuyItemAGetItemBEligProducts(List<Integer> buyItemAGetItemBEligProducts) {
		this.buyItemAGetItemBEligProducts = buyItemAGetItemBEligProducts;
	}
	public List<Integer> getBuyItemAGetItemBOffrProducts() {
		return buyItemAGetItemBOffrProducts;
	}
	public void setBuyItemAGetItemBOffrProducts(List<Integer> buyItemAGetItemBOffrProducts) {
		this.buyItemAGetItemBOffrProducts = buyItemAGetItemBOffrProducts;
	}

	public double getBuy2ItemsGet1Item() {
		return buy2ItemsGet1Item;
	}
	public void setBuy2ItemsGet1Item(double buy2ItemsGet1Item) {
		this.buy2ItemsGet1Item = buy2ItemsGet1Item;
	}
	public double getBuy2ItemsGet1ItemItemPrice() {
		return buy2ItemsGet1ItemItemPrice;
	}
	public void setBuy2ItemsGet1ItemItemPrice(double buy2ItemsGet1ItemItemPrice) {
		this.buy2ItemsGet1ItemItemPrice = buy2ItemsGet1ItemItemPrice;
	}
	public List<Integer> getBuy2ItemsGet1ItemEligProducts() {
		return buy2ItemsGet1ItemEligProducts;
	}
	public void setBuy2ItemsGet1ItemEligProducts(List<Integer> buy2ItemsGet1ItemEligProducts) {
		this.buy2ItemsGet1ItemEligProducts = buy2ItemsGet1ItemEligProducts;
	}
	public List<Integer> getBuy2ItemsGet1ItemBOffrProducts() {
		return buy2ItemsGet1ItemBOffrProducts;
	}
	public void setBuy2ItemsGet1ItemBOffrProducts(List<Integer> buy2ItemsGet1ItemBOffrProducts) {
		this.buy2ItemsGet1ItemBOffrProducts = buy2ItemsGet1ItemBOffrProducts;
	}
}
