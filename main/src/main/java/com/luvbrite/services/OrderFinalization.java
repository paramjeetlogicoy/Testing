package com.luvbrite.services;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.luvbrite.dao.CartOrderDAO;
import com.luvbrite.dao.OrderDAO;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.web.models.CartOrder;
import com.luvbrite.web.models.Order;
import com.luvbrite.web.models.OrderLineItem;
import com.luvbrite.web.models.OrderLineItemCart;
import com.luvbrite.web.models.OrderNotes;

@Service
public class OrderFinalization {
	
	private static Logger logger = Logger.getLogger(OrderFinalization.class);
	private static boolean offhourPromoActive = true;
	private static boolean valentinesPromoActive = false;
	private static boolean freeGramPromo = true;
	
	private long orderNumber = 0;	
	public long getOrderNumber() {
		return orderNumber;
	}
	
	private Order order;
	public Order getOrder(){
		return order;
	}

	@Autowired
	private OrderDAO dao;
	
	@Autowired
	private CartOrderDAO cartDAO;
	
	
	public String finalizeOrder(CartOrder cartOrder, CartLogics cartLogics){
		String response = "";
		
		try {
			
			if(cartOrder != null 
					&& cartOrder.get_id() != 0 
					&& cartOrder.getStatus().equals("incart")
					&& cartOrder.getLineItems() != null
					&& cartOrder.getLineItems().size() > 0){
				
				//Create new Order
				order = new Order();
				
				//Check for offhour promo
				offHourPromo(cartOrder, cartLogics);
				
				//Check for other promos
				otherPromos(cartOrder, cartLogics);
				
				//First Order Check
				firstOrderCheck(cartOrder, cartLogics);
				
				//Copy info from cartOrder to Order
				copyCartOrder(cartOrder, order);
				
				//Get new orderId, orderNumber
				order.set_id(dao.getNextSeq());
				
				orderNumber = dao.getNextOrderNumber();				
				order.setOrderNumber(orderNumber);
				
				//Set the order status
				order.setStatus("new");
				
				//Save the order.
				dao.save(order);
				
				//Delete or Update CartOrder
				manageCartOrder(cartOrder);
				
				response = "";
			}
			else{
				response = "Not a valid order";
			}
			
		}catch(Exception e){
			
			response = "There was some error creating the order, please try later.";
			logger.error(Exceptions.giveStackTrace(e));
		}

		return response;
	}
	
	private void copyCartOrder(CartOrder co, Order o){
		
		o.setSubTotal(co.getSubTotal());
		o.setTotal(co.getTotal());
		o.setSource(co.getSource());
		o.setDate(Calendar.getInstance().getTime());
		
		if(co.getBilling() != null) 	o.setBilling(co.getBilling());
		if(co.getShipping() != null) 	o.setShipping(co.getShipping());
		if(co.getNotes() != null) 		o.setNotes(co.getNotes());
		if(co.getCustomer() != null) 	o.setCustomer(co.getCustomer());
		

		List<OrderLineItemCart> coItems = co.getLineItems();
		List<OrderLineItemCart> newCoItems = new ArrayList<OrderLineItemCart>();
		List<OrderLineItem> items = new ArrayList<OrderLineItem>();
		
		if(coItems != null){
			
			for(OrderLineItemCart coItem : coItems){
				if(coItem.isInstock()) {
					items.add(coItem);					
				}	
				else{
					newCoItems.add(coItem);
				}
			}
			
			o.setLineItems(items);
			co.setLineItems(newCoItems);
		}
	}
	
	/**
	 * If the order is placed during offhours, add the free item
	 * to the order 
	 * */
	private void offHourPromo(CartOrder co, CartLogics cartLogics){
		
		if(offhourPromoActive){
			
			//Check if it off hour
			Calendar now = Calendar.getInstance();
			int hour = now.get(Calendar.HOUR_OF_DAY);
			int minute = now.get(Calendar.MINUTE);
			
			if((hour == 22 && minute >= 29) || (hour >= 23) || (hour <= 10)){
				
				//Add the new item
				OrderLineItemCart newItem = new OrderLineItemCart();
				newItem.setTaxable(false);
				newItem.setType("item");
				newItem.setName("Kiva Milk or Dark Chocolate (Offhour Promo)");
				newItem.setPromo("offhourpromo");
				newItem.setProductId(11825);
				newItem.setVariationId(0);
				newItem.setQty(1);
				newItem.setCost(10d);
				newItem.setPrice(0d);
				newItem.setImg("/uploads/2015/04/edibles-kiva-confections-large.jpg");

				List<OrderLineItemCart> olic = co.getLineItems();
				olic.add(newItem);
				co.setLineItems(olic);				
				
				//Update orderTotals
				cartLogics.calculateSummary(co);
			}
			
			//System.out.println("Time - " + hour);
		}
	}
	
	
	/**
	 * Check for other adhoc promos 
	 * */
	private void otherPromos(CartOrder co, CartLogics cartLogics){
		
		if(valentinesPromoActive && co.getTotal() >= 120d){
				
			//Add the new item
			OrderLineItemCart newItem = new OrderLineItemCart();
			newItem.setTaxable(false);
			newItem.setInstock(true);
			newItem.setType("item");
			newItem.setName("Sensi Chew Amore – Aphrodisiac");
			newItem.setPromo("ValentinesDayPromos");
			newItem.setProductId(6098);
			newItem.setVariationId(0);
			newItem.setQty(1);
			newItem.setCost(13d);
			newItem.setPrice(0d);
			newItem.setImg("/uploads/2015/12/sensi-chew-amore.jpg");

			List<OrderLineItemCart> olic = co.getLineItems();
			olic.add(newItem);
			co.setLineItems(olic);				
			
			//Update orderTotals
			cartLogics.calculateSummary(co);
		}
		
		if(freeGramPromo){
				
			//Add the new item
			OrderLineItemCart newItem = new OrderLineItemCart();
			newItem.setTaxable(false);
			newItem.setInstock(true);
			newItem.setType("item");
			newItem.setName("1 Gram - Assorted Variety");
			newItem.setPromo("Free Gram Promo");
			newItem.setProductId(10504);
			newItem.setVariationId(0);
			newItem.setQty(1);
			newItem.setCost(20d);
			newItem.setPrice(0d);
			newItem.setImg("/products/1GramAssortedVariety.jpg");

			List<OrderLineItemCart> olic = co.getLineItems();
			olic.add(newItem);
			co.setLineItems(olic);				
			
			//Update orderTotals
			cartLogics.calculateSummary(co);
		}
	}
	
	/**
	 * Check is this is customers first order and update order notes accordingly
	 * Also add the britebox to the order
	 **/
	
	private void firstOrderCheck(CartOrder co, CartLogics cartLogics){
			
		String response = cartLogics.firstOrderCheck(co.getCustomer().get_id());
		if(response.equals("Y")){

			OrderNotes notes = co.getNotes();
			if(notes == null) notes = new OrderNotes();
			if(notes.getAdditonalNotes()==null){
				notes.setAdditonalNotes("**FIRST ORDER**");
			}
			else {
				notes.setAdditonalNotes(notes.getAdditonalNotes() + "**FIRST ORDER**");
			}

			co.setNotes(notes);
		}	
	};
	
	/**
	 * Delete the cart order if all the items are copied to the
	 * placed order. If any item remains, set ordertotal to zero and 
	 * keep it. 
	 * */
	private void manageCartOrder(CartOrder co){
		if(co.getLineItems()!=null && co.getLineItems().size()>0){
			co.setSubTotal(0d);
			co.setTotal(0d);
			co.getBilling().getPmtMethod().setCardData(null);
			co.setNotes(null);

			cartDAO.save(co);			
		}

		else {
			//Delete Cartorder
			cartDAO.deleteById(co.get_id());
		}
	}
	
}
