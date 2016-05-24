package com.luvbrite.dao;

import java.util.ArrayList;
import java.util.List;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.luvbrite.utils.Exceptions;
import com.luvbrite.web.models.Order;

@Service
public class OrdersDaoImp implements OrdersDao {

	private static Logger logger = LoggerFactory.getLogger(OrdersDaoImp.class);
	
	@Autowired
	private Datastore datastore;
	

	@Override
	public List<Order> findOrders(String orderBy, int limit, int offset) {
		
		List<Order> orders = new ArrayList<Order>();
		
		try {
			
			final Query<Order> query = datastore.createQuery(Order.class).order(orderBy);
			
			if(limit != 0)
				query.limit(limit);
			
			query.offset(offset);
			
			orders = query.asList();
			
		} catch (Exception e){
			logger.error(Exceptions.giveStackTrace(e));
		}		
		
		return orders;
	}

	@Override
	public Order findOrderByNumber(long orderNumber) {
		
		Order order = new Order();
		
		try {
			
			order = datastore.find(Order.class, "orderNumber", orderNumber).get();
			
		} catch (Exception e){
			logger.error(Exceptions.giveStackTrace(e));
		}
		
		return order;
	}

	@Override
	public List<Order> findOrders(String orderBy) {
		
		return findOrders(orderBy, 0, 0);
	}

}
