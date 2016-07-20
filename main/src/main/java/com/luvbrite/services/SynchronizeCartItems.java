package com.luvbrite.services;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.mongodb.morphia.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.luvbrite.dao.CartOrderDAO;
import com.luvbrite.dao.LogDAO;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.web.models.CartOrder;
import com.luvbrite.web.models.Log;
import com.luvbrite.web.models.OrderLineItemCart;
import com.luvbrite.web.models.Product;

/**
 * @author gautam
 *	
 *Whenever there is a change in the product, go through the items in the cart
 *and make sure they are in sync with the new product attributes
 *This is to take care of changes like 
 *	- product becomes out-of-stock
 *	- product/product-varation price changes
 *	- product-variation removed/changed/out-of-stock
 *
 */
@Service
public class SynchronizeCartItems {
	
	private static Logger logger = LoggerFactory.getLogger(SynchronizeCartItems.class);
	
	@Autowired
	private CartOrderDAO cartDao;
	
	@Autowired
	private CartLogics cartLogics;
	
	@Autowired
	private CouponManager couponManager;
	
	@Autowired
	private LogDAO logDao;
	
	public String sync(Product product){
		
		String response = "";
		
		int updateCounter = 0,
				processCounter = 0;
		
		try {
			
			if(product!=null){
				
				long productId = product.get_id();
				boolean productOutOfStock = false,
						variableProduct = false;
				
				/*If product is outofstock*/
				if(product.getStockStat().equals("outofstock")) productOutOfStock = true;				

				/*Variable Product*/
				if(product.isVariation()) 						variableProduct = true;


				Query<CartOrder> q = cartDao.createQuery();
				q.filter("lineItems.pid", productId);
				
				List<CartOrder> cos = q.asList();
				for(CartOrder co : cos){
					
					processCounter++;
					
					List<OrderLineItemCart> lis = co.getLineItems();
					if(lis != null){
						boolean itemChanged = false;
						String couponCode = "";
						StringBuilder logDetails = new StringBuilder().append("Product changed detected. ");
						
						List<OrderLineItemCart> deletedLis = new ArrayList<OrderLineItemCart>();
						
						for(OrderLineItemCart li : lis){
							
							/* Make sure the lineItem is specific to current product */
							if(li.getProductId() == productId){ 
								
								if(productOutOfStock 
										&& li.isInstock()) {

									li.setInstock(false);
									itemChanged = true;
									
									logDetails.append("Item with " + productId + " updated to outofstock");
								}
								
								/*Product is in stock, but lineItem says it's out-of-stock*/
								else if(!productOutOfStock && !li.isInstock()){
									li.setInstock(true);
									itemChanged = true;
									
									logDetails.append("Item with " + productId + " updated to instock");
								}
								
								
								/*Product is currently variable, but in cart it's not, 
								 *Or product is not variable, but in cart it is  
								 *
								 *In both scenarios delete the item */
								if( (variableProduct && li.getVariationId()==0) 
										|| (!variableProduct && li.getVariationId()!=0) ){
									deletedLis.add(li);
									itemChanged = true;
									
									logDetails.append("Item with " + productId + " deleted!");
								}
								
								
								/*
								 *If the product is not variable. update the price changes 
								 **/
								if(!variableProduct){
									
									/*The item is currently on sale, and prices doesn't match,  
									 *update the cart to be on sale*/
									if(product.getSalePrice()!=0){

										if(li.getPrice() != product.getSalePrice()){											
											li.setPrice(product.getSalePrice());
											li.setCost(product.getPrice());
											
											itemChanged = true;
											
											logDetails.append("Item with " + productId + ", price updated");
										}
									}

									/* The item is currently NOT on sale, and prices doesn't match, 
									 * update the cart price to regular price*/
									else{

										if(li.getPrice() != product.getPrice()){											
											li.setPrice(product.getPrice());
											li.setCost(product.getPrice());
											
											itemChanged = true;
											
											logDetails.append("Item with " + productId + ", price updated");
										}
									}
								}
							}	
							
							
							
							/*Check if there is any coupon applied to that order*/
							if(li.getType().equals("coupon")){
								couponCode = li.getName();
							}
						}
						
						
						/*If any line item was changed*/
						if(itemChanged){
							
							updateCounter++;
							
							/*Delete any items if required*/
							if(deletedLis != null && deletedLis.size()>0){
								for(OrderLineItemCart dli : deletedLis){
									lis.remove(dli);
								}
							}
							
							/*If there was an itemChange and couponCode was present, reapply coupon*/
							if(!"".equals(couponCode)){
								couponManager.reapplyCoupon(couponCode, co, false);
							}
							
							/*Recalculate order summary*/
							cartLogics.calculateSummary(co);							
							logDetails.append("order total recalculated.");
							
							
							/*Update the cart order*/
							cartDao.save(co);
							

							
							
							/**
							 * Update Log
							 * */
							try {
								
								Log log = new Log();
								log.setCollection("cartorders");
								log.setDetails(logDetails.toString());
								log.setDate(Calendar.getInstance().getTime());
								log.setKey(co.get_id());
								log.setUser("System");
								
								logDao.save(log);					
							}
							catch(Exception e){
								logger.error(Exceptions.giveStackTrace(e));
							}
						}
					}						
				}
				
				
				
			}
			
			else{
				response = "No update performed. Product is null";
			}
			
			
		} catch (Exception e){
			response = e.getMessage();
			logger.error(Exceptions.giveStackTrace(e));
		}
		
		
		System.out.println("SynchronizeCartItems.sync - " 
				+ processCounter + " cartOrders processed. " 
				+ updateCounter + " cartOrders updated.");
		
		
		return response;
	}

}
