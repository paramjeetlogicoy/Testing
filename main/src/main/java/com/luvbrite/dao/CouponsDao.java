package com.luvbrite.dao;

import java.util.List;

import com.luvbrite.web.models.Coupon;

public interface CouponsDao {
	
	List<Coupon> findCoupons(String orderBy);
	
	List<Coupon> findCoupons(String orderBy, int limit, int offset);
	
	Coupon findCouponByCode(String code);

}
