package com.luvbrite.services;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.luvbrite.dao.CartOrderDAO;
import com.luvbrite.dao.CouponDAO;
import com.luvbrite.dao.CouponSpecialDAO;
import com.luvbrite.dao.LogDAO;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.utils.Utility;
import com.luvbrite.web.models.AttrValue;
import com.luvbrite.web.models.CartOrder;
import com.luvbrite.web.models.Coupon;
import com.luvbrite.web.models.CouponSpecial;
import com.luvbrite.web.models.Log;
import com.luvbrite.web.models.OrderLineItemCart;
import com.luvbrite.web.models.GenericResponse;


@Service
public class CouponManager {

	private static Logger logger = Logger.getLogger(CouponManager.class);
			
	@Autowired
	private CouponDAO dao;	

	@Autowired
	private CartOrderDAO cartDao;
	
	@Autowired
	private CartLogics cartLogics;
	
	@Autowired
	private LogDAO logDao;
	
	@Autowired
	private CouponSpecialDAO couponSpecialDao;
	
	
	private SimpleDateFormat sd = new SimpleDateFormat("yyyyMMdd");
	private NumberFormat nf = NumberFormat.getCurrencyInstance();
	
	
	/**
	 * Called during cart update
	 *
	 * @param saveOrder - Whether the order should be saved here or not. This is to avoid multiple updates in database
	 * @param action - What action caused the re-application of coupon. (ex: update qty, add item, remove item, etc)
	 * 
	 * @return Any error or resp message from the methods.
	 */
	public String reapplyCoupon(String couponCode, CartOrder order, boolean saveOrder, String action){
		
		if(couponCode == null) couponCode = "";
		Coupon coupon = dao.get(couponCode);

		String resp = preValidateCoupon(coupon, couponCode, action);
		if(!resp.equals("")){ //if there was some issue during coupon application, remove coupon
			removeCouponPrivate(couponCode, null, order);
			//System.out.println("Remove in preValidate");
		}
		else{
			resp = postValidateCoupon(coupon, order.get_id(), order, saveOrder, action);
			if(!resp.equals("")){ //if there was some issue during coupon application, remove coupon
				removeCouponPrivate(couponCode, null, order);
				//System.out.println("Remove in postValidate");
			}
		}
		
		return resp;
	}
	
	
	
	/**
	 * Called when a coupon is needs to be removed
	 */
	public GenericResponse removeCoupon(String couponCode, Long orderId){
		GenericResponse gr = new GenericResponse();
		gr.setSuccess(false);

		if(couponCode == null || couponCode.equals("") || orderId == null || orderId == 0){
			gr.setMessage("Invalid promocode / order id.");
			return gr;
		}
		
		return removeCouponPrivate(couponCode, orderId, null);
	}
	
	
	public GenericResponse removeCoupon(String couponCode, CartOrder order){
		GenericResponse gr = new GenericResponse();
		gr.setSuccess(false);

		if(couponCode == null || couponCode.equals("") || order == null || order.get_id() == 0){
			gr.setMessage("Invalid promocode / order id.");
			return gr;
		}
		
		return removeCouponPrivate(couponCode, null, order);
	}
	
	
	private GenericResponse removeCouponPrivate(String couponCode, Long orderId, CartOrder order){

		GenericResponse cr = new GenericResponse();
		cr.setSuccess(false);


		Coupon coupon = dao.get(couponCode);
		if(coupon!=null){				

			if(order == null && orderId != null){
				order = cartDao.get(orderId);
			}

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

						boolean updateOrderTotal = false;

						/* Go through items and remove the discounted price */
						List<OrderLineItemCart> olics = order.getLineItems();
						for(OrderLineItemCart olic : olics){

							if(olic.getPromo()!=null && olic.getPromo().equals("p")){
								olic.setPromo("");
								olic.setPrice(olic.getCost());

								updateOrderTotal= true;
							}
						}

						String recalctxt = "";
						if(updateOrderTotal){

							//Update orderTotals
							cartLogics.calculateSummary(order);
							recalctxt = "recalculated and ";
						}

						//Since coupon was removed, order needs to be saved even if updateOrderTotal is false;
						cartDao.save(order);

						/**
						 * Update Log
						 * */
						try {

							Log log = new Log();
							log.setCollection("cartorders");
							log.setDetails("Order " + recalctxt + "updated because of coupon removal");
							log.setDate(Calendar.getInstance().getTime());
							log.setKey(order.get_id());
							log.setUser("System");

							logDao.save(log);					
						}
						catch(Exception e){
							logger.error(Exceptions.giveStackTrace(e));
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

		return cr;
	}
	
	public String increaseCouponLimit(String userEmail, int count, double value){
		
		String resp = "";
		
		Coupon coupon = dao.createQuery()
				.field("emails").equalIgnoreCase(Pattern.quote(userEmail))
				.field("type").equal("F")
				.field("_id").contains("rewards10")
				.get();
		
		if(coupon != null){
			
			Calendar now = Calendar.getInstance();
			now.add(Calendar.MONTH, 6);
			
			coupon.setCouponValue(value);
			coupon.setMaxUsageCount(coupon.getMaxUsageCount() + count);
			coupon.setExpiry(now.getTime());
			coupon.setActive(true);
			
			dao.save(coupon);

			try {
				
				Log log = new Log();
				log.setCollection("coupons");
				log.setDetails("Coupon " + coupon.get_id() + 
						". Coupon made active, expiration date set to +6 months, value set to 10. Increment count and value by " + 
						coupon.getMaxUsageCount());
				log.setDate(Calendar.getInstance().getTime());
				log.setKey(0);
				log.setUser("System");
				
				logDao.save(log);					
			}
			catch(Exception e){
				logger.error(Exceptions.giveStackTrace(e));
			}
		}
		else{
			resp = "No coupon found for the user " + userEmail;
		}
		
		
		return resp;
	}
	
	/**
	 * Called when a coupon is applied during checkout process
	 */
	public GenericResponse applyCoupon(String couponCode, Long orderId){

		GenericResponse cr = new GenericResponse();
		cr.setSuccess(false);

		if(couponCode == null) couponCode = "";
		if(orderId==null) orderId = 0l;
		
		couponCode = couponCode.toLowerCase();

		
		if(!couponCode.equals("") && orderId != 0){
			
			Coupon coupon = dao.get(couponCode);
			String preValidate = preValidateCoupon(coupon, couponCode, "applycoupon");
			
			if("".equals(preValidate)){
				
				
				String postValidate = postValidateCoupon(coupon, orderId, null, true, "applycoupon"); //saveOrder
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
	
	
	private String preValidateCoupon(Coupon coupon, String couponCode, String action){
		
		String resp = "";
		if(coupon != null){
			
			if(!coupon.isActive()){
				resp = couponCode + " is not Active";
			}
			
			//Coupon count need to be checked only during applycoupon
			//during other scenarios, usageCount includes current use.
			else if(action.equals("applycoupon") 
					&& coupon.getMaxUsageCount() !=0 
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
	
	
	private String postValidateCoupon(Coupon coupon, long orderId, CartOrder order, boolean saveOrder, String action){
		
		String resp = "";
		List<Long> pids = new ArrayList<Long>();
		List<String> emails = new ArrayList<String>();
		
		try{
			
			if(coupon.getPids() != null 
					&& coupon.getPids().size()>0){				
				pids = coupon.getPids();
			}
			
			List<String> cEmails = coupon.getEmails();
			if(cEmails != null && !cEmails.isEmpty()){
				for(String email : cEmails){
					emails.add(email.toLowerCase());
				}
			}
			
			if(order == null){
				order = cartDao.get(orderId);
			}
			
			if(order !=null){
				
				boolean matchingProductsFound = false,
						couponApplied = false;
				double orderTotal = 0d;
				boolean noValidItemsFound = true;
				
				String specialHandling = specialApplyHandlers(order, coupon, action);
				if(!specialHandling.equals("")) return specialHandling;
				
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
						
						/*Check if the item is valid*/
						if(validPromoItem(olic)){
							noValidItemsFound = false;
						}
					}
				}
				
				else{
					
					return "There is no item in your cart";
				}
				
				
				if(couponApplied) return "The coupon had already been applied to this order";
				
				if(noValidItemsFound) return "No eligible items found in the cart.";
				
				
				/**
				 * 	If there matching Products or 
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
					if(!emails.isEmpty()){
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
						
						resp = applyDiscount(coupon, order, saveOrder);
						
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
	
	private String applyDiscount(Coupon coupon, CartOrder order, boolean saveOrder){
		
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
					//System.out.println("CM - orderTotal = " + orderTotal);
				}
			}
			
			/* There the orderTotal is less then there is something wrong */
			if(orderTotal <= 0d) return "Order total is invalid";
			
			
			/* if pids containes products, but productItems = 0, it means there are no valid products in the cart*/
			if(productItems == 0 && !pids.isEmpty()){
				return "This promotion doesn't apply to the products in your cart.";
			}
			
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
			
			case "PO" :
			case "F"  :

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
				
				//System.out.println("CM - applicableTotal = " + applicableTotal);
				if(productItems > 0){ 
					for(OrderLineItemCart olic : olics){
						if(validPromoItem(olic)){
							
							if(pids.contains(olic.getProductId())){
								double itemDiscount = (olic.getCost() / applicableTotal) * couponValue;
								olic.setPrice(olic.getCost() - itemDiscount);
								olic.setPromo("p");

								//System.out.println("CM - F itemDiscount = " + itemDiscount);
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

							//System.out.println("CM - F else itemDiscount = " + itemDiscount);	

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

							//System.out.println("CM - R itemDiscount = " + itemDiscount);
							
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

							//System.out.println("CM - R else itemDiscount = " + itemDiscount);

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
							
							/*If the same coupon is being applied again, update coupon obj as well*/
							if(removeC.get_id().equals(coupon.get_id())){
								coupon.setUsageCount(removeC.getUsageCount());
							}
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
				

				/* Some programs handles the save themselves, so no need to do it here. */				
				if(saveOrder){
					
					//Update orderTotals
					cartLogics.calculateSummary(order);

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
				&& olic.isInstock() 
				&& (olic.getPromo() == null 
					|| olic.getPromo().equals("")
					|| olic.getPromo().equals("p")); //Item is not already a promo item other than coupon
	}
	
	
	private String specialApplyHandlers(CartOrder order, Coupon coupon, String action){
		
		String message = "";
		String couponCode = coupon.get_id().toUpperCase();
		if(couponCode.equals("WMPOWER")){
			
			boolean qualifyingProductFound = false,
					promoProductFound = false;
			
			/* Check if the coupon is applied to this order.*/
			List<OrderLineItemCart> olis = order.getLineItems();
			for(OrderLineItemCart item: olis){
				if(item.getProductId() == 11856 && item.isInstock()){
					qualifyingProductFound = true;
				}
				
				else if(item.getProductId() == 11872 && item.isInstock()){
					promoProductFound = true;
				}
				
				if(qualifyingProductFound && promoProductFound)
					break;
			}
			
			
			if(!qualifyingProductFound){
				message = "No eligible items found in the cart";
			}
			
			else{
				
				if(!promoProductFound){
					//Add promo product
				
					OrderLineItemCart newItem = new OrderLineItemCart();
					newItem.setTaxable(false);
					newItem.setInstock(true);
					newItem.setType("item");
					newItem.setName("Power Puff Roll (Single House Roll)");
					newItem.setProductId(11872);
					newItem.setVariationId(7619);
					newItem.setQty(1);
					newItem.setCost(10d);
					newItem.setPrice(0d);
					newItem.setImg("/products/PowerPuffNewImage-1.jpg");
					
					List<AttrValue> attrValList = new ArrayList<>();
					AttrValue attrVal = new AttrValue();
					attrVal.setAttr("Type");
					attrVal.setValue("Jungle Mix");
					
					attrValList.add(attrVal);
					
					newItem.setSpecs(attrValList);

					olis.add(newItem);
				}
			}
			
		}
		
		else if(coupon.getType().equals("PO")){ //Offer
			productOfferCoupon(order, coupon, action);
		}
		
		return message;
	}
	
	
	
	/**
	 * LOGIC
	 * 
	 * Product offer coupons works like Fixed Discount coupons.
	 * The coupon value will be the value of the discount offered on the product. The coupon
	 * will be only applicable to the offered product, i.e. coupon.pids will have the product_id of the
	 * offered product (more clarification on the example below).
	 * The details about the offered product and it's option will be saved in another
	 * collection called coupon_specials.
	 * 
	 * 
	 * Lets assume we are giving a free joint (product_id = 101) worth $10 for any purchase of 
	 * jungle mix (product_id = 201).
	 * The coupon will be created like this.
	 * 
	  
	       {
				"_id" : "somecouponcode",
				"description" : "JOINTPROMO",
				"type" : "PO",
				"usageCount" : 0,
				"maxUsageCount" : 1,
				"amt" : 10,
				"active" : true,
				"minAmt" : 0,
				"pids" : [
					NumberLong(101)
				],
				"maxDiscAmt" : 0
			}
			
	 * 
	 * Here note the following fields
	 * 		amt 		- The value of the product offered. If a $20 product is offered at $8, then amt will be $12.
	 * 		description - This is the reference to 'coupon_specials' collection. Should be same for all coupons 
	 * 					  created for this particular promo.
	 * 		type 		- PO for this offer
	 * 		pids 		- The product which is offered, in this case joint (product_id = 101)
	 * 
	 * maxDiscAmt and minAmt can be used to control the coupon further just like any normal coupon.
	 * 
	 * The corresponding coupon_specials entry will look like this:
	 * 
	 
		{
			"_id" : "JOINTPROMO",
			"validProductIds" : [
				NumberLong(201)
			],
			"olic" : {
				"instock" : true,
				"type" : "item",
				"name" : "Joint",
				"itemId" : NumberLong(0),
				"qty" : 1,
				"pid" : NumberLong(101),
				"vid" : NumberLong(7081),
				"cost" : 10,
				"price" : 10,
				"taxable" : false,
				"img" : "/uploads/2015/09/joint.jpg",
				"specs" : [
					{
						"attr" : "some attr",
						"value" : "some value"
					}
				]
			}
		}
	 
	 * 
	 * Here note the following fields
	 * 	_id 			- This is the reference to 'coupons' collection's description field. 
	 * validProductIds	- The products for which this promo is valid. In our example jungle mix (product_id = 201)
	 * 					  if this field is empty, it means the offer applies to all products.
	 * 
	 * olic				- The OrderLineItemCart object that will be added to the order when offer is applied
	 * 
	 * 
	 * Control flows to the below method when the coupon.type is PO
	 * First it checks if there is any qualifying products in the cart.
	 * Then it checks to see if the offer product is already in the cart, 
	 * 	if yes, we keep it as it is,
	 * 	else, we add the product.
	 * 
	 * The control goes back to applying coupon and follows the logic of the Fixed Coupon
	 * 
	 * In case of removing the coupon, the discount is removed, but the item stays in the cart
	 * at full price.
	 * */
	
	
	private String productOfferCoupon(CartOrder order, Coupon coupon, String action){
		
		String message = "";
		
			
		boolean qualifyingProductFound = false,
				promoProductFound = false;
		
		
		//get the coupon_specials details
		CouponSpecial cs = couponSpecialDao.findOne("couponCode", 
				coupon.getDescription().trim().toLowerCase());
		if(cs == null){
			message = "No valid specials found for this coupon.";
			return message;
		}
		
		List<Long> pids = new ArrayList<Long>();
		if(cs.getValidProductIds() != null 
				&& cs.getValidProductIds().size()>0){				
			pids = cs.getValidProductIds();
		}
		
		//If there is no qualifying products listed, it means all products qualify
		if(pids.size() == 0){
			qualifyingProductFound = true;
		}
		
		
		/* Check if the coupon is applied to this order.*/
		List<OrderLineItemCart> olis = order.getLineItems();
		for(OrderLineItemCart item: olis){

			if(pids.contains(item.getProductId())){
				qualifyingProductFound = true;
			}
			
			else if(cs.getOlic().getProductId() == item.getProductId() 
					&& item.isInstock()){
				
				if(cs.getOlic().getVariationId() != 0){
					
					if(cs.getOlic().getVariationId() == item.getVariationId()){
						promoProductFound = true;
					}
					
				} else {
					
					promoProductFound = true;
				}
			}
			
			if(qualifyingProductFound && promoProductFound)
				break;
		}
		
		
		if(!qualifyingProductFound){
			message = "No eligible items found in the cart";
		}
		
		else{
			
			/**
			 * action.equals("applycoupon") 
			 * 
			 * Coupon logic is re-applied whenever there is a change to the cart.
			 * For example if an item removed and that item was the offer item, 
			 * we need to make sure it's not added back into the cart.
			 * 
			 * So don't add the offer item unless the action is "applycoupon"
			 **/
			
			if(!promoProductFound 
					&& action.equals("applycoupon")){
				//Add promo product
			
				OrderLineItemCart newItem = cs.getOlic();
				olis.add(newItem);
			}
		}
		
		return message;
	}

}
