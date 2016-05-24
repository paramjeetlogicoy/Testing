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
				
				double subCost = 0d;
				double subTotal = 0d;
				double total = 0d;
				double discount = 0d;
				
				List<OrderLineItemCart> items = order.getLineItems();
				if(items != null){
					for(OrderLineItemCart item : items){
						
						if(item.getType().equals("item") && item.isInstock()){
							
							double itemPrice = item.getPrice();
							if(itemPrice > 0d){
								subCost += (item.getCost()*item.getQty());
								subTotal += (itemPrice*item.getQty());								
							}
							
						}
					}
				}
				
				if(subTotal < 0d) subTotal = 0d;
				if(subCost < 0d) subCost = 0d;
				
				subCost = Utility.Round(subCost, 2);
				subTotal = Utility.Round(subTotal, 2);
				
				discount = subCost - subTotal;				
				if(discount > subTotal)
					discount = subTotal;
				
				total = subTotal - discount;
				
				order.setSubTotal(subTotal);
				order.setTotal(total);	
			}
			
			
		} catch (Exception e){
			logger.error(Exceptions.giveStackTrace(e));
		}
		
		
	}

}
