package com.luvbrite.dao;

import java.util.List;
import java.util.regex.Pattern;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.luvbrite.web.models.Coupon;

@Service
public class CouponDAO extends BasicDAO<Coupon, String> {

	@Autowired
	protected CouponDAO(Datastore ds) {
		super(ds);
	}
	
	private void setFilters(Query<Coupon> query, Pattern regExp){
		query.or(
				query.criteria("_id").equal(regExp),
				query.criteria("emails").equal(regExp)
				);
	}
	
	
	public long count(String search){
		
		if(!search.equals("")) {

			Query<Coupon> query = getDs().createQuery(getEntityClass());
			
			//Apply search criteria
			if(!search.equals("")){
				Pattern regExp = Pattern.compile(search + ".*", Pattern.CASE_INSENSITIVE);	
				setFilters(query, regExp);
			}
			
			return count(query);	
		}
		
		else
			return super.count();
	}
	
	
	public List<Coupon> find(String orderBy, int limit, int offset, String search){
		
		final Query<Coupon> query = getDs().createQuery(getEntityClass()).order(orderBy);

		//Apply search criteria
		if(!search.equals("")){
			Pattern regExp = Pattern.compile(search + ".*", Pattern.CASE_INSENSITIVE);	
			setFilters(query, regExp);
		}

		//Apply limit if applicable
		if(limit != 0) query.limit(limit);

		//Apply offset
		query.offset(offset);

		return query.asList();			
	}
}
