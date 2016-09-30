package com.luvbrite.services;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.mongodb.morphia.query.Query;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.luvbrite.dao.CartOrderDAO;
import com.luvbrite.dao.LogDAO;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.web.models.CartOrder;
import com.luvbrite.web.models.Log;
import com.luvbrite.web.models.OrderLineItemCart;
import com.luvbrite.web.models.Price;
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
	
	private static Logger logger = Logger.getLogger(SynchronizeCartItems.class);
	
	@Autowired
	private CartOrderDAO cartDao;
	
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
						StringBuilder logDetails = new StringBuilder().append("Product change detected. ");
						
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
									
									logDetails.append("Item with " + productId + " removed from order id " + co.get_id());
								}
								
								
								/*
								 *If the product is not variable. update the price changes 
								 *
								 *If the product is variable, price are stored in price collection
								 *and is synced during changes to that (another method below). 
								 **/
								if(!variableProduct){
									
									/*The item is currently on sale, and prices doesn't match,  
									 *update the cart to be on sale*/
									if(product.getSalePrice()!=0){

										if(li.getPrice() != product.getSalePrice()){											
											li.setPrice(product.getSalePrice());
											li.setCost(product.getPrice());
											li.setPromo("s");
											
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
											li.setPromo("");
											
											itemChanged = true;
											
											logDetails.append("Item with " + productId + ", price updated");
										}
									}
								}
							}	
						}
						
						
						/*If any line item was changed*/
						if(itemChanged){
							
							updateCounter++;
							
							/*Delete any items if required*/
							if(deletedLis != null && !deletedLis.isEmpty()){
								for(OrderLineItemCart dli : deletedLis){
									lis.remove(dli);
								}
							}
							
							
							
							/**    ---- NOTE ----
							 *  
							 * Coupons are re-applied every time the cart loads and 
							 * totals are recalculated, so no need to do it again here! 
							 **/
							
							
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
	
	
	public String sync(List<Price> prices){
		
		String response = "";
		
		int updateCounter = 0,
				processCounter = 0;
		
		try {
			
			if(prices!=null && !prices.isEmpty()){
			
				long productId = prices.get(0).getProductId();
				
				/**Get all the items in the cart for this product*/
				List<CartOrder> cos = cartDao.createQuery().filter("lineItems.pid", productId).asList();
				for(CartOrder co : cos){
					
					processCounter++;
					
					List<OrderLineItemCart> lis = co.getLineItems();
					if(lis != null){
						
						boolean itemChanged = false;
						StringBuilder logDetails = new StringBuilder().append("Price change detected.");
						
						List<OrderLineItemCart> deletedLis = new ArrayList<OrderLineItemCart>();

						
						/** Check if the variationIds in lineItem matches the _id of each price in prices.
						 *	
						 *	if there are not match, that means that price line is not there anymore, delete the li
						 *	if there is a match, run through the logic to match the lineItem to the most recent price. 
						 **/
						
						for(OrderLineItemCart li : lis){
							
							/* Make sure the lineItem is specific to current product */
							if(li.getProductId() == productId) {
								
								boolean priceActive = false;
								Price currVariation = null;
								long varitionId = li.getVariationId();
								
								/* Check if the variationId still matches an Active Price */
								for(Price price : prices){
									if(varitionId==price.get_id()){
										
										priceActive = true;
										currVariation = price;
										break;
									}
								}
								
								/* If matches, run through logic */
								if(priceActive) {
									
									boolean variationOutOfStock = false;
									long priceId = currVariation.get_id();
									
									/*If this variation is outofstock*/
									if(currVariation.getStockStat().equals("outofstock")) variationOutOfStock = true;	
									
									if(variationOutOfStock 
											&& li.isInstock()) {

										li.setInstock(false);
										itemChanged = true;

										logDetails.append("Item with " + productId + ":" + priceId + " updated to outofstock");
									}
										
									/*variation is in stock, but lineItem says it's out-of-stock*/
									else if(!variationOutOfStock && !li.isInstock()){
										li.setInstock(true);
										itemChanged = true;
										
										logDetails.append("Item with " + productId + ":" + priceId + " updated to instock");
									}
										
											
									/*The item is currently on sale, and prices doesn't match,  
									 *update the cart to be on sale*/
									if(currVariation.getSalePrice()!=0){

										if(li.getPrice() != currVariation.getSalePrice()){											
											li.setPrice(currVariation.getSalePrice());
											li.setCost(currVariation.getRegPrice());
											li.setPromo("s");
											
											itemChanged = true;
											
											logDetails.append("Item with " + productId + ":" + priceId + ", price updated");
										}
									}

									/* The item is currently NOT on sale, and prices doesn't match, 
									 * update the cart price to regular price*/
									else{

										if(li.getPrice() != currVariation.getRegPrice()){											
											li.setPrice(currVariation.getRegPrice());
											li.setCost(currVariation.getRegPrice());
											li.setPromo("");
											
											itemChanged = true;
											
											logDetails.append("Item with " + productId + ":" + priceId + ", price updated");
										}
									}
								}								

								/* If doesn't match, mark for deletion */
								else{
									
									deletedLis.add(li);
									itemChanged = true;
								}	
								
							
							}//Matching productId to lineItem.pid. No need to do anything in the else case! 	
							
														
						}
						
						/* We have run through all lineItem for that cartOrder, lets do necessary deletes and updates */
						if(itemChanged){
							
							updateCounter++;
							
							/*Delete any items if required*/
							if(deletedLis != null && !deletedLis.isEmpty()){
								for(OrderLineItemCart dli : deletedLis){
									lis.remove(dli);
									logDetails.append("Item with " + dli.getProductId() + ":" + dli.getVariationId() + ", removed from order id " + co.get_id());
								}
							}
							
							
							
							/**    ---- NOTE ----
							 *  
							 * Coupons are re-applied every time the cart loads and 
							 * totals are recalculated, so no need to do it again here! 
							 **/
							
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
				response = "No update performed. prices obj is null";
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
