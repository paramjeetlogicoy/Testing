package com.luvbrite.web.models.ordermeta;

import java.util.List;

import com.luvbrite.web.models.ordermeta.itemmeta.Meta;

public class LineItem {
	
	private int id = 0;
	private int product_id = 0;
	private int quantity = 0;
	private List<Meta> meta = null;
	
	private String name = "";
	private String total = "";
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getProduct_id() {
		return product_id;
	}
	public void setProduct_id(int product_id) {
		this.product_id = product_id;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public List<Meta> getMeta() {
		return meta;
	}
	public void setMeta(List<Meta> meta) {
		this.meta = meta;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTotal() {
		return total;
	}
	public void setTotal(String total) {
		this.total = total;
	}
}
