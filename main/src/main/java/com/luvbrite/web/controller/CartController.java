package com.luvbrite.web.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.luvbrite.dao.CartOrderDAO;
import com.luvbrite.dao.LogDAO;
import com.luvbrite.dao.PriceDAO;
import com.luvbrite.dao.ProductDAO;
import com.luvbrite.dao.UserDAO;
import com.luvbrite.services.CartLogics;
import com.luvbrite.services.CouponManager;
import com.luvbrite.services.OrderFinalization;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.utils.Utility;
import com.luvbrite.web.models.AttrValue;
import com.luvbrite.web.models.CartOrder;
import com.luvbrite.web.models.CartResponse;
import com.luvbrite.web.models.ControlOptions;
import com.luvbrite.web.models.CreateOrderResponse;
import com.luvbrite.web.models.GenericResponse;
import com.luvbrite.web.models.Log;
import com.luvbrite.web.models.OrderCustomer;
import com.luvbrite.web.models.OrderLineItemCart;
import com.luvbrite.web.models.OrderNotes;
import com.luvbrite.web.models.Price;
import com.luvbrite.web.models.Product;
import com.luvbrite.web.models.User;
import com.luvbrite.web.models.UserDetailsExt;


@Controller
@RequestMapping(value = "/cart")
public class CartController {
	
	private static Logger logger = LoggerFactory.getLogger(CartController.class);

	@Autowired
	private CartOrderDAO dao;
	
	@Autowired
	private UserDAO userDao;
	
	@Autowired
	private PriceDAO priceDao;
	
	@Autowired
	private ProductDAO prdDao;
	
	@Autowired
	private LogDAO logDao;
	
	@Autowired
	private CartLogics cartLogics;
	
	@Autowired
	private CouponManager couponManager;
	
	@Autowired
	private OrderFinalization orderFinalization;
	
	@Autowired
	private ControlOptions controlOptions;
	
	@RequestMapping(method = RequestMethod.GET)
	public String homePage(@AuthenticationPrincipal 
			UserDetailsExt user, 
			ModelMap model){
		
		if(user!=null)
			model.addAttribute("userId", user.getId());
		
		return "cart";		
	}	
	
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public @ResponseBody CreateOrderResponse addToCart(
			@CookieValue(value = "lbbagnumber", defaultValue = "0") String orderIdS, 
			@RequestBody OrderLineItemCart lineItem){

		CreateOrderResponse r = new CreateOrderResponse();
		r.setSuccess(false);
		
		//System.out.println("orderIdS:" + orderIdS + ";");
		
		try {
			
			if(lineItem == null 
					|| lineItem.getProductId() == 0 
					|| lineItem.getQty() == 0){
				
				r.setMessage("Invalid product or quantity.");
			}
			
			else {
				/**
				 * Check if there is an existing order.
				 * If yes, add item to that order, else
				 * create an order
				 **/
				
				boolean createOrder = false,
						proceed = true;
				CartOrder order = new CartOrder();
				
				long orderId = Utility.getLong(orderIdS);
				if(orderId == 0l){
					createOrder = true;
				}				
				else {					
					order = dao.get(orderId);
					if(order == null || !order.getStatus().equals("incart")){
						createOrder = true;
					}
				}
				
				if(createOrder){
					
					orderId = dao.getNextSeq();
					if(orderId != 0l){

						order = new CartOrder();
						order.set_id(orderId);
						order.setOrderNumber(0);
						order.setDate(Calendar.getInstance().getTime());
						order.setStatus("incart");
						order.setSource("checkout");
					}
					else{
						proceed = false;
						r.setMessage("There was some error creating the order, please try later.");
					}
				}
				
				
				
				if(proceed){
					
					int totalItems = 0,
							productItems = 0;
					
					long pid = lineItem.getProductId(),
							vid = lineItem.getVariationId();
					
					//Check if the same item is already in the order
					boolean itemFound = false;
					String couponCode = "";
					List<OrderLineItemCart> items = order.getLineItems();
					if(items != null){
						for(OrderLineItemCart item : items){
							
							if(item.getProductId() == pid &&
									item.getVariationId() == vid ){
								
								int currQty = item.getQty();
								int newQty = currQty + lineItem.getQty();
								
								item.setQty(newQty);
								productItems = newQty;
								
								itemFound = true;						
							}
							
							/*totalItems refers to the cart count*/
							if(item.getType().equals("item")
									&& item.isInstock()){
								totalItems+= item.getQty();
							}
							
							else if(item.getType().equals("coupon")){
								couponCode = item.getName();
							}
						}
					}
					
					
					/**
					 * If item doesn't exist in the cart already,
					 * we build the item and add it to CartOrder.lineItems
					 **/					
					if(!itemFound){
						if(items == null)
							items = new ArrayList<OrderLineItemCart>();

						OrderLineItemCart newItem = buildNewItem(lineItem, pid, vid);
						if(newItem != null){
							
							items.add(newItem);
							
							/*totalItems refers to the cart count*/
							totalItems+= newItem.getQty();
							
							productItems = newItem.getQty();
						}
					}
					
					
					/*Update order with lineItems*/
					order.setLineItems(items);
					
					
					/* Reapply coupons if any! */
					if(!couponCode.equals("")){
						couponManager.reapplyCoupon(couponCode, order, false);
					}

					
					/**
					 * We don't run the deal check here because cart is
					 * not shown during addToCart function. Also, we check for 
					 * deals during every cart load, so it's not required here. 
					 **/
					
					
					/*Update orderTotals*/
					cartLogics.calculateSummary(order);
					
					dao.save(order);
					
					r.setSuccess(true);
					r.setCartCount(totalItems);
					r.setProductCount(productItems);
					r.setOrderId(orderId);					
				}
			}
			
		}catch(Exception e){
			
			r.setMessage("There was some error creating the order, please try later.");
			logger.error(Exceptions.giveStackTrace(e));
		}
		
		return r;	
	}
	
	@SuppressWarnings("unused")
	private boolean specsMatch(List<AttrValue> specs1, List<AttrValue> specs2){
		
		boolean result = false;
		
		if(specs1 == null && specs2 == null){
			return true;
		}
		
		if(specs1.size() != specs2.size()){
			return false;
		}
		
		try {
			
			boolean itemsMatch = true;
			for(int i=0; i<specs1.size(); i++){
				if(!specs1.get(i).getAttr().equals( specs2.get(i).getAttr() ) 
						|| !specs1.get(i).getValue().equals( specs2.get(i).getValue() )){
					itemsMatch = false;
					break;
				}
			}
			
			result = itemsMatch;
		}
		catch(Exception e){
			result = false;
		}
		
		
		return result;
	}
	

		
	
	@RequestMapping(value = "/productInCart", method = RequestMethod.GET)
	public @ResponseBody GenericResponse productInCart(
			@CookieValue(value = "lbbagnumber", defaultValue = "0") String orderIdS,
			@RequestParam(value="pid", required=false) Integer productId) {
		
		GenericResponse gr = new GenericResponse();
		gr.setSuccess(false);
		
		int qty = 0;
		if(productId == null) productId = 0;
		
		if(orderIdS!=null && productId != 0){
			CartOrder order = dao.createQuery()
					.field("_id").equal(Utility.getLong(orderIdS))
					.retrievedFields(true, "lineItems.qty", "lineItems.pid", "lineItems.instock")
					.get();
			
			if(order!=null && order.getLineItems() != null){
				for(OrderLineItemCart li : order.getLineItems()){
					if(li.getProductId() == productId && li.isInstock()){
						qty += li.getQty();
						gr.setSuccess(true);
					}
				}
			}
		}
 
		gr.setMessage(qty+"");
		
		return gr;
	}
	
	
	
	
	/**
	 * 
	 * Get order details
	 *  
	 **/	
	@RequestMapping(value = "/getcart/{orderId}")
	public @ResponseBody CartOrder orderDetails(@PathVariable long orderId){
		
		return findOrder(orderId);			
	}
	
	/**
	 * Get the cart orderdetails for Cart
	 * order number is pulled from the cookie
	 * User details are pulled from Principal 
	 **/
	@RequestMapping(value = "/getcart")
	public @ResponseBody CartResponse getCart(
			@AuthenticationPrincipal UserDetailsExt user,
			@CookieValue(value = "lbbagnumber", defaultValue = "0") String orderIdS){
		
		CartResponse cr = new CartResponse();
		CartOrder order = findOrder(Utility.getLong(orderIdS));
		
		
		/*If userinfo is available, update the order with it.*/
		if(order !=null && user != null){
			User userDb = userDao.get(user.getId());
			
			if(userDb != null){
				
				cr.setUser(userDb);
				
				OrderCustomer cus = new OrderCustomer();
				cus.set_id(userDb.get_id());
				cus.setEmail(userDb.getEmail());
				cus.setName(userDb.getFname() + " " + userDb.getLname());
	
				order.setCustomer(cus);
				dao.save(order);
			}
		}
		
		/**
		 * Since we need to remove the deals if they are inactive,
		 * we have to check for it every time the cart loads, not just
		 * during cart updates. 
		 **/
		cartLogics.applyDeals(order, controlOptions, true);
		
		
		cr.setOrder(order);
		cr.setConfig(controlOptions);
		
		return cr;			
	}
	
	
	private CartOrder findOrder(long orderId){
		return dao.findCartOrder(orderId);
	}
	
	
	
	@RequestMapping(value = "/{orderId}/savedeliverynote", method = RequestMethod.POST)
	public @ResponseBody GenericResponse saveDeliveryNote(
			@PathVariable long orderId, @RequestBody OrderNotes notes){
		
		GenericResponse gr = new GenericResponse();
		gr.setSuccess(false);
		gr.setMessage("");

		try {

			if(notes != null && notes.getDeliveryNotes() != null){
				
				CartOrder currOrder = dao.findCartOrder(orderId);
				if(currOrder != null){
					OrderNotes or = currOrder.getNotes();
					
					if(or==null)	
						or = new OrderNotes();
					
					or.setDeliveryNotes(notes.getDeliveryNotes());
					
					currOrder.setNotes(or);;
					dao.save(currOrder);
					
					gr.setSuccess(true);
				}
				else{
					gr.setMessage("Order not found.");
				}
			}
			else{
				gr.setMessage("Notes parameter is null.");
			}
			
		}catch(Exception e){

			gr.setMessage("There was some error updating the address, please try later.");
			logger.error(Exceptions.giveStackTrace(e));
		}
		
		return gr;
	}
	
	
	
	@RequestMapping(value = "/{orderId}/savedeliveryaddr", method = RequestMethod.POST)
	public @ResponseBody GenericResponse saveDeliveryAddr(
			@PathVariable long orderId, @RequestBody User user){
		
		GenericResponse gr = new GenericResponse();
		gr.setSuccess(false);
		gr.setMessage("");

		try {

			if(user == null 
					|| user.getBilling() == null 
					|| user.getBilling().getPhone() == null){

				gr.setMessage("Please fill in all mandatory fields.");				
			}
			else{
				
				CartOrder order = dao.findCartOrder(orderId);
				if(order == null){
					gr.setMessage("Invalid order id. Please refresh the page and try again.");
				}
				
				else {
					
					order.setBilling(user.getBilling());
					dao.save(order);
					
					gr.setSuccess(true);
				}				
				
			}
			
		}catch(Exception e){

			gr.setMessage("There was some error updating the address, please try later.");
			logger.error(Exceptions.giveStackTrace(e));
		}
		
		return gr;
	}
	
	
	
	@RequestMapping(value = "/{orderId}/placeorder", method = RequestMethod.POST)
	public @ResponseBody GenericResponse placeOrder(
			@PathVariable long orderId, @RequestBody CartOrder order){
		
		GenericResponse gr = new GenericResponse();
		gr.setSuccess(false);
		gr.setMessage("");

		try {

			if(order == null 
					|| orderId == 0
					|| order.get_id() == 0  
					|| orderId != order.get_id() 
					|| order.getLineItems() == null 
					|| order.getLineItems().size() == 0){

				gr.setMessage("Unable to find any valid order. Please contact the customer care.");				
			}
			else{
				
				CartOrder currOrder = dao.findCartOrder(orderId);
				if(currOrder == null){
					gr.setMessage("Invalid order id. "
							+ "Please refresh the page and try again. "
							+ "If problem persists, please contact customer care.");
				}				
				else {					
					String resp = orderFinalization.finalizeOrder(order);
					if("".equals(resp)){
						gr.setSuccess(true);
						gr.setMessage(orderFinalization.getOrderNumber()+"");
					}
					else{
						gr.setMessage(resp);
					}
				}				
				
			}
			
		}catch(Exception e){

			gr.setMessage("There was some error updating the address, please try later.");
			logger.error(Exceptions.giveStackTrace(e));
		}
		
		return gr;
	}
	
	
	
	@RequestMapping(value = "/updatecart", method = RequestMethod.GET)
	public @ResponseBody CreateOrderResponse updateCart(
			@RequestParam(value="oid", required=false) Long orderId,
			@RequestParam(value="vid", required=false) Integer variationId,
			@RequestParam(value="pid", required=false) Integer productId,
			@RequestParam(value="qty", required=false) Integer qty){
		
		CreateOrderResponse cr = new CreateOrderResponse();
		cr.setSuccess(false);
		cr.setMessage("");
		
		try {
			//Validations
			if(orderId == null || orderId == 0){
				cr.setMessage(" - Invalid order ID.");
			}
			
			if((variationId == null || variationId == 0) 
					&& (productId == null || productId == 0) ){
				cr.setMessage(cr.getMessage() + " - Invalid item/product ID.");
			}
			
			if(qty == null || qty == 0){
				cr.setMessage(cr.getMessage() + " - Invalid quantity.");
			}
			
			if(cr.getMessage().length()>1){
				return cr;
			}
			
			
			CartOrder order = dao.get(orderId);
			
			int totalItems = 0;
			boolean itemFound = false;
			String couponCode = "";
			List<OrderLineItemCart> items = order.getLineItems();
			if(items != null){
				for(OrderLineItemCart item : items){
					if(item.getProductId() == productId 
							&& item.getVariationId() == variationId){
						
						int oldQty = item.getQty();
						item.setQty(qty);
						
						totalItems+= item.getQty();
						
						
						/**
						 * Update Log
						 * */
						try {
							
							Log log = new Log();
							log.setCollection("cartorders");
							log.setDetails("Order quantity updated from " + oldQty + " to " + qty);
							log.setDate(Calendar.getInstance().getTime());
							log.setKey(order.get_id());
							log.setUser("customer");
							
							logDao.save(log);					
						}
						catch(Exception e){
							logger.error(Exceptions.giveStackTrace(e));
						}
						
						
						
						
						itemFound = true;
					}
					
					else {
						
						if(item.getType().equals("item")
								&& item.isInstock()){
							totalItems+= item.getQty();
						}
						
						else if(item.getType().equals("coupon")){
							couponCode = item.getName();
						}
					}
				}
			}
			
			if(itemFound){
				
				/* Reapply coupons if any! */
				if(!couponCode.equals("")){
					couponManager.reapplyCoupon(couponCode, order, false);
				}
				
				
				//Run through deal logic
				cartLogics.applyDeals(order, controlOptions, false);
				
				
				//Update orderTotals
				cartLogics.calculateSummary(order);
				
				//Save order
				dao.save(order);		

				cr.setSuccess(true);
				cr.setCartCount(totalItems);			
				cr.setOrder(order);
			}
			
			else{
				cr.setMessage("Item was not found in the cart. Please fresh the page and try again.");
			}
			
			
			
		}catch(Exception e){
			logger.error(Exceptions.giveStackTrace(e));
			cr.setMessage(e.getMessage());
		}
		
		
		return cr;
	}
	
	
	
	@RequestMapping(value = "/removecoupon/{couponCode}", method = RequestMethod.GET)
	public @ResponseBody GenericResponse removeCoupon(
			@PathVariable String couponCode,
			@RequestParam(value="oid", required=false) Long orderId){
		
		return couponManager.removeCoupon(couponCode, orderId);
	}
	
	
	
	@RequestMapping(value = "/applycoupon/{couponCode}", method = RequestMethod.GET)
	public @ResponseBody GenericResponse applyCoupon(
			@PathVariable String couponCode,
			@RequestParam(value="oid", required=false) Long orderId){
		
		return couponManager.applyCoupon(couponCode, orderId);
	}
	
	
	
	@RequestMapping(value = "/removeitem", method = RequestMethod.DELETE)
	public @ResponseBody CreateOrderResponse removeItem(
			@RequestParam(value="oid", required=false) Long orderId,
			@RequestParam(value="vid", required=false) Integer variationId){
		
		CreateOrderResponse cr = new CreateOrderResponse();
		cr.setSuccess(false);
		cr.setMessage("");
		
		try {
			//Validations
			if(orderId == null || orderId == 0){
				cr.setMessage(" - Invalid order ID.");
			}
			
			if(variationId == null || variationId == 0){
				cr.setMessage(cr.getMessage() + " - Invalid item ID.");
			}
			
			if(cr.getMessage().length()>1){
				return cr;
			}
			
			
			CartOrder order = dao.get(orderId);
			
			int totalItems = 0;
			boolean itemFound = false;
			String couponCode = "";		

			List<OrderLineItemCart> items = order.getLineItems();
			if(items != null){
				Iterator<OrderLineItemCart> iter = items.iterator();
				
				while(iter.hasNext()){
					OrderLineItemCart item = iter.next();
					if(item.getVariationId() == variationId){
						iter.remove();
							
						/*Update Log*/
						try {
							
							Log log = new Log();
							log.setCollection("cartorders");
							log.setDetails("Item removed - " + item.getName() 
									+ ". Var Id - " + variationId);
							log.setDate(Calendar.getInstance().getTime());
							log.setKey(order.get_id());
							log.setUser("customer");
							
							logDao.save(log);					
						}
						catch(Exception e){
							logger.error(Exceptions.giveStackTrace(e));
						}
						
						
						itemFound = true;
						
						break;
					}
				}
			}
			
			if(itemFound){
				
				items = order.getLineItems();
				if(items != null){
					for(OrderLineItemCart item : items){						
						if(item.getType().equals("item")
								&& item.isInstock()){
							totalItems+= item.getQty();
						}
						
						
						if(item.getType().equals("coupon")){
							couponCode = item.getName();
						}
					}
				}
				
				
				/* Reapply coupons if any! */
				if(!couponCode.equals("")){
					couponManager.reapplyCoupon(couponCode, order, false);
				}				
				
				
				//Run through deal logic
				cartLogics.applyDeals(order, controlOptions, false);
				
				
				//Update orderTotals
				cartLogics.calculateSummary(order);
				
				
				//Save order
				dao.save(order);		

				cr.setSuccess(true);
				cr.setCartCount(totalItems);			
				cr.setOrder(order);
			}
			
			else{
				cr.setMessage("Item was not found in the cart. Please fresh the page and try again.");
			}
			
			
			
		}catch(Exception e){
			logger.error(Exceptions.giveStackTrace(e));
			cr.setMessage(e.getMessage());
		}
		
		
		return cr;
	}
	
	
	
	/**
	 * Get cart count
	 **/
	@RequestMapping(value = "/getCount")
	public @ResponseBody CreateOrderResponse cartCount(
			@CookieValue(value = "lbbagnumber", defaultValue = "0") String orderIdS){
		
		CreateOrderResponse r = new CreateOrderResponse();
		int cartCount = 0;
		
		try {
			
			cartCount = returnCartCount(Utility.getLong(orderIdS));
			
		}catch(Exception e){			
			r.setMessage(e.getMessage());
			logger.error(Exceptions.giveStackTrace(e));			
		}
		
		r.setCartCount(cartCount);
		
		return r;				
	}	
	
	
	private int returnCartCount(long orderId){
		
		int cartCount = 0;
		try {
			
			if(orderId != 0){
				CartOrder order = dao.get(orderId);
				if(order != null){
					List<OrderLineItemCart> items = order.getLineItems();
					if(items != null){
						for(OrderLineItemCart item : items){							
							if(item.getType().equals("item") 
									&& item.isInstock()){
								cartCount+= item.getQty();
							}
						}
					}
				}
			}
			
		}catch(Exception e){			
			logger.error(Exceptions.giveStackTrace(e));			
		}

		return cartCount;
		
	}
	
	private OrderLineItemCart buildNewItem(OrderLineItemCart lineItem, long pid, long vid){
		
		boolean pricesFound = false;
		
		OrderLineItemCart newItem = new OrderLineItemCart();
		newItem.setTaxable(false);
		newItem.setType("item");
		newItem.setName(lineItem.getName());
		newItem.setProductId(pid);
		newItem.setVariationId(vid);
		newItem.setQty(lineItem.getQty());
		
		/*If variation Id is not zero, get specs from price collection*/
		if(vid!=0){
			Price price = priceDao.createQuery()
					
						.field("pid").equal(pid)
						.field("_id").equal(vid)
						
						.get();
			
			if(price != null){
				pricesFound = true;
				
				double cost = price.getRegPrice(),
						salePrice = price.getSalePrice();
				
				if(salePrice == 0){
					salePrice = cost;
				}
				else if(salePrice < cost){
					newItem.setPromo("s");
				}
				
				newItem.setCost(cost);
				newItem.setPrice(salePrice);
				
				if(price.getImg() != null) newItem.setImg(price.getImg());
				else if(lineItem.getImg() != null) newItem.setImg(lineItem.getImg());
				
				if(price.getVariation() != null) newItem.setSpecs(price.getVariation());
				
			}
		}
		else{
			
			Product product = prdDao.get(pid);
			if(product != null){
				pricesFound = true;
				
				double cost = product.getPrice(),
						salePrice = product.getSalePrice();
				
				if(salePrice == 0)	salePrice = cost;
				
				newItem.setCost(cost);
				newItem.setPrice(salePrice);
				newItem.setSpecs(null);
				
				if(product.getFeaturedImg() != null) newItem.setImg(product.getFeaturedImg());
			}
		}
		
		
		if(!pricesFound) {
			logger.error("No prices found while adding to cart ProductId:" 
					+ pid + " , VariationId:" 
					+ vid +".");
			
			return null;
		}
		
		return newItem;
	}
}
