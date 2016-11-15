package com.luvbrite.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	
	
	public Map<String, Boolean> availableDeals(CartOrder order, ControlOptions cOps){
		
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
			
			//Promocode and double down
			boolean couponPresent = false;
			boolean doubleDownApplied = false;
			List<OrderLineItemCart> items = order.getLineItems();
			if(items != null){
				for(OrderLineItemCart item : items){					
					if(item.getPromo() != null){
						
						if("p".equals(item.getPromo()))
								couponPresent = true;
						
						if("doubledownoffer".equals(item.getPromo()))
							doubleDownApplied = true;
							
					}
				}
			}			
			dealStat.put("couponPresent", couponPresent);
			dealStat.put("doubleDownApplied", doubleDownApplied);
			
			
		} catch (Exception e){
			logger.error(Exceptions.giveStackTrace(e));
		}	
		
		return dealStat;
	}
	
	
	
	public void applyDeals(CartOrder order, ControlOptions cOps, boolean saveOrder){
		
		try {

			
			if(order == null) return;

			
			
			double doubleDownThresholdAmt = cOps.getDoubleDown(),
					doubleDownOfferAmt = cOps.getDoubleDownOfferValue(),
					total = 0d;
			
			boolean orderChanged = false,
					couponPresent = false,
					doubleDownWasPresent = false,
					briteBoxEligible = false,
					briteBoxPresent = false;
			
			int index = -1,
				eligibleItemIndex = -1,
				briteBoxIndex = -1;
			
			List<Integer> doubleDownEligibleProducts = cOps.getDoubleDownEligibleProducts();
			
			
			/**
			 * Remove existing doubledown discounts. we will go through
			 * the eligibility check again and will apply the discount
			 * if eligible 
			 * 
			 * If there are items with coupons applied item.promo == 'p'
			 * note them as well, doubleDown offers doesn't combine with
			 * other promos.
			 * 
			 * We don't check if doubleDown is active during the
			 * removal process because items with doubledown offer
			 * has to be removed in all cases.
			 * */
		

			/*Find the total to see if the item is eligible*/
			List<OrderLineItemCart> items = order.getLineItems();
			if(items != null){
				for(OrderLineItemCart item : items){
					
					index++;
					
					if( item.getPromo() != null 
							&& "doubledownoffer".equals(item.getPromo()) ){
						
						doubleDownWasPresent = true;
						
						item.setPromo("");
						item.setPrice(item.getCost());
						
						orderChanged = true;
													
					}
					
					if( item.getPromo() != null 
							&& "p".equals(item.getPromo()) ){
						
						couponPresent = true;
					}
					
					
					if(item.getType().equals("item") && item.isInstock()){
						
						if(item.getProductId() == 11839){
							briteBoxPresent = true;
							briteBoxIndex = index;
						}
						
						double itemPrice = item.getPrice();
						if(itemPrice > 0d){
							total += (itemPrice*item.getQty());								
						}
						
					}
				}
			}
			
			
			if(!couponPresent && !doubleDownWasPresent){
				//First BriteBoxcheck
				if(order.getCustomer() != null && 
						order.getCustomer().get_id() != 0 && 
						total >= 75d){
					
					if(firstOrderCheck(order.getCustomer().get_id())
							.equalsIgnoreCase("Y")){
						briteBoxEligible = true;					
					}
				}
				
				
				if(briteBoxEligible && !briteBoxPresent){
					
					//Add the new item
					OrderLineItemCart newItem = new OrderLineItemCart();
					newItem.setTaxable(false);
					newItem.setType("item");
					newItem.setName("Brite Box");
					newItem.setPromo("firsttimepatient");
					newItem.setProductId(11839);
					newItem.setVariationId(0);
					newItem.setQty(1);
					newItem.setCost(50d);
					newItem.setPrice(0d);
					newItem.setImg("/products/brite-box-img.jpg");

					List<OrderLineItemCart> olic = order.getLineItems();
					olic.add(newItem);
					order.setLineItems(olic);
					
					orderChanged = true;
					briteBoxPresent = true;
				}
			}
			
			
			if(!briteBoxEligible && briteBoxPresent){
				
				//Removed Item
				List<OrderLineItemCart> olic = order.getLineItems();
				olic.remove(briteBoxIndex);
				
				orderChanged = true;	
				briteBoxPresent = false;			
			}
			
			
			/*Item is eligible for double down when order total is >= doubleDownAmt*/
			
			if(doubleDownThresholdAmt > 0d 				&&  
				doubleDownOfferAmt > 0d  				&&
				doubleDownEligibleProducts.size() > 0 	&&
				!couponPresent 							&&  
				!briteBoxPresent						&&  
				total >= doubleDownThresholdAmt) {				

				double eligibleTotal = 0d;
				index = -1;

				for(OrderLineItemCart item : items){
					
					index++;

					if( item.getType().equals("item") 
							&& item.isInstock()
							&& doubleDownEligibleProducts
								.contains(Long.valueOf(item.getProductId()).intValue()) ){

						double itemPrice = item.getPrice();
						if(itemPrice > 0d){
							
							
							/*Check to see if the order qualifies for doubledown even if  
							 *this item (1 qty) is removed */
							if((total - itemPrice) >= doubleDownThresholdAmt){
								/*It qualifies*/

								eligibleTotal = itemPrice;	
								eligibleItemIndex = index;

								break;
							}
							
							/* else keep looking for more items */	
							
						}
						
					}
				}
				
				
				if(eligibleTotal > 0 && eligibleItemIndex > -1){
					
					if(eligibleTotal < doubleDownOfferAmt)
						doubleDownOfferAmt = eligibleTotal;

					OrderLineItemCart item = items.get(eligibleItemIndex);
					
					double newPrice = item.getPrice() - ( doubleDownOfferAmt/item.getQty() );
					
					item.setPrice(newPrice);
					item.setPromo("doubledownoffer");	
					
					orderChanged = true;
				}				
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
