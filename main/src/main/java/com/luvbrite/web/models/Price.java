package com.luvbrite.web.models;

import java.util.List;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity("prices")
public class Price {
	
	@Id
	private long _id;
	
	@Property("vid")
	private long variationId;

	@Property("pid")
	private long productId;
	
	private List<AttrValue> variation;
	
	private int stockCount;
	
	private double regPrice;
	private double salePrice;
	
	private String stockStat;
	private String img;
	
	public long get_id() {
		return _id;
	}
	public void set_id(long _id) {
		this._id = _id;
	}
	public long getVariationId() {
		return variationId;
	}
	public void setVariationId(long variationId) {
		this.variationId = variationId;
	}
	public long getProductId() {
		return productId;
	}
	public void setProductId(long productId) {
		this.productId = productId;
	}
	public List<AttrValue> getVariation() {
		return variation;
	}
	public void setVariation(List<AttrValue> variation) {
		this.variation = variation;
	}
	public int getStockCount() {
		return stockCount;
	}
	public void setStockCount(int stockCount) {
		this.stockCount = stockCount;
	}
	public double getRegPrice() {
		return regPrice;
	}
	public void setRegPrice(double regPrice) {
		this.regPrice = regPrice;
	}
	public double getSalePrice() {
		return salePrice;
	}
	public void setSalePrice(double salePrice) {
		this.salePrice = salePrice;
	}
	public String getStockStat() {
		return stockStat;
	}
	public void setStockStat(String stockStat) {
		this.stockStat = stockStat;
	}
	public String getImg() {
		return img;
	}
	public void setImg(String img) {
		this.img = img;
	}
}
