package com.luvbrite.web.models;

import java.util.Date;
import java.util.List;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity("coupons")
public class Coupon {

	@Id
	private String _id; //Coupon Code
	private String description;
	private String type;   //F fixed, R percentage rate, PO offers
	
	private Date expiry;
	
	private int usageCount;
	private int maxUsageCount;
	
	@Property("amt")
	private double couponValue;
	
	private boolean active;
	
	private List<String> emails;
	private List<Long> pids;
	private double minAmt; //Order total should be greater than this value for the coupon to work
	private double maxDiscAmt; //Max discount that can be given for this discount.
	

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Date getExpiry() {
		return expiry;
	}

	public void setExpiry(Date expiry) {
		this.expiry = expiry;
	}

	public int getUsageCount() {
		return usageCount;
	}

	public void setUsageCount(int usageCount) {
		this.usageCount = usageCount;
	}

	public int getMaxUsageCount() {
		return maxUsageCount;
	}

	public void setMaxUsageCount(int maxUsageCount) {
		this.maxUsageCount = maxUsageCount;
	}

	public double getCouponValue() {
		return couponValue;
	}

	public void setCouponValue(double couponValue) {
		this.couponValue = couponValue;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public List<String> getEmails() {
		return emails;
	}

	public void setEmails(List<String> emails) {
		this.emails = emails;
	}

	public List<Long> getPids() {
		return pids;
	}

	public void setPids(List<Long> pids) {
		this.pids = pids;
	}

	public double getMinAmt() {
		return minAmt;
	}

	public void setMinAmt(double minAmt) {
		this.minAmt = minAmt;
	}

	public double getMaxDiscAmt() {
		return maxDiscAmt;
	}

	public void setMaxDiscAmt(double maxDiscAmt) {
		this.maxDiscAmt = maxDiscAmt;
	}
}
