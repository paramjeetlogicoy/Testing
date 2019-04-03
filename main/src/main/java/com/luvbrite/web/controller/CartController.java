package com.luvbrite.web.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
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
import com.luvbrite.dao.CouponDAO;
import com.luvbrite.dao.LogDAO;
import com.luvbrite.dao.PriceDAO;
import com.luvbrite.dao.ProductDAO;
import com.luvbrite.dao.UserDAO;
import com.luvbrite.dao.ZiptaxDAO;
import com.luvbrite.services.CartLogics;
import com.luvbrite.services.ControlConfigService;
import com.luvbrite.services.CouponManager;
import com.luvbrite.services.EmailService;
import com.luvbrite.services.OrderFinalization;
import com.luvbrite.services.PostOrderMeta;
import com.luvbrite.services.paymentgateways.SquareUp;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.utils.Utility;
import com.luvbrite.web.models.Address;
import com.luvbrite.web.models.AttrValue;
import com.luvbrite.web.models.CartOrder;
import com.luvbrite.web.models.CartResponse;
import com.luvbrite.web.models.Coupon;
import com.luvbrite.web.models.CreateOrderResponse;
import com.luvbrite.web.models.DoubleDownProducts;
import com.luvbrite.web.models.Email;
import com.luvbrite.web.models.GenericResponse;
import com.luvbrite.web.models.Log;
import com.luvbrite.web.models.Order;
import com.luvbrite.web.models.OrderCustomer;
import com.luvbrite.web.models.OrderLineItemCart;
import com.luvbrite.web.models.OrderNotes;
import com.luvbrite.web.models.OrderPlaceResponse;
import com.luvbrite.web.models.OrderTax;
import com.luvbrite.web.models.Price;
import com.luvbrite.web.models.Product;
import com.luvbrite.web.models.Shipping;
import com.luvbrite.web.models.TaxComponent;
import com.luvbrite.web.models.User;
import com.luvbrite.web.models.UserDetailsExt;
import com.luvbrite.web.models.Ziptax;
import com.luvbrite.web.models.squareup.AmountMoney;
import com.luvbrite.web.models.squareup.Charge;


@Controller
@RequestMapping(value = "/cart")
public class CartController {
	
	private static Logger logger = Logger.getLogger(CartController.class);

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
	private CouponDAO couponDao;
	
	@Autowired
	private ZiptaxDAO taxDao;
	
	@Autowired
	private CartLogics cartLogics;
	
	@Autowired
	private CouponManager couponManager;
	
	@Autowired
	private OrderFinalization orderFinalization;
	
	@Autowired
	private ControlConfigService ccs;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private PostOrderMeta postOrderMeta;
	
	@Autowired
	private SquareUp paymentService;
	
	@RequestMapping(method = RequestMethod.GET)
	public String homePage(@AuthenticationPrincipal 
			UserDetailsExt user, 
			ModelMap model){
		
		if(user!=null && user.isEnabled())
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
						couponManager.reapplyCoupon(couponCode, order, false, "additem");
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
	
	
	@RequestMapping(value = "/add/multi/", method = RequestMethod.POST)
	public @ResponseBody CreateOrderResponse addToCartMulti(
			@CookieValue(value = "lbbagnumber", defaultValue = "0") String orderIdS, 
			@RequestBody List<OrderLineItemCart> lineItems){

		CreateOrderResponse r = new CreateOrderResponse();
		r.setSuccess(false);
		
		//System.out.println("orderIdS:" + orderIdS + ";");
		
		try {
			
			if(lineItems == null 
					|| lineItems.size() == 0){
				
				r.setMessage("Invalid items.");
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

					List<OrderLineItemCart> items = order.getLineItems();
					String couponCode = "";
					
					for(OrderLineItemCart requestitem : lineItems){

						long pid = requestitem.getProductId(),
								vid = requestitem.getVariationId();
						
						//Check if the same item is already in the order
						boolean itemFound = false;
						
						if(items != null){
							for(OrderLineItemCart item : items){
								
								if(item.getProductId() == pid &&
										item.getVariationId() == vid ){
									
									int currQty = item.getQty();
									int newQty = currQty + requestitem.getQty();
									
									item.setQty(newQty);
									productItems+= newQty;
									
									itemFound = true;						
								}
								
								if(item.getType().equals("coupon")){
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

							OrderLineItemCart newItem = buildNewItem(requestitem, pid, vid);
							if(newItem != null){
								
								items.add(newItem);
								
								productItems+= newItem.getQty();
							}
						}					
						
					}
					
					
					/* Calculate total Items in the cart */
					for(OrderLineItemCart item : items){		
						if(item.getType().equals("item")
								&& item.isInstock()){
							totalItems+= item.getQty();
						}
					}
					
					/*Update order with lineItems*/
					order.setLineItems(items);
					
					
					/* Reapply coupons if any! */
					if(!couponCode.equals("")){
						couponManager.reapplyCoupon(couponCode, order, false, "additem");
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
	
	
	@RequestMapping(value = "/adddoubledown/{productId}/{varitaionId}", method = RequestMethod.POST)
	public @ResponseBody CreateOrderResponse addDoubleDownItem(
			@CookieValue(value = "lbbagnumber", defaultValue = "0") String orderIdS, 
			@PathVariable Long productId, 
			@PathVariable Long varitaionId,
			HttpSession sess){

		CreateOrderResponse r = new CreateOrderResponse();
		r.setSuccess(false);
		/*double doubleDownThresholdAmt = ccs.getcOps().getDoubleDown(),
				doubleDownOfferAmt = ccs.getcOps().getDoubleDownOfferValue();*/
		
		//System.out.println("orderIdS:" + orderIdS + ";");
		
		try {
			
			if(productId == null 
					|| productId == 0){
				
				r.setMessage("There is something wrong with this product. "
						+ "Please select a different product, or contact support team.");
			}
			
			else {
				
				
				/** Find the product **/
				Product product = prdDao.get(productId);
				if(product == null){
					r.setMessage("This product doesn't seems to be valid. "
						+ "Please select a different product, or contact support team.");
				}				
				else {
						
					/** If its simple, create the lineItem **/
					OrderLineItemCart lineItem = new OrderLineItemCart();
					lineItem.setName(product.getName());
					lineItem.setQty(1);
					lineItem.setProductId(productId);
					lineItem.setImg(product.getFeaturedImg());
					lineItem.setVariationId(varitaionId);
					
					long orderId = Utility.getLong(orderIdS);
					CartOrder order = dao.get(orderId);
					if(order == null || !order.getStatus().equals("incart")){
						r.setMessage("No valid order found.");
					}		
					
					else {
						
						int totalItems = 0;
						
						long pid = lineItem.getProductId(),
								vid = lineItem.getVariationId();
						
						//Check if the same item is already in the order
						boolean itemFound = false;

						List<OrderLineItemCart> removeItems = new ArrayList<>();
						List<OrderLineItemCart> items = order.getLineItems();
						if(items != null){
							for(OrderLineItemCart item : items){
								
								if(item.getProductId() == pid && 
										item.getVariationId() == vid){
									
									/**
									 * if the item is already in the cart, we need to see if just 
									 * applying the discount is enough, if thats the case, then we won't
									 * change the quantity.
									 * 
									 * Else if applying the discount brings the total order value below 
									 * threshold then we will update the quantity by 1
									 * */
									
									//Scrapped the above/below logic for now
									
									//If we discount the item and it goes below threshold, then increase quantity 
									/*if( (order.getTotal() - doubleDownOfferAmt) < doubleDownThresholdAmt){
										item.setQty(item.getQty()+1);
									}*/
									
									item.setQty(item.getQty()+1);
									item.setPromo("doubledownoffer");
									
									itemFound = true;						
								}
								
								
								//Remove Holiday Promo if present
								if( item.getPromo() != null 
										&& "fifthflower".equals(item.getPromo()) ){
									removeItems.add(item);
								}

								// Remove britebox if present
								else if( item.getPromo() != null 
										&& "firsttimepatient".equals(item.getPromo()) ){
									removeItems.add(item);
								}
								
								/*totalItems refers to the cart count*/
								if(item.getType().equals("item")
										&& item.isInstock()){
									totalItems+= item.getQty();
								}
							}
						}
						
						

						if(removeItems.size() > 0){
							items.removeAll(removeItems);
							totalItems = totalItems - removeItems.size();
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

								newItem.setPromo("doubledownoffer");
								items.add(newItem);
								
								/*totalItems refers to the cart count*/
								totalItems+= newItem.getQty();
							}
						}
						
						
						/*Update order with lineItems*/
						order.setLineItems(items);
						
						
						//Run through deal logic
						cartLogics.applyDeals(order, ccs.getcOps(), false, sess);
						
						
						/*Update orderTotals*/
						cartLogics.calculateSummary(order);
						
						dao.save(order);
						
						r.setSuccess(true);
						r.setCartCount(totalItems);				
						r.setOrder(order);				
					}
				
				}
				
			}
			
		}catch(Exception e){
			
			r.setMessage("There was some error creating the order, please try later.");
			logger.error(Exceptions.giveStackTrace(e));
		}
		
		return r;	
	}
	
	
	/** This method is not called anymore! **/
	@RequestMapping(value = "/addbritebox", method = RequestMethod.POST)
	public @ResponseBody CreateOrderResponse addBriteBoxItem(
			@CookieValue(value = "lbbagnumber", defaultValue = "0") String orderIdS,
			HttpSession sess){

		CreateOrderResponse r = new CreateOrderResponse();
		r.setMessage("Britebox offer no longer exists");
		r.setSuccess(false);
		
		return r;	
	}
	
	
	@RequestMapping(value = "/add-assorted-variety", method = RequestMethod.POST)
	public @ResponseBody CreateOrderResponse addAssortedVariety(
			@CookieValue(value = "lbbagnumber", defaultValue = "0") String orderIdS,
			HttpSession sess){

		CreateOrderResponse r = new CreateOrderResponse();
		r.setSuccess(false);

		
		try {
						
			
			long orderId = Utility.getLong(orderIdS);
			CartOrder order = dao.get(orderId);
			if(order == null || !order.getStatus().equals("incart")){
				r.setMessage("No valid order found.");
			}		
			
			else {
				
				int totalItems = 0;

				List<OrderLineItemCart> removeItems = new ArrayList<>();
				
				List<OrderLineItemCart> items = order.getLineItems();
				if(items != null){
					for(OrderLineItemCart item : items){

						if( item.getPromo() != null 
								&& "fifthflower".equals(item.getPromo()) ){
							// If already present remove it. Any issue with item which is preventing it from 
							// appearing in the cart will be solved this away
							removeItems.add(item);
						}
						
						//If there is BriteBox, remove it.
						else if( item.getPromo() != null 
								&& "firsttimepatient".equals(item.getPromo()) ){
							removeItems.add(item);
						}
						
						//If there is doubledownoffer, remove the promo.
						if( item.getPromo() != null 
								&& "doubledownoffer".equals(item.getPromo()) ){							
							item.setPrice(item.getCost());
							item.setPromo("");
						}
						
						if(item.getType().equals("item")
								&& item.isInstock()){
							totalItems+= item.getQty();
						}
					}
				}
				
				
				if(removeItems.size() > 0){
					items.removeAll(removeItems);
					totalItems = totalItems - removeItems.size();
				}
				
				
				
				//Add fresh new item
				OrderLineItemCart newItem = new OrderLineItemCart();
				newItem.setTaxable(false);
				newItem.setInstock(true);
				newItem.setType("item");
				newItem.setName("Holiday Promo 1/8th");
				newItem.setPromo("fifthflower");
				newItem.setProductId(11871);
				newItem.setVariationId(0);
				newItem.setQty(1);
				newItem.setCost(45d);
				newItem.setPrice(0d);
				newItem.setImg("/products/TahoeOG.jpg");

				items.add(newItem);
				
				
				/*Update order with lineItems*/
				order.setLineItems(items);
				
				
				//Run through deal logic
				cartLogics.applyDeals(order, ccs.getcOps(), false, sess);
				
				
				/*Update orderTotals*/
				cartLogics.calculateSummary(order);
				
				dao.save(order);
				
				r.setSuccess(true);
				r.setCartCount(totalItems);				
				r.setOrder(order);
			}
			
		}catch(Exception e){
			
			r.setMessage("There was some error creating the order, please try later.");
			logger.error(Exceptions.giveStackTrace(e));
		}
		
		return r;	
	}
	
	
	@RequestMapping(value = "/add-promo-420", method = RequestMethod.POST)
	public @ResponseBody CreateOrderResponse addPromo420(
			@CookieValue(value = "lbbagnumber", defaultValue = "0") String orderIdS,
			HttpSession sess){

		CreateOrderResponse r = new CreateOrderResponse();
		r.setSuccess(false);
		r.setMessage("This promotion has expired.");
		
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
	
	
	@RequestMapping(value = "/validatezip", method = RequestMethod.GET)
	public @ResponseBody GenericResponse validateZipcode(
			@RequestParam(value="zip", required=true) String zipcode) {

		GenericResponse gr = new GenericResponse();
		gr.setSuccess(true);
		
		//validate zipcode
		String zipCode = zipcode.replaceAll("\\D+", "");
		if(zipCode.length() > 5){
			zipCode = zipCode.substring(0, 5);
		}
		
		gr.setMessage(zipValidation(Utility.getInteger(zipCode)));
		
		return gr;
	}
	
	
	private String zipValidation(int zip){
		
		String result = "";
		
		boolean local = false;
		boolean shipping = false;
		
		List<Integer> localZipcodes = ccs.getcOps().getLocalZipcodes();
		if(localZipcodes!=null){
			for(Integer lz : localZipcodes){
		
				if((int)lz == zip){
					local = true;
					break;
				}
			}
		}

		List<Integer> shippingZipcodes = ccs.getcOps().getShippingZipcodes();
		if(shippingZipcodes!=null){
			for(Integer sz : shippingZipcodes){
		
				if((int)sz == zip){
					shipping = true;
					break;
				}
			}
		}
		
		if(local && shipping){
			result = "both";
		}
		else if(local){
			result = "local";
		}
		else if(shipping){
			result = "shipping";
		}
		else{
			result = "invalid";
		}
		
		return result;
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
			@CookieValue(value = "lbbagnumber", defaultValue = "0") String orderIdS,
			HttpSession sess){
		
		CartResponse cr = new CartResponse();
		CartOrder order = findOrder(Utility.getLong(orderIdS));

		
		/*If userinfo is available, update the order with it.*/
		if(order !=null && user != null){
			User userDb = userDao.get(user.getId());
			
			if(userDb != null && userDb.getStatus().equals("active")){
				
				cr.setUser(userDb);
				
				OrderCustomer cus = new OrderCustomer();
				cus.set_id(userDb.get_id());
				cus.setEmail(userDb.getEmail());
				cus.setName(userDb.getFname() + " " + userDb.getLname());
	
				order.setCustomer(cus);
				
				//Check for prev delivery address and populate it into the order shipping address
				Address a = cartLogics.getPrevShippingInfo(userDb.get_id());
				if(a != null){
					
					if(order.getShipping() == null || 
							order.getShipping().getAddress() == null ||
							order.getShipping().getAddress().getAddress1().equals("")){
						
						if(order.getShipping() == null){
							order.setShipping(new Shipping());
						}
						
						order.getShipping().setAddress(a);
					}
				}
				
				else {

					//Check for user billing address and populate it into the order shipping address
					if(userDb.getBilling() != null &&
							!userDb.getBilling().getAddress1().equals("")){
						
						if(order.getShipping() == null || 
								order.getShipping().getAddress() == null ||
								order.getShipping().getAddress().getAddress1().equals("")){
							
							if(order.getShipping() == null){
								order.setShipping(new Shipping());
							}
							
							order.getShipping().setAddress(userDb.getBilling());
						}
						
					}
					
				}
				
				dao.save(order);
			}
		}
		
		/**
		 * Some deals in the cart might expire and new deals needs to be applied
		 * In that cases, we need to adjust the coupons and order totals in the 
		 * cart. To keep the process simple, we run the order object through a 
		 * post processing system, which will check for these changes everytime
		 * the cart loads.
		 **/
		
		if(order !=null) 
			cartPostProcessing(order, true /*reapply coupon*/, sess, "getcart");
		
		
		cr.setOrder(order);
		cr.setConfig(ccs.getcOps());
		
		
		/**
		 * Get doubledown products
		 * */
		List<Integer> ddPrdIds = ccs.getcOps().getDoubleDownEligibleProducts();
		if(ddPrdIds != null && ddPrdIds.size()>0){			
			
			List<DoubleDownProducts> ddps = new ArrayList<>();
			
			for(Integer ddPid : ddPrdIds){		
			
				Product prd = prdDao.createQuery()
						.field("_id")
						.equal(ddPid)
						
						.filter("stockStat", "instock")

						.retrievedFields(true, "featuredImg", "_id", "name", "url", "variation")
						.get();
				
				if(prd != null){
					
					if(prd.isVariation()){
						List<Price> prices = priceDao.createQuery()
								.field("pid").equal(ddPid)
								.filter("stockStat", "instock")
								.filter("regPrice", 20)
								.retrievedFields(true, "_id", "variation")
								.asList();
						
						if(prices!=null){
							for(Price p : prices){
								
								DoubleDownProducts ddp = new DoubleDownProducts();
								ddp.set_id(prd.get_id());
								ddp.setFeaturedImg(prd.getFeaturedImg());
								ddp.setName(prd.getName());
								ddp.setUrl(prd.getUrl());
								
								ddp.setVid(p.get_id());
								
								try{									
									ddp.setVariationName(p.getVariation().get(0).getAttr());	
									ddp.setVariationValue(p.getVariation().get(0).getValue());									
								}catch(Exception e){}
								
								ddps.add(ddp);
							}
						}
					}
					else{
						
						DoubleDownProducts ddp = new DoubleDownProducts();
						ddp.set_id(prd.get_id());
						ddp.setFeaturedImg(prd.getFeaturedImg());
						ddp.setName(prd.getName());
						ddp.setUrl(prd.getUrl());
						
						ddp.setVid(0);
						ddps.add(ddp);
					}
				}
			}
/*			System.out.println(prdDao.createQuery()
						.field("_id")
						.in(ddPrdIdsL)

						//.filter("status", "publish")
						.filter("stockStat", "instock")
						
						.retrievedFields(true, "featuredImg", "_id", "name").getQueryObject().toString());*/
			
			cr.setDdPrds(ddps);
		}
		
		if(order !=null) {
			cr.setAvailableDeals(cartLogics.availableDeals(order));
			
			//Get productIds of all flowers
			List<Product> prds = prdDao.createQuery()
					.field("categories").equal("Flowers")
					.filter("status", "publish")
					.filter("stockStat", "instock")
					.order("-_id")
					.retrievedFields(true, "_id")
					.asList();
			
			List<Long> flowers = new ArrayList<Long>();
			if(prds != null){
				for(Product p : prds){
					flowers.add(p.get_id());
				}
				
				cr.setCommonList(flowers);
			}
		}
		
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
	public @ResponseBody CreateOrderResponse saveDeliveryAddr(
			@PathVariable long orderId, @RequestBody Shipping shipping){
		
		CreateOrderResponse or = new CreateOrderResponse();
		or.setSuccess(false);
		or.setMessage("");

		try {

			if(shipping == null 
					|| shipping.getAddress() == null 
					|| shipping.getDeliveryMethod() == null){

				or.setMessage("Please fill in all mandatory fields.");				
			}
			else{
				
				CartOrder order = dao.findCartOrder(orderId);
				if(order == null){
					or.setMessage("Invalid order id. Please refresh the page and try again.");
				}
				
				else {
					
					//validate zipcode
					String zipCode = shipping.getAddress().getZip().replaceAll("\\D+", "");
					if(zipCode.length() > 5){
						zipCode = zipCode.substring(0, 5);
					}
					String zipVal = zipValidation(Utility.getInteger(zipCode));
					if(zipVal.equals("invalid")){
						or.setMessage("Sorry, we currently don't service your area. " 
								+ "We are working very hard on expanding to your city. ");	
						
						return or;
					}
					
					/**
					 * Since we allow editing zipcode, we need to make
					 * that they didn't switch from a shipping zipcode
					 * to local zipcode. So set it again here!
					 ***/					
					if(zipVal.equals("local")){
						shipping.setDeliveryMethod("Local Delivery");
					}
					else if(zipVal.equals("shipping")){
						shipping.setDeliveryMethod("Overnight Shipping");					
					}
					
					                                       
					/* Validate City and State for Tax rate*/
					double taxRate = 9.5d;
					                                       
					//Zero sales for this year
					Calendar now = Calendar.getInstance();
					if(now.get(Calendar.YEAR) == 2018){
						taxRate = 0d;
					}
					else {
						List<Ziptax> taxes = taxDao.createQuery()
							.field("zipcode").equal(zipCode)
							.field("city").equal(shipping.getAddress().getCity().toLowerCase())
							.asList();
						if(taxes != null && !taxes.isEmpty()){
							//System.out.println("Tax found " + taxes.get(0).getTaxRate());
							taxRate = taxes.get(0).getTaxRate();
						}
					}
					
                                        
                                        OrderCustomer orderCustomer = order.getCustomer();
					User usr = null;                                       
                                 
					if (orderCustomer != null) {                                      
						usr = userDao.findOne("_id", (long) orderCustomer.get_id());                                               
						if (usr!=null&&usr.getApproveStatus() != null) {
                                                    if(usr.getApproveStatus().equals("1")){
                                                        taxRate = 0d;
                                                    }
						}
					}                                        
                                        
                                        
//					if(order.getCustomer() != null){
//						/** Check the user type to see if its medical or recreational. Medical users pay 0 sales tax **/
//						User userDb = userDao.createQuery()
//								.field("_id").equal(order.getCustomer().get_id())
//								.field("memberType").equal("medical")
//								.retrievedFields(true, "_id")
//								.get();
//						if(userDb != null){
//							taxRate = 0d;
//						}
//					}
					
					TaxComponent taxComponent = new TaxComponent();
					taxComponent.setDescription("Excise Tax");
					taxComponent.setRate(ccs.getcOps().getCaCannabisExciseTax());
					
					TaxComponent salesTaxComponent = new TaxComponent();
					salesTaxComponent.setDescription("Sales Tax");
					salesTaxComponent.setRate(taxRate);
					
					List<TaxComponent> taxComponents = new ArrayList<>();
					taxComponents.add(taxComponent);
					taxComponents.add(salesTaxComponent);

					OrderTax orderTax = new OrderTax();
					orderTax.setTaxComponents(taxComponents);
					
					order.setOrderTax(orderTax);
					
					
					/** 
					 * Rush fee
					 */
					if(shipping.isRushDelivery()){
						shipping.setRushFee(ccs.getcOps().getRushDeliveryCharge());
					}
					else{
						shipping.setRushFee(0d);
					}
					
					order.setShipping(shipping);
					
					//Update orderTotals
					cartLogics.calculateSummary(order);
					dao.save(order);
					
					//Send deliveryMethod back to client.
					or.setMessage(shipping.getDeliveryMethod());
					or.setOrder(order);
					or.setSuccess(true);
				}				
				
			}
			
		}catch(Exception e){

			or.setMessage("There was some error updating the address, please try later.");
			logger.error(Exceptions.giveStackTrace(e));
		}
		
		return or;
	}
	
	
	
	@RequestMapping(value = "/{orderId}/placeorder", method = RequestMethod.POST)
	public @ResponseBody OrderPlaceResponse placeOrder(
			@PathVariable long orderId, @RequestBody CartOrder order,
			HttpServletRequest request){
		
		OrderPlaceResponse gr = new OrderPlaceResponse();
		gr.setSuccess(false);
		gr.setMessage("");
		gr.setPaymentProcessed(false);
		gr.setOrderFinalizationError(false);

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
					
					
					/* Save Remote IP into the orderData */
					try{
						order.getBilling().getPmtMethod().setIp(request.getRemoteAddr());
					}catch(Exception e){}
					
					

					boolean cardDataPresent = false;
					try{
						if(order.getBilling().getPmtMethod().getCardData() != null){
							cardDataPresent = true;
						}
					}catch(Exception e){}
					
					/* If there is payment data, process payment */		
					if(cardDataPresent){
						
						long money = 0l;
						String cardNonce = "";
						
						try {							
							/* Multiply by 100 to make it in cents */
							money = (long) (order.getTotal() * 100);
							cardNonce = order.getBilling().getPmtMethod().getCardData().getNonce();
							
						}catch(Exception e){
							gr.setMessage("Invalid payment details");
							return gr;
						}
						
						/* Create a unique number */
						SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
						String idemPotencyKey = sd.format(Calendar.getInstance().getTime()) 
											+ "_" 
											+ (int) (Math.random()*1000);
						//System.out.println("idemPotencyKey - " + idemPotencyKey);
						
						Charge body = new Charge();
						body.setAmount_money(new AmountMoney(money));
						body.setCard_nonce(cardNonce);
						//body.setDelay_capture(true); //For doing Auth only (no capture)
						body.setIdempotency_key(idemPotencyKey);
						body.setReference_id("CartOrderID:" + order.get_id());
						
						GenericResponse paymentResponse = paymentService.processPayment(body);
						if(paymentResponse.isSuccess()){
							gr.setPaymentProcessed(true);
							
							/* Save transactionId */
							order.getBilling().getPmtMethod().setTransactionId(paymentResponse.getMessage());
						}
						else{
							gr.setPaymentError(paymentResponse.getMessage());
							return gr;
						}		
					}
					/** Payment process ends **/
						
					String resp = orderFinalization.finalizeOrder(order, cartLogics);
					if("".equals(resp)){
						gr.setSuccess(true);
						gr.setMessage(orderFinalization.getOrderNumber()+"");
						
						
						Order newOrder = orderFinalization.getOrder();
						//Sent Confirmation Email
						try {
							
							Email email = new Email();
							email.setEmailTemplate("order-confirmation");
							email.setFromName("Luvbrite Orders");
							email.setFromEmail("no-reply@luvbrite.com");
							email.setRecipientName(newOrder.getCustomer().getName());
							
							if(ccs.getcOps().isDev()){
								email.setRecipientEmail("admin@day2dayprinting.com");								
							}
							else{
								email.setRecipientEmail(newOrder.getCustomer().getEmail());								
								email.setBccs(Arrays.asList(new String[]{"orders-notify@luvbrite.com"}));
							}
							
							email.setEmailTitle("Order Confirmation Email");
							email.setSubject("Luvbrite Order#" + newOrder.getOrderNumber() + " placed successfully");
							email.setEmailInfo("Your order with Luvbrite.");
							
							email.setEmail(newOrder);
							
							emailService.sendEmail(email);
							
						}catch(Exception e){
							logger.error(Exceptions.giveStackTrace(e));
						}
						
						
						//Sent Order Meta to Inventory
						try {							
							// uncomment this for prod enviroment
							if(!ccs.getcOps().isDev()) postOrderMeta.postOrder(newOrder);
                                                        
                                                        // this for dev enviroment
							//if(ccs.getcOps().isDev()) postOrderMeta.postOrder(newOrder);
						
                                                }catch(Exception e){
							logger.error(Exceptions.giveStackTrace(e));
						}
						
					}
					else{
						gr.setOrderFinalizationError(true);
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
			@RequestParam(value="qty", required=false) Integer qty,
			HttpSession sess){
		
		CreateOrderResponse cr = new CreateOrderResponse();
		cr.setSuccess(false);
		cr.setMessage("");
		
		try {
			//Validations
			if(orderId == null || orderId == 0){
				cr.setMessage(" - Invalid order ID.");
			}
			
			if((variationId == null || variationId == 0) 
					&& (productId == null || productId == 0 || productId == 11839) ){ //11839 - britebox
				cr.setMessage(cr.getMessage() + " - Invalid item/product ID.");
			}
			
			if(qty == null || qty == 0){
				cr.setMessage(cr.getMessage() + " - Invalid quantity.");
			}
			
			if(cr.getMessage().length()>1){
				return cr;
			}
			
			
			CartOrder order = dao.get(orderId);
			if(order == null){
				cr.setMessage("There was some error processing your request. Please refresh the page and try again");
				logger.error("updateCart. No order found for orderId - " + orderId);
				return cr;
			}
			
			int totalItems = 0;
			boolean itemFound = false;
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
					}
				}
			}
			
			if(itemFound){
				
				/*Run through the post processing logic*/
				cartPostProcessing(order, true /*Check and re-apply coupons*/, sess, "updateqty");		

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
	public @ResponseBody CreateOrderResponse removeCoupon(
			@PathVariable String couponCode,
			@RequestParam(value="oid", required=false) Long orderId,
			HttpSession sess){
		
		CreateOrderResponse cr = new CreateOrderResponse();
		cr.setSuccess(false);
		cr.setMessage("");
		
		GenericResponse gr = couponManager.removeCoupon(couponCode, orderId);
		if(gr.isSuccess()){
			cr.setSuccess(true);
			
			CartOrder order = dao.get(orderId);
			if(order != null){
				cartPostProcessing(order, false /*No need to re-apply coupon, hence false*/, sess, "removecoupon");
				
				cr.setOrder(order);
			}
		}
		
		return cr;
	}
	
	
	
	@RequestMapping(value = "/applycoupon/{couponCode}", method = RequestMethod.GET)
	public @ResponseBody CreateOrderResponse applyCoupon(
			@PathVariable String couponCode,
			@RequestParam(value="oid", required=false) Long orderId,
			HttpSession sess){
		
		CreateOrderResponse cr = new CreateOrderResponse();
		cr.setSuccess(false);
		cr.setMessage("");
		
		GenericResponse gr = couponManager.applyCoupon(couponCode, orderId);
		if(gr.isSuccess()){
			cr.setSuccess(true);
			
			CartOrder order = dao.get(orderId);
			if(order != null){
				cartPostProcessing(order, false /*No need to re-apply coupon, hence false*/, sess, "applycoupon");
				
				cr.setOrder(order);
			}
		}
		
		else{
			cr.setMessage(gr.getMessage());
		}
		
		return cr;
	}
	
	
	
	@RequestMapping(value = "/removeitem", method = RequestMethod.DELETE)
	public @ResponseBody CreateOrderResponse removeItem(
			@RequestParam(value="oid", required=false) Long orderId,
			@RequestParam(value="vid", required=false) Integer variationId,
			@RequestParam(value="pid", required=false) Integer productId,
			HttpSession sess){
		
		CreateOrderResponse cr = new CreateOrderResponse();
		cr.setSuccess(false);
		cr.setMessage("");
		
		try {
			//Validations
			if(orderId == null || orderId == 0){
				cr.setMessage(" - Invalid order ID.");
			}
			
			if((variationId == null || variationId == 0) && (productId == null || productId == 0)){
				cr.setMessage(cr.getMessage() + " - Invalid item/product ID.");
			}
			
			if(cr.getMessage().length()>1){
				return cr;
			}
			
			
			CartOrder order = dao.get(orderId);
			
			int totalItems = 0;
			boolean itemFound = false;

			
			List<OrderLineItemCart> items = order.getLineItems();
			if(items != null){
				Iterator<OrderLineItemCart> iter = items.iterator();
				
				while(iter.hasNext()){
					OrderLineItemCart item = iter.next();
					if((variationId==0 && productId == item.getProductId()) 
							|| (item.getVariationId() == variationId)){
						
						iter.remove();
							
						/*Update Log*/
						try {
							
							Log log = new Log();
							log.setCollection("cartorders");
							log.setDetails("Item removed - " + item.getName() 
									+ ". Var Id - " + variationId
									+ ". Product Id - " + productId);
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
					}
				}
				
				/*Run through the post processing logic*/
				cartPostProcessing(order, true /*Check and re-apply coupons*/, sess, "removeitem");

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

	
	@RequestMapping(value = "/getcpromos")
	public @ResponseBody GenericResponse getCustomerPromos(@AuthenticationPrincipal 
			UserDetailsExt user){
		
		GenericResponse gr = new GenericResponse();
		gr.setSuccess(false);
		gr.setMessage("");
		
		if(user!=null && user.isEnabled()){
			final User u = userDao.createQuery()
					.field("_id").equal(user.getId())
					.retrievedFields(true, "email")
					.get();
			
			List<Coupon> coupons = couponDao.createQuery()
					.field("emails").equalIgnoreCase(Pattern.quote(u.getEmail()))
					.field("active").equal(true)
					.field("expiry").greaterThan(Calendar.getInstance().getTime())
					.retrievedFields(true, "_id")
					.asList();
			
			List<String> promos = new ArrayList<String>();
			if(coupons != null && !coupons.isEmpty()){
				for(Coupon c : coupons){
					promos.add(c.get_id());
				}
				
				gr.setSuccess(true);
				gr.setResults(promos);
			}
		}
		
		return gr;		
	}	

	
	@RequestMapping(value = "/foc")
	public @ResponseBody GenericResponse firstOrderCheck(@AuthenticationPrincipal 
			UserDetailsExt user){
		
		GenericResponse gr = new GenericResponse();
		gr.setSuccess(false);
		gr.setMessage("");
		
		if(user!=null && user.getId() != 0){
			String response = cartLogics.firstOrderCheck(user.getId());
			if(response.equals("Y")){
				gr.setSuccess(true);
				gr.setMessage("check order total before showing the popup");
			}
		}
		
		return gr;		
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
	
	private void cartPostProcessing(CartOrder order, boolean couponReapply, HttpSession sess, String action){
		
		String couponCode = "";
		boolean itemsPresent = false;
		
		try {
			
			List<OrderLineItemCart> items = order.getLineItems();
			
			if(items != null){
				for(OrderLineItemCart item : items){
					
					if(item.getType().equals("item") && item.isInstock()){
						itemsPresent = true;
					}
					
					else if(item.getType().equals("coupon")){
						couponCode = item.getName();
					}
				}
			}
			
			
			/* Reapply coupons if any! */
			if(!couponCode.equals("") && couponReapply){
				couponManager.reapplyCoupon(couponCode, order, false, action);
			}				
			
			
			//Run through deal logic
			if(itemsPresent) cartLogics.applyDeals(order, ccs.getcOps(), false, sess, couponManager);
			
			
			//Update orderTotals
			cartLogics.calculateSummary(order);
			
			
			//Save order
			dao.save(order);	

			
		}catch(Exception e){			
			logger.error(Exceptions.giveStackTrace(e));			
		}
		
		
	}
}
