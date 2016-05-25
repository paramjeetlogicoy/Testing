package com.luvbrite.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.luvbrite.utils.Exceptions;
import com.luvbrite.utils.Utility;
import com.luvbrite.web.models.CartOrder;
import com.luvbrite.web.models.OrderLineItemCart;

@Service
public class CartOrderSummary {
	
	private static Logger logger = LoggerFactory.getLogger(CartOrderSummary.class);
	
	public void calculateSummary(CartOrder order){
		
		try {
			
			if(order!=null){

				double subTotal = 0d;
				double total = 0d;
				double discount = 0d;
				
				List<OrderLineItemCart> items = order.getLineItems();
				if(items != null){
					for(OrderLineItemCart item : items){
						
						if(item.getType().equals("item") && item.isInstock()){
							
							double itemPrice = item.getPrice();
							if(itemPrice > 0d){
								subTotal += (item.getCost()*item.getQty());
								total += (itemPrice*item.getQty());								
							}
							
						}
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

}
