package com.luvbrite.web.models;

import java.util.Date;
import java.util.List;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity("products")
public class Product {
	
	@Id
	private long _id;
	
	//The ID of user who last edited this.
	private int editedLast;
	private int stock;
	private int reviewCount;
	
	private double price;	
	private double salePrice;
	private int rating = -1;
	
	private String name;
	private String description;
	private String url;
	private String status;
	private String stockStat;
	private String featuredImg;
	private String editLock;
	private String priceRange;
	
	private boolean variation;
	
	private Date dateCreated;
	
	private List<ProductAttr> attrs;
	private List<AttrValue> defaultAttr;
	private List<String> categories;
	private List<Integer> rps; /*Related product ids*/
	
	private ProductFilters productFilters;
	private SeoElem seoElem;
	private List<ProductVisuals> prdVisuals; /*Additional Imgs and Videos*/
	
	private Date newBatchArrival;
	
	public long get_id() {
		return _id;
	}
	public void set_id(long _id) {
		this._id = _id;
	}
	public int getEditedLast() {
		return editedLast;
	}
	public void setEditedLast(int editedLast) {
		this.editedLast = editedLast;
	}
	public int getStock() {
		return stock;
	}
	public void setStock(int stock) {
		this.stock = stock;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public double getSalePrice() {
		return salePrice;
	}
	public void setSalePrice(double salePrice) {
		this.salePrice = salePrice;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getStockStat() {
		return stockStat;
	}
	public void setStockStat(String stockStat) {
		this.stockStat = stockStat;
	}
	public String getFeaturedImg() {
		return featuredImg;
	}
	public void setFeaturedImg(String featuredImg) {
		this.featuredImg = featuredImg;
	}
	public String getEditLock() {
		return editLock;
	}
	public void setEditLock(String editLock) {
		this.editLock = editLock;
	}
	public boolean isVariation() {
		return variation;
	}
	public void setVariation(boolean variation) {
		this.variation = variation;
	}
	public Date getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	public List<ProductAttr> getAttrs() {
		return attrs;
	}
	public void setAttrs(List<ProductAttr> attrs) {
		this.attrs = attrs;
	}
	public List<AttrValue> getDefaultAttr() {
		return defaultAttr;
	}
	public void setDefaultAttr(List<AttrValue> defaultAttr) {
		this.defaultAttr = defaultAttr;
	}
	public String getPriceRange() {
		return priceRange;
	}
	public void setPriceRange(String priceRange) {
		this.priceRange = priceRange;
	}
	public List<String> getCategories() {
		return categories;
	}
	public void setCategories(List<String> categories) {
		this.categories = categories;
	}
	public List<Integer> getRps() {
		return rps;
	}
	public void setRps(List<Integer> rps) {
		this.rps = rps;
	}
	public int getRating() {
		return rating;
	}
	public void setRating(int rating) {
		this.rating = rating;
	}
	public int getReviewCount() {
		return reviewCount;
	}
	public void setReviewCount(int reviewCount) {
		this.reviewCount = reviewCount;
	}
	public ProductFilters getProductFilters() {
		return productFilters;
	}
	public void setProductFilters(ProductFilters productFilters) {
		this.productFilters = productFilters;
	}
	public SeoElem getSeoElem() {
		return seoElem;
	}
	public void setSeoElem(SeoElem seoElem) {
		this.seoElem = seoElem;
	}
	public List<ProductVisuals> getPrdVisuals() {
		return prdVisuals;
	}
	public void setPrdVisuals(List<ProductVisuals> prdVisuals) {
		this.prdVisuals = prdVisuals;
	}
	public Date getNewBatchArrival() {
		return newBatchArrival;
	}
	public void setNewBatchArrival(Date newBatchArrival) {
		this.newBatchArrival = newBatchArrival;
	}
}
