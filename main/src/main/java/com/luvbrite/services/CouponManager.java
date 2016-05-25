package com.luvbrite.services;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.luvbrite.dao.CartOrderDAO;
import com.luvbrite.dao.CouponDAO;
import com.luvbrite.dao.LogDAO;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.utils.Utility;
import com.luvbrite.web.models.CartOrder;
import com.luvbrite.web.models.Coupon;
import com.luvbrite.web.models.Log;
import com.luvbrite.web.models.OrderLineItemCart;
import com.luvbrite.web.models.GenericResponse;


@Service
public class CouponManager {

	private static Logger logger = LoggerFactory.getLogger(CouponManager.class);
			
	@Autowired
	private CouponDAO dao;	

	@Autowired
	private CartOrderDAO cartDao;
	
	@Autowired
	private CartOrderSummary orderSummary;
	
	@Autowired
	private LogDAO logDao;
	
	
	private SimpleDateFormat sd = new SimpleDateFormat("yyyyMMdd");
	private NumberFormat nf = NumberFormat.getCurrencyInstance();
	
	
	/**
	 * Called during cart update
	 **/
	public String reapplyCoupon(String couponCode, CartOrder order){
		
		if(couponCode == null) couponCode = "";
		Coupon coupon = dao.get(couponCode);
		
		return applyDiscount(coupon, order);
	}
	
	
	
	/**
	 * Called when a coupon is needs to be removed
	 */
	public GenericResponse removeCoupon(String couponCode, Long orderId){
		
		GenericResponse cr = new GenericResponse();
		cr.setSuccess(false);
		
		if(couponCode == null || couponCode.equals("") || orderId == null || orderId == 0){
			cr.setMessage("Invalid promocode / order id.");
		}
		else{

			Coupon coupon = dao.get(couponCode);
			if(coupon!=null){				
				
				CartOrder order = cartDao.get(orderId);
				if(order != null){
					
					boolean couponFound = false;
					
					if(order.getLineItems() != null){
						
						/* Check if the coupon is applied to this order.*/
						Iterator<OrderLineItemCart> iter = order.getLineItems().iterator();
						while(iter.hasNext()){

							OrderLineItemCart item = iter.next();
							if(item.getType().equals("coupon")
									&& item.getName().equals(couponCode)){

								couponFound = true;
								iter.remove();

								break;
							}
						}

						if(couponFound){

							boolean updateOrder = false;
							
							/* Go through items and remove the discounted price */
							List<OrderLineItemCart> olics = order.getLineItems();
							for(OrderLineItemCart olic : olics){
								
								if(olic.getPromo()!=null && olic.getPromo().equals("p")){
									olic.setPromo("");
									olic.setPrice(olic.getCost());
									
									updateOrder= true;
								}
							}
							
							if(updateOrder){
								
								//Update orderTotals
								orderSummary.calculateSummary(order);
								
								cartDao.save(order);
								
								/**
								 * Update Log
								 * */
								try {
									
									Log log = new Log();
									log.setCollection("cartorders");
									log.setDetails("Order recalculated and updated because of coupon removal");
									log.setDate(Calendar.getInstance().getTime());
									log.setKey(order.get_id());
									log.setUser("System");
									
									logDao.save(log);					
								}
								catch(Exception e){
									logger.error(Exceptions.giveStackTrace(e));
								}
							}
							
							
							
							/* Update count usage count as well as the log.*/
							coupon.setUsageCount(coupon.getUsageCount()-1);	
							dao.save(coupon);

							try {
								
								Log log = new Log();
								log.setCollection("coupons");
								log.setDetails("Coupon " + coupon.get_id() + " removed from orderId " + order.get_id()
										+ ". Coupon usage decreased");
								log.setDate(Calendar.getInstance().getTime());
								log.setKey(0);
								log.setUser("System");
								
								logDao.save(log);					
							}
							catch(Exception e){
								logger.error(Exceptions.giveStackTrace(e));
							}
							
							
							
							cr.setSuccess(true);																	
							
							
							
						}
						
						else{
							cr.setMessage("Coupon not found in this order.");
						}

					}
					
					else{
						cr.setMessage("No items found in the order.");
					}
				}
				
				else{
					cr.setMessage("Order not found.");
				}
			}
			
			else{
				cr.setMessage("Coupon code not found.");
			}
		}
		
		return cr;
	}
	
	
	
	/**
	 * Called when a coupon is applied during checkout process
	 */
	public GenericResponse applyCoupon(String couponCode, Long orderId){

		GenericResponse cr = new GenericResponse();
		cr.setSuccess(false);

		if(couponCode == null) couponCode = "";
		if(orderId==null) orderId = 0l;

		
		if(!couponCode.equals("") && orderId != 0){
			
			Coupon coupon = dao.get(couponCode);
			String preValidate = preValidateCoupon(coupon, couponCode);
			
			if("".equals(preValidate)){
				
				
				String postValidate = postValidateCoupon(coupon, orderId);
				if("".equals(postValidate)){
					cr.setSuccess(true);
					
				}
				else{
					cr.setMessage(postValidate);
				}
			
				
			}			
			else{
				cr.setMessage(preValidate);
			}
		}
		
		
		
		else{
			
			if(couponCode.equals(""))
				cr.setMessage("Invalid promo code");
			
			else if(orderId == 0)
				cr.setMessage("Invalid order");
		}
		
		return cr;	
	}	
	
	
	private String preValidateCoupon(Coupon coupon, String couponCode){
		
		String resp = "";
		if(coupon != null){
			
			if(!coupon.isActive()){
				resp = couponCode + " is not Active";
			}
			
			
			else if(coupon.getMaxUsageCount() !=0 
					&& (coupon.getUsageCount() >= coupon.getMaxUsageCount()) ){
				resp = couponCode + " maxed out";
			}
			
			else if(coupon.getExpiry() != null 
					&& (getDateValue(coupon.getExpiry()) 
							< getDateValue(Calendar.getInstance().getTime()))){	
				
				resp = couponCode + " expired";			
			}
			
		}
		
		else{
			resp = couponCode + " is not a valid promo code";
		}
		
		
		return resp;
	}
	
	
	private String postValidateCoupon(Coupon coupon, long orderId){
		
		String resp = "";
		List<Long> pids = new ArrayList<Long>();
		List<String> emails = new ArrayList<String>();
		
		try{
			
			if(coupon.getPids() != null 
					&& coupon.getPids().size()>0){				
				pids = coupon.getPids();
			}
			
			if(coupon.getEmails() != null 
					&& coupon.getEmails().size()>0){				
				emails = coupon.getEmails();
			}
			
			CartOrder order = cartDao.get(orderId);
			if(order !=null){
				
				boolean matchingProductsFound = false,
						couponApplied = false;
				double orderTotal = 0d;
				
				List<OrderLineItemCart> olics = order.getLineItems();
				if(olics != null && olics.size() > 0){
					for(OrderLineItemCart olic : olics){
						if(pids.contains(olic.getProductId())){
							matchingProductsFound = true;
						}
						
						if(olic.getType().equals(coupon.get_id())){
							couponApplied = true;
						}
						
						orderTotal+= ( olic.getPrice()*olic.getQty() );
					}
				}
				
				else{
					
					return "There is no item in your cart";
				}
				
				
				if(couponApplied) return "The coupon had already been applied to this order";
				
				
				/*	If there matching Products or 
				 * 	if the promo applies to all products i.e. pids.size() == 0 
				 */
				if(matchingProductsFound || pids.size() == 0){
					
					double minAmtReq = coupon.getMinAmt();
					if(minAmtReq != 0 &&
							orderTotal < minAmtReq){
						
						return "To apply this promo, Order total should be greater than " 
									+ nf.format(minAmtReq);
					}
					
					
						
					/* Check if promo has email restriction, 
					 * If yes, make sure the customer is logged in and 
					 * is eligible to use the coupon */
					if(emails.size()>0){
						if(order.getCustomer()==null || order.getCustomer().getEmail().equals("")){
							resp = "To apply this promo, you need to login in to "
									+ "the system with your username and password.";
						}
						else{
							
							/* If customer is logged in check if the  
							 * promo is valid for this user */
							if(!emails.contains(order.getCustomer().getEmail())){
								resp = "This promo is not applicable to " 
											+ order.getCustomer().getEmail() + ".";
							}
						}
					}
					
					
					/* No error so far
					 * Apply the coupon
					 **/
					if(resp.equals("")){
						
						resp = applyDiscount(coupon, order);
						
					}
					
				}
				else{
					
					resp = "This promotion doesn't apply to the products in your cart.";
				}
			}
			else{				
				resp = "Invalid order";
			}
			
		}catch(Exception e){
			
			logger.error(Exceptions.giveStackTrace(e));
			resp = "There was some error applying the coupon. "
					+ "Please try again or contact customer care.";
		}
		
		
		return resp;
	}
	
	private String applyDiscount(Coupon coupon, CartOrder order){
		
		String resp = "";
		
		try{

			List<Long> pids = new ArrayList<Long>();
			
			double orderTotal = 0d,
					productTotal = 0d,
					applicableTotal = 0d,
					
					//maxDiscAmt = coupon.getMaxDiscAmt(), //not used for now
					
					couponValue = coupon.getCouponValue(),
					totalDiscount = 0d;
			
			int productItems = 0;

			boolean updateOrder = false;
			
			if(coupon.getPids() != null 
					&& coupon.getPids().size()>0){				
				pids = coupon.getPids();
			}
			
			
			List<OrderLineItemCart> olics = order.getLineItems();
			for(OrderLineItemCart olic : olics){
				if(validPromoItem(olic)){

					if(pids.contains(olic.getProductId())){
						productTotal+= ( olic.getCost()*olic.getQty() );
						productItems++;
					}
					
					orderTotal+= ( olic.getCost()*olic.getQty() );
					System.out.println("CM - orderTotal = " + orderTotal);
				}
			}
			
			/* There the orderTotal is less then there is something wrong */
			if(orderTotal <= 0d) return "Order total is invalid";
			
			if(productItems > 0){ 
				applicableTotal = productTotal;
			}
			else {
				applicableTotal = orderTotal;
			}
						
			/** 
			 * 					Applicable to both CASES below
			 * ----------------------------------------------------------
			 * IF(productItems>0) 
			 * 		that means the coupon is applicable to a set
			 * 		of products in the cart. So we apply the discount to 
			 * 		only those items
			 * ELSE
			 * 		we apply the discount to all items in the cart.
			 * 
			 * 
			 *     $$$$$     -----     IMPORTANT     -----     $$$$$
			 * Since we are using the ratio, no need to use olic.getQty() here.
			 * */
			
			switch (coupon.getType()){
			
			
			case "F" :

				/**
				 * We divide the coupon value across all eligible products.
				 * To get the couponValue that needs to be applied to an item
				 * we find the ratio of itemCost to orderTotal. This ratio multiplied
				 * with the coupon value gives the discount that needs to be applied 
				 * to that item.
				 * 
				 * Item discount here would be 
				 *	(olic.getCost() / applicableTotal) * couponValue;
				 * */
				
				/* If total amount is less than discount, make sure 
				 * couponValue is brought down to total amount
				 */
				if(applicableTotal < couponValue)
					couponValue = applicableTotal;
				
				System.out.println("CM - applicableTotal = " + applicableTotal);
				if(productItems > 0){ 
					for(OrderLineItemCart olic : olics){
						if(validPromoItem(olic)){
							
							if(pids.contains(olic.getProductId())){
								double itemDiscount = (olic.getCost() / applicableTotal) * couponValue;
								olic.setPrice(olic.getCost() - itemDiscount);
								olic.setPromo("p");

								System.out.println("CM - F itemDiscount = " + itemDiscount);
								totalDiscount+= (itemDiscount * olic.getQty());
							}
						}
					}
				}
				
				else {
					for(OrderLineItemCart olic : olics){
						if(validPromoItem(olic)){
							
							double itemDiscount = (olic.getCost() / applicableTotal) * couponValue;
							olic.setPrice(olic.getCost() - itemDiscount);
							olic.setPromo("p");

							System.out.println("CM - F else itemDiscount = " + itemDiscount);	

							totalDiscount+= (itemDiscount * olic.getQty());	
						}
					}					
				}
				
				updateOrder = true;
				
				break;
				
			case "R" :

				
				/**
				 * Item discount here would be 
				 *	olic.getCost() * ( couponValue / 100 );
				 */				
				
				if(productItems > 0){ 
					for(OrderLineItemCart olic : olics){
						if(pids.contains(olic.getProductId()) 
								&& validPromoItem(olic)){
							
							double itemDiscount = olic.getCost() * ( couponValue/ 100 );
							olic.setPrice(olic.getCost() - itemDiscount);
							olic.setPromo("p");

							System.out.println("CM - R itemDiscount = " + itemDiscount);
							
							totalDiscount+= (itemDiscount * olic.getQty());
						}
					}
				}
				
				else {
					for(OrderLineItemCart olic : olics){
						
						if(validPromoItem(olic)){
							
							double itemDiscount = olic.getCost() * ( couponValue/ 100 );
							olic.setPrice(olic.getCost() - itemDiscount);
							olic.setPromo("p");

							System.out.println("CM - R else itemDiscount = " + itemDiscount);

							totalDiscount+= (itemDiscount * olic.getQty());	
						}
					}					
				}
				
				updateOrder = true;
				
				break;
				
			default :
				resp = coupon.get_id() + " is not a valid promo code.";
			}
			
			if(updateOrder){
				
				
				//Remove all coupons if already there.
				Iterator<OrderLineItemCart> iter = order.getLineItems().iterator();
				while(iter.hasNext()){
					OrderLineItemCart item = iter.next();
					if(item.getType().equals("coupon")){
						
						/*
						 * Reduce the coupon usage count */
						Coupon removeC = dao.get(item.getName());
						if(removeC!=null){
							removeC.setUsageCount(removeC.getUsageCount()-1);							
							dao.save(removeC);
						}
						
						/* Remove item from the order as well*/
						iter.remove();
					}
				}
				
				
				//Add discount item to the order
				OrderLineItemCart olic = new OrderLineItemCart();
				olic.setType("coupon");
				olic.setName(coupon.get_id());
				olic.setPrice(totalDiscount);
				
				order.getLineItems().add(olic);
				
				
				//Update orderTotals
				orderSummary.calculateSummary(order);
				
				//Save Order				
				cartDao.save(order);

				
				/**
				 * Update Log
				 * */
				try {
					
					Log log = new Log();
					log.setCollection("cartorders");
					log.setDetails("Order recalculated and updated because of coupon");
					log.setDate(Calendar.getInstance().getTime());
					log.setKey(order.get_id());
					log.setUser("System");
					
					logDao.save(log);					
				}
				catch(Exception e){
					logger.error(Exceptions.giveStackTrace(e));
				}
				
				
				/**
				 * Update coupon and log
				 */				
				coupon.setUsageCount(coupon.getUsageCount()+1);
				dao.save(coupon);

				try {
					
					Log log = new Log();
					log.setCollection("coupons");
					log.setDetails("Coupon " + coupon.get_id() + " applied to orderId " + order.get_id()
							+ ". Coupon usage incremented");
					log.setDate(Calendar.getInstance().getTime());
					log.setKey(0);
					log.setUser("System");
					
					logDao.save(log);					
				}
				catch(Exception e){
					logger.error(Exceptions.giveStackTrace(e));
				}
				
				
			}
			
		}catch(Exception e){			
			logger.error(Exceptions.giveStackTrace(e));
			resp = "There was some error applying the coupon. "
					+ "Please try again or contact customer care.";
		}
		
		return resp;
	}
	
	private int getDateValue(Date d){
		return Utility.getInteger(sd.format(d));
	}
	
	private boolean validPromoItem(OrderLineItemCart olic){
		return 
				olic.getType().equals("item")
				&& (olic.getPromo() == null 
					|| !olic.getPromo().equals("s")); //Item is not already discounted
	}

}
