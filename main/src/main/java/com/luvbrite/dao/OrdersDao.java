package com.luvbrite.dao;

import java.util.List;

import com.luvbrite.web.models.Order;

public interface OrdersDao {
	
	List<Order> findOrders(String orderBy);
	
	List<Order> findOrders(String orderBy, int limit, int offset);
	
	Order findOrderByNumber(long orderNumber);

}
