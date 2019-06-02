package com.luvbrite.web.models;

public class ProductAvailable {

private int product_id;
private int category_id;
private int strainid;
private String inv_productname;
private String strain_name;
private double total_purchase_qty;
private double total_packet_qty;
private double total_sold_qty;
private double total_remain_qty;
private double total_purchase_weight;
private double total_packed_weight;
private double total_sold_weight;
private double total_remain_weight;
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
}
