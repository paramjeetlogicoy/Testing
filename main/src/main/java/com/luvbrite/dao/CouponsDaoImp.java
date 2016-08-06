package com.luvbrite.dao;

import java.util.ArrayList;
import java.util.List;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.luvbrite.utils.Exceptions;
import com.luvbrite.web.models.Coupon;

@Service
public class CouponsDaoImp implements CouponsDao {

	private static Logger logger = Logger.getLogger(CouponsDaoImp.class);

	@Autowired
	private Datastore datastore;
	
	@Override
	public List<Coupon> findCoupons(String orderBy) {

		return findCoupons(orderBy, 0, 0); 	
	}

	@Override
	public List<Coupon> findCoupons(String orderBy, int limit, int offset) {

		List<Coupon> coupons = new ArrayList<Coupon>();

		
		try {
			
			final Query<Coupon> query = datastore.createQuery(Coupon.class).order(orderBy);
			
			if(limit != 0)
				query.limit(limit);
			
			query.offset(offset);
			
			coupons = query.asList();
			
		} catch (Exception e){
			logger.error(Exceptions.giveStackTrace(e));
		}
		
		return coupons;
	}

	@Override
	public Coupon findCouponByCode(String code) {

		Coupon coupon = new Coupon();
		
		try {
			
			coupon = datastore.find(Coupon.class, "_id", code).get();
			
		} catch (Exception e){
			logger.error(Exceptions.giveStackTrace(e));
		}
		
		return coupon;
	}

}
