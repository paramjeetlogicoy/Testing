package com.luvbrite.web.models;

import java.util.List;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

@Embedded
public class OrderLineItem {
	
	private String type = "";
	private String name = "";
	
	private long itemId = 0;
	private int qty = 0;
	
	@Property("pid")
	private long productId = 0;
	
	@Property("vid")
	private long variationId = 0;
	
	private double cost = 0d;
	private double price = 0d;
	
	private boolean taxable = false;
	
	private String img;
	
	@Embedded
	private List<AttrValue> specs;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getItemId() {
		return itemId;
	}

	public void setItemId(long itemId) {
		this.itemId = itemId;
	}

	public int getQty() {
		return qty;
	}

	public void setQty(int qty) {
		this.qty = qty;
	}

	public long getProductId() {
		return productId;
	}

	public void setProductId(long productId) {
		this.productId = productId;
	}

	public long getVariationId() {
		return variationId;
	}

	public void setVariationId(long variationId) {
		this.variationId = variationId;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public boolean isTaxable() {
		return taxable;
	}

	public void setTaxable(boolean taxable) {
		this.taxable = taxable;
	}

	public List<AttrValue> getSpecs() {
		return specs;
	}

	public void setSpecs(List<AttrValue> specs) {
		this.specs = specs;
	}

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}
}
