package com.luvbrite.web.models;

import java.util.Date;
import java.util.List;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity("products")
public class Product implements Comparable<Product> {
	
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
	private String status;  //['draft','publish','private','discontinued'];
	private String stockStat;
	private String featuredImg;
	private String editLock;
	private String priceRange;
	private int minStockLimit;
        
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
        public int getMinStockLimit() {
            return minStockLimit;
        }
        public void setMinStockLimit(int minStockLimit) {
           this.minStockLimit = minStockLimit;
        }

    @Override
    public String toString() {
        return "Product{" + "\n_id=" + _id + ", \n newBatchArrival=" + newBatchArrival + ", \nproduct_id=" + product_id + ", \ncategory_id=" + category_id + ", \nstrainid=" + strainid + ", \nstrain_name=" + strain_name + ", \ntotal_purchase_qty=" + total_purchase_qty + ", \ntotal_packet_qty=" + total_packet_qty + ", \ntotal_sold_qty=" + total_sold_qty + ", \ntotal_remain_qty=" + total_remain_qty + ", \ntotal_sold_weight=" + total_sold_weight + ", \ntotal_remain_weight=" + total_remain_weight + ", \nmongo_productid=" + mongo_productid + '}';
    }

   


/**************************************************************************************************************/
	private int product_id;
	private int category_id;
	private int strainid;

	private String strain_name;
	private double total_purchase_qty;
	private double total_packet_qty;
	private double total_sold_qty;
	private double total_remain_qty;
	private double total_purchase_weight;
	private double total_packed_weight;
	private double total_sold_weight;
	private double total_remain_weight;
	private String inv_productname;
	private Long mongo_productid;
   




	public String getInv_productname() {
		return inv_productname;
	}
	public void setInv_productname(String inv_productname) {
		this.inv_productname = inv_productname;
	}
	public Long getMongo_productid() {
		return mongo_productid;
	}
	public void setMongo_productid(Long mongo_productid) {
		this.mongo_productid = mongo_productid;
	}
	public void setTotal_remain_qty(double total_remain_qty) {
		this.total_remain_qty = total_remain_qty;
	}
	public void setTotal_remain_weight(double total_remain_weight) {
		this.total_remain_weight = total_remain_weight;
	}
	public int getProduct_id() {
		return product_id;
	}
	public void setProduct_id(int product_id) {
		this.product_id = product_id;
	}
	public int getCategory_id() {
		return category_id;
	}
	public void setCategory_id(int category_id) {
		this.category_id = category_id;
	}

	public String getStrain_name() {
		return strain_name;
	}
	public void setStrain_name(String strain_name) {
		this.strain_name = strain_name;
	}
	public double getTotal_purchase_qty() {
		return total_purchase_qty;
	}
	public void setTotal_purchase_qty(double total_purchase_qty) {
		this.total_purchase_qty = total_purchase_qty;
	}
	public double getTotal_packet_qty() {
		return total_packet_qty;
	}
	public void setTotal_packet_qty(double total_packet_qty) {
		this.total_packet_qty = total_packet_qty;
	}
	public double getTotal_sold_qty() {
		return total_sold_qty;
	}
	public void setTotal_sold_qty(double total_sold_qty) {
		this.total_sold_qty = total_sold_qty;
	}
	public double getTotal_remain_qty() {
		return total_remain_qty;
	}
	public void setTotal_remain_qty() {
		this.total_remain_qty = this.total_packet_qty-this.total_sold_qty;
	}
	public double getTotal_purchase_weight() {
		return total_purchase_weight;
	}
	public void setTotal_purchase_weight(double total_purchase_weight) {
		this.total_purchase_weight = total_purchase_weight;
	}
	public double getTotal_packed_weight() {
		return total_packed_weight;
	}
	public void setTotal_packed_weight(double total_packed_weight) {
		this.total_packed_weight = total_packed_weight;
	}
	public double getTotal_sold_weight() {
		return total_sold_weight;
	}
	public void setTotal_sold_weight(double total_sold_weight) {
		this.total_sold_weight = total_sold_weight;
	}
	public double getTotal_remain_weight() {
		return total_remain_weight;
	}

	public int getStrainid() {
		return strainid;
	}
	public void setStrainid(int strainid) {
		this.strainid = strainid;
	}

	public void setTotal_remain_weight() {
		this.total_remain_weight = this.total_packed_weight-this.total_sold_weight;
	}
/*******************************************************************************************************/
	@Override
	public int compareTo(Product o) {
		// TODO Auto-generated method stub
		
		Long id=this.get_id();
		Long id2= o.get_id();
	//	System.out.println("In COMPARETO=  "+id.compareTo(id2));
		return id.compareTo(id2);
	}

}
