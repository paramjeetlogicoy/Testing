package com.luvbrite.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.luvbrite.dao.CartOrderDAO;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.utils.Utility;
import com.luvbrite.web.models.CartOrder;
import com.luvbrite.web.models.ControlOptions;
import com.luvbrite.web.models.OrderLineItemCart;

@Service
public class CartLogics {
	
	private static Logger logger = LoggerFactory.getLogger(CartLogics.class);
	
	@Autowired
	private CartOrderDAO dao;
	
	public void calculateSummary(CartOrder order){
		
		try {
			
			if(order!=null){

				double subTotal = 0d, 
						total = 0d, 
						discount = 0d,
						couponDiscount = 0d;
				
				int index = -1,
						couponIndex = -1;
				
				boolean couponFound = false;
				
				List<OrderLineItemCart> items = order.getLineItems();
				if(items != null){
					
					for(OrderLineItemCart item : items){
						
						index++;
						
						if(item.getType().equals("item") && item.isInstock()){
							
							double itemPrice = item.getPrice();
							if(itemPrice >= 0d){
								subTotal += (item.getCost()*item.getQty());
								total += (itemPrice*item.getQty());	
								
								if(item.getPromo()!=null && item.getPromo().equals("p")){
									couponFound = true;
									couponDiscount += (subTotal - total);
								}
							}
							
						}
						
						
						if(item.getType().equals("coupon")){
							couponIndex = index;
						}
					}
					
					
					
					if(couponFound && couponIndex > -1){
						
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
	
	
	public void applyDeals(CartOrder order, ControlOptions cOps, boolean saveOrder){
		
		try {


			double doubleDownAmt = cOps.getDoubleDown(),
					offerAmt = cOps.getDoubleDownOfferValue(),
					total = 0d;
			
			boolean orderChanged = false,
					couponPresent = false;
			
			int eligibleItemIndex = -1;
			
			List<Integer> pids = cOps.getDoubleDownEligibleProducts();
			
			
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
			if(order!=null){

				/*Find the total to see if the item is eligible*/
				List<OrderLineItemCart> items = order.getLineItems();
				if(items != null){
					for(OrderLineItemCart item : items){
						
						if( item.getPromo() != null 
								&& "doubledownoffer".equals(item.getPromo()) ){
							
							item.setPromo("");
							item.setPrice(item.getCost());
							
							orderChanged = true;
														
						}
						
						if( item.getPromo() != null 
								&& "p".equals(item.getPromo()) ){
							
							couponPresent = true;
						}
					}
				}
			}
			
			
			/**
			 * Run the order through eligibility check
			 * */
			
			if(order!=null && doubleDownAmt > 0d 
					&& !couponPresent
					&& pids.size() > 0 && offerAmt > 0d){
				
				
				/*Find the total to see if the item is eligible*/
				List<OrderLineItemCart> items = order.getLineItems();
				if(items != null){
					for(OrderLineItemCart item : items){
						
						if(item.getType().equals("item") && item.isInstock()){
							
							double itemPrice = item.getPrice();
							if(itemPrice > 0d){
								total += (itemPrice*item.getQty());								
							}
							
						}
					}
				}
				
				
				/*Item is eligible for double down when order total is >= doubleDownAmt*/
				if(total >= doubleDownAmt){

					double eligibleTotal = 0d;
					int index = -1;

					for(OrderLineItemCart item : items){
						
						index++;

						if( item.getType().equals("item") 
								&& item.isInstock()
								&& pids.contains(Long.valueOf(item.getProductId()).intValue()) ){

							double itemPrice = item.getPrice();
							if(itemPrice > 0d){
								
								
								/*Check to see if the order qualifies for doubledown even if  
								 *this item (1 qty) is removed */
								if((total - itemPrice) >= doubleDownAmt){
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
						
						if(eligibleTotal < offerAmt)
							offerAmt = eligibleTotal;

						OrderLineItemCart item = items.get(eligibleItemIndex);
						
						double newPrice = item.getPrice() - ( offerAmt/item.getQty() );
						
						item.setPrice(newPrice);
						item.setPromo("doubledownoffer");	
						
						orderChanged = true;
					}
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

}
