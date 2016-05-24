package com.luvbrite.dao;

import java.util.List;
import java.util.regex.Pattern;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.luvbrite.utils.SequenceGen;
import com.luvbrite.utils.Utility;
import com.luvbrite.web.models.Order;

@Service
public class OrderDAO extends BasicDAO<Order, Long> {

	@Autowired
	protected OrderDAO(Datastore ds) {
		super(ds);
	}
	
	private void setFilters(Query<Order> query, Pattern regExp, long customerId){
		
		if(customerId!=0){
			
			query.and(			
				query.criteria("customer._id").equal(customerId),
				query.criteria("lineItems.name").equal(regExp)
			);
			
		}
		else{
			query.or(
				query.criteria("customer.email").equal(regExp),
				query.criteria("customer.name").equal(regExp),
				query.criteria("billing.fname").equal(regExp),
				query.criteria("billing.lname").equal(regExp),
				query.criteria("lineItems.name").equal(regExp)
			);
		}
	}
	
	
	public long count(String search, long customerId){
		
		if(!search.equals("")) {

			Query<Order> query = getDs().createQuery(getEntityClass());
			
			if(search.trim().equals(Utility.getLong(search)+"")){//If the search is for a number.
				query.field("orderNumber").equal(Utility.getLong(search));
				
				if(customerId!=0){
					query.field("customer._id").equal(customerId);
				}
			}
			
			else {

				Pattern regExp = Pattern.compile(search + ".*", Pattern.CASE_INSENSITIVE);	
				setFilters(query, regExp, customerId);
			}
			
			return count(query);	
		}
		
		else if(customerId != 0){
			
			Query<Order> query = getDs().createQuery(getEntityClass());
			query.field("customer._id").equal(customerId);
			return count(query);	
		}
		
		else
			return super.count();
	}
	
	
	public List<Order> find(String orderBy, int limit, int offset, String search, long customerId){
		
		final Query<Order> query = getDs().createQuery(getEntityClass()).order(orderBy);		

		//Apply search criteria
		if(!search.equals("")){
			
			if(search.trim().equals(Utility.getLong(search)+"")){//If the search is for a number.
				query.field("orderNumber").equal(Utility.getLong(search));
				
				if(customerId!=0){
					query.field("customer._id").equal(customerId);
				}
			}
			
			else {

				Pattern regExp = Pattern.compile(search + ".*", Pattern.CASE_INSENSITIVE);	
				setFilters(query, regExp, customerId);
			}
		}
		else if(customerId!=0){
			query.field("customer._id").equal(customerId);
		}
		
		//Apply limit if applicable
		if(limit != 0) query.limit(limit);

		//Apply offset
		query.offset(offset);
		
		
		return query.asList();			
	}
	
	
	public long count(String search){
		return count(search, 0l);
	}
	
	
	public List<Order> find(String orderBy, int limit, int offset, String search){		
		return find(orderBy, limit, offset, search, 0l); 			
	}
	
	
	public long getNextSeq() {
		return SequenceGen.getNextSequence(getDs(), "orders__id");
	}
	
	
	public long getNextOrderNumber() {
		return SequenceGen.getNextSequence(getDs(), "orders_order_number");
	}

}
