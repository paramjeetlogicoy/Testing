package com.luvbrite.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.luvbrite.dao.CartOrderDAO;
import com.luvbrite.dao.OrderDAO;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.utils.Utility;
import com.luvbrite.web.models.Address;
import com.luvbrite.web.models.CartOrder;
import com.luvbrite.web.models.ControlOptions;
import com.luvbrite.web.models.GenericResponse;
import com.luvbrite.web.models.Order;
import com.luvbrite.web.models.OrderLineItemCart;

@Service
public class CartLogics {
	
	private static Logger logger = Logger.getLogger(CartLogics.class);
	
	@Autowired
	private CartOrderDAO dao;
	
	@Autowired
	private OrderDAO completedOrderdao;
	
	public void calculateSummary(CartOrder order){
		
		try {
			
			if(order!=null){

				double subTotal = 0d, 
						total = 0d, 
						discount = 0d,
						couponDiscount = 0d;
				
				int index = -1,
						couponIndex = -1;
				
				List<OrderLineItemCart> items = order.getLineItems();
				if(items != null){
					
					for(OrderLineItemCart item : items){
						
						index++;
						
						if(item.getType().equals("item") && item.isInstock()){
							
							double itemPrice = item.getPrice();
							if(itemPrice >= 0d){
								
								double cost = item.getCost();
								int qty = item.getQty();
								
								subTotal += (cost * qty);
								total += (itemPrice * qty);	
								
								if(item.getPromo()!=null && item.getPromo().equals("p")){
									couponDiscount += ((cost - itemPrice) * qty);
								}
							}
							
						}
						
						
						if(item.getType().equals("coupon")){
							couponIndex = index;
						}
					}
					
					
					
					if(couponIndex > -1){
						
						OrderLineItemCart item = items.get(couponIndex);
						item.setPrice(couponDiscount);
					}
				}
				
				
				if(total < 0d) total = 0d;
				if(subTotal < 0d) subTotal = 0d;
				
				subTotal = Utility.Round(subTotal, 2);
				total = Utility.Round(total, 2);
				
				discount = subTotal - total;				
				if(discount > subTotal)
					discount = subTotal;
				
				
				order.setSubTotal(subTotal);
				order.setTotal(total);
			}
			
			
		} catch (Exception e){
			logger.error(Exceptions.giveStackTrace(e));
		}		
		
	}
	
	
	
	public Map<String, Boolean> availableDeals(CartOrder order){
		
		Map<String, Boolean> dealStat = new HashMap<>();
		
		try {
			
			boolean firstTimepatient = false;
			
			
			/**
			 * Run the order through eligibility check
			 * */
			
			//First BriteBoxcheck
			if(order.getCustomer() != null && 
					order.getCustomer().get_id() != 0){
				
				if(firstOrderCheck(order.getCustomer().get_id())
						.equalsIgnoreCase("Y")){
					firstTimepatient = true;					
				}
			}			
			dealStat.put("firstTimepatient", firstTimepatient);
			
			
		} catch (Exception e){
			logger.error(Exceptions.giveStackTrace(e));
		}	
		
		return dealStat;
	}
	
	private CouponManager couponManager;
	public void applyDeals(CartOrder order, ControlOptions cOps, boolean saveOrder, HttpSession sess, CouponManager couponManager){
		this.couponManager = couponManager;
		applyDeals(order, cOps, saveOrder, sess);
	}
	
	public void applyDeals(CartOrder order, ControlOptions cOps, boolean saveOrder, HttpSession sess){
		
		try {

			
			if(order == null) return;
			
			double doubleDownThresholdAmt = cOps.getDoubleDown(),
					doubleDownOfferAmt = cOps.getDoubleDownOfferValue(),
					total = 0d;
			
			boolean orderChanged = false,
					couponPresent = false,
					briteBoxEligible = false,
					autoAddBriteBox = true;
			
			int index = -1,
				doubleDownItemIndex = -1,
				briteBoxIndex = -1,
				fifthFlowerIndex = -1;
			
			String couponCode = "";

			if(sess != null && 
					sess.getAttribute("autoBriteBoxAdd") != null && 
					( (String) sess.getAttribute("autoBriteBoxAdd") ).equals("false")){
				
				autoAddBriteBox = false;
			}
			
			/**
			 * Any coupons applied will be removed if doubledown or britebox is applied.
			 * That done by calling remove coupon before applying either of it.
			 * 
			 * If a coupon is applied to an existing order which has doubledown,
			 * coupon takes precedence.
			 * 
			 * Option to apply coupon or double down is disabled for order with britebox.
			 * 
			 * If an order has coupon and then later qualified for britebox, then coupon has
			 * to be removed and britebox applied (provided autoAddBriteBox is true).
			 * 
			 * */
		

			/*Find the total to see if the item is eligible*/
			List<OrderLineItemCart> items = order.getLineItems();
			if(items != null){
				for(OrderLineItemCart item : items){
					
					index++;
					
					if( item.getPromo() != null 
							&& "p".equals(item.getPromo()) ){
						
						couponPresent = true;
					}
					
					
					if(item.getType().equals("item") && item.isInstock()){
						
						double itemPrice = item.getPrice();
						
						if(item.getProductId() == 11839){
							briteBoxIndex = index;
						}
						
						else if(item.getProductId() == 11871){
							fifthFlowerIndex = index;
						}
						
						if( item.getPromo() != null 
								&& "doubledownoffer".equals(item.getPromo()) ){
							
							doubleDownItemIndex = index;
							
							itemPrice = item.getCost();
						}
						
						if(itemPrice > 0d){
							total += (itemPrice*item.getQty());								
						}
						
					}
					
					else if(item.getType().equals("coupon")){
						couponCode = item.getName();
					}
				}
			}
			
				
			//First BriteBoxcheck
			if(order.getCustomer() != null && 
					order.getCustomer().get_id() != 0 && 
					total >= 75d){
				
				if(firstOrderCheck(order.getCustomer().get_id())
						.equalsIgnoreCase("Y")){
					briteBoxEligible = true;	
					

					// If britebox eligible and not yet added to the order, add it
					if(briteBoxIndex == -1 && autoAddBriteBox){

						//Add fresh new item
						OrderLineItemCart newItem = new OrderLineItemCart();
						newItem.setTaxable(false);
						newItem.setInstock(true);
						newItem.setType("item");
						newItem.setName("Brite Box");
						newItem.setPromo("firsttimepatient");
						newItem.setProductId(11839);
						newItem.setVariationId(0);
						newItem.setQty(1);
						newItem.setCost(50d);
						newItem.setPrice(0d);
						newItem.setImg("/products/brite-box-img.jpg");

						items.add(newItem);

						/*Update order with lineItems*/
						order.setLineItems(items);

						orderChanged = true;
						briteBoxIndex = items.size() - 1;
					}
					
					
					if(briteBoxIndex != -1){
						//if any coupons are present, remove it.
						if(couponPresent && couponManager != null){
							GenericResponse cgr = couponManager.removeCoupon(couponCode, order);
							System.out.println(" GR MSG : " + cgr.isSuccess() + " : " + cgr.getMessage());
						}
					}
				}
			}	
			
			
			//If briteBox is not eligible, but present
			if(!briteBoxEligible && briteBoxIndex != -1){
				
				//Removed Item
				List<OrderLineItemCart> olic = order.getLineItems();
				olic.remove(briteBoxIndex);
				
				orderChanged = true;	
				briteBoxIndex = -1;		
			}
			
			
			
			
			//Here we are checking if the double down promo present is still valid			
			if( doubleDownItemIndex != -1 				&&
				doubleDownThresholdAmt > 0d 			&&  
				doubleDownOfferAmt > 0d  				&&
				!couponPresent 							&&  
				briteBoxIndex == -1						&& 
				fifthFlowerIndex == -1					&&  
				total >= doubleDownThresholdAmt) {				

				double itemCost = 0d;				
				
				if(doubleDownItemIndex > -1){
					
					OrderLineItemCart item = items.get(doubleDownItemIndex);
					itemCost = item.getCost();

					/*Check to see if the order qualifies for doubledown even if  
					 *this item (1 qty) is removed */
					if((total - itemCost) >= doubleDownThresholdAmt){
						/*It qualifies*/
					
						if(itemCost < doubleDownOfferAmt)
							doubleDownOfferAmt = itemCost;
						
						double newPrice = itemCost - ( doubleDownOfferAmt/item.getQty() );
						
						if(newPrice != item.getPrice()){
							
							item.setPrice(newPrice);
							item.setPromo("doubledownoffer");	
							
							orderChanged = true;
						}
						/* If there is no change in the price, no need to update the order! */
					}
					
					/* If item doesn't qualify */
					else {
						
						item.setPrice(itemCost);
						item.setPromo("");	
						
						orderChanged = true;
					}
				}				
			}
			
			
			
			//If doubledown is currently present, but order doesn't qualify anymore, then remove doubledown.
			// OR
			//Rare case where doubledown and britebox are present, remove doubledown.
			// OR
			//Case where doubledown and coupon are present, remove doubledown.
			if(doubleDownItemIndex != -1 && 
					(total < doubleDownThresholdAmt || doubleDownThresholdAmt == 0 
					|| briteBoxIndex != -1
					|| couponPresent)){
				
				OrderLineItemCart item = items.get(doubleDownItemIndex);
				
				item.setPrice(item.getCost());
				item.setPromo("");	
				
				orderChanged = true;				
			}
			
			
			if(saveOrder && orderChanged){
				
				/* Calculate order total */
				calculateSummary(order);
				
				/* Save Order*/
				dao.save(order);
			}
			
			
		} catch (Exception e){
			logger.error(Exceptions.giveStackTrace(e));
		}
		
		
	}
	
	
	public String firstOrderCheck(long customerId){
		
		String response = "";
		
		Query<Order> q = completedOrderdao.createQuery()
				.field("status").notEqual("cancelled")
				.field("customer._id").equal(customerId);
		
		if(completedOrderdao.count(q) == 0){
			response = "Y";
		}
		
		return response;
	}
	
	public Address getPrevShippingInfo(long customerId){
		
		Address a = null;
		Query<Order> q = completedOrderdao.createQuery()
				.field("customer._id").equal(customerId)
				.order("-orderNumber").limit(1);
		
		Order o = q.get();
		if(o != null && 
				o.getShipping() != null && 
				o.getShipping().getAddress() != null){
			
			a = o.getShipping().getAddress();
		}		
		
		return a;		
	}
}
