package com.luvbrite.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.luvbrite.dao.OrderDAO;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.utils.Utility;
import com.luvbrite.web.models.Address;
import com.luvbrite.web.models.Billing;
import com.luvbrite.web.models.Email;
import com.luvbrite.web.models.Order;
import com.luvbrite.web.models.OrderCustomer;
import com.luvbrite.web.models.OrderLineItem;
import com.luvbrite.web.models.OrderNotes;
import com.luvbrite.web.models.PaymentMethod;
import com.luvbrite.web.models.Shipping;
import com.luvbrite.web.models.ordermeta.BillingAddress;
import com.luvbrite.web.models.ordermeta.LineItem;
import com.luvbrite.web.models.ordermeta.OrderMain;

@Service
public class InOfficeOrderLogic {

	private static Logger logger = Logger.getLogger(InOfficeOrderLogic.class);
	private String message = "";
	private long orderNumber;

	@Autowired
	private OrderDAO dao;
	
	@Autowired
	private ControlConfigService ccs;
	
	@Autowired
	private EmailService emailService;

	public String create(OrderMain orderMain){	

		Connection tcon = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		Order order;
		message = "";

		try {

			if(orderMain==null){
				return "Error extracting the order main data.";
			}


			/***
			 * INSERT THE PAYLOAD INTO DB
			 ***/



			com.luvbrite.web.models.ordermeta.Order orderm = orderMain.getOrder();

			
			//Create new Order, and its components
			order = new Order();	
			Billing billing = new Billing();
			PaymentMethod pmtMethod = new PaymentMethod();
			Shipping shipping = new Shipping();
			OrderCustomer customer = new OrderCustomer();
			OrderNotes notes = new OrderNotes();
			Address address = new Address();
			List<OrderLineItem> olis = new ArrayList<>();
			
			
			BillingAddress billingM = orderm.getBilling_address();

			//Prep billing info
			String customerName = billingM.getFirst_name();

			pmtMethod.setMethod("cod");
			pmtMethod.setType("Donation on Delivery");
			billing.setPmtMethod(pmtMethod);

			//Prep shipping info
			address.setAddress1("2030 S Westgate Ave");
			address.setCity("Los Angeles");
			address.setFirstName(customerName);
			address.setState("CA");
			address.setZip("90025");
			shipping.setAddress(address);
			
			shipping.setDeliveryMethod("In Office");
			shipping.setShippingCharge(0d);
			
			//Notes
			notes.setAdditonalNotes("Sales ID: " + orderm.getOrder_number());
			notes.setDeliveryNotes("In office order placed in LB Inv");

			//Customer Info
			customer.setName(customerName);
			customer.setEmail("info@luvbrite.com");


			//Build OrderLineItem from product
			
			List<LineItem> line_items = orderm.getLine_items();
			for(LineItem li: line_items){
				if(li != null){
					
					double salesPrice = Utility.getDouble(li.getTotal());
					
					OrderLineItem oli = new OrderLineItem();
					oli.setItemId(0);
					oli.setName(li.getName());
					oli.setQty(li.getQuantity());
					oli.setTaxable(false);
					oli.setVariationId(0);
					
					if(salesPrice > 0){
						oli.setType("item");
						oli.setCost(salesPrice);
						oli.setPrice(salesPrice);
					}
					else {
						oli.setType("coupon");
						oli.setCost(salesPrice * -1);
						oli.setPrice(salesPrice * -1);
					}
						

					olis.add(oli);
				}
			}



			//Fill in the order object

			//Get new orderId, orderNumber
			orderNumber = dao.getNextOrderNumber();
			order.set_id(dao.getNextSeq());
			order.setOrderNumber(orderNumber);

			order.setStatus("new");
			order.setLineItems(olis);
			order.setBilling(billing);
			order.setShipping(shipping);
			order.setCustomer(customer);

			order.setDate(Calendar.getInstance().getTime());
			order.setSource("System LB INV (In Office)");
			order.setNotes(notes);
			order.setStatus("delivered");
			
			double total = Utility.getDouble(orderm.getTotal());
			double discount = Utility.getDouble(orderm.getTotal_discount());
			
			order.setTotal(total);
			order.setSubTotal(total + discount);
			
			dao.save(order);
			
			

			//Sent Confirmation Email
			try {
				
				Email email = new Email();
				email.setEmailTemplate("order-confirmation");
				email.setFromName("Luvbrite Orders");
				email.setFromEmail("no-reply@luvbrite.com");
				email.setRecipientName(order.getCustomer().getName());
				
				if(ccs.getcOps().isDev()){
					email.setRecipientEmail("admin@day2dayprinting.com");
				}
				else{
					email.setRecipientEmail(order.getCustomer().getEmail());								
					email.setBccs(Arrays.asList(new String[]{"orders-notify@luvbrite.com"}));
				}
				
				email.setEmailTitle("Order Confirmation Email");
				email.setSubject("Luvbrite Order#" + order.getOrderNumber() + " placed successfully");
				email.setEmailInfo("In office order with Luvbrite.");
				
				email.setEmail(order);
				
				emailService.sendEmail(email);
				
			}catch(Exception e){
				logger.error(Exceptions.giveStackTrace(e));
			}
			
			

		}catch(Exception e){
			logger.error(Exceptions.giveStackTrace(e));
			message = e.getMessage();
		}
		finally{	
			try{if(pst!=null)pst.close();}catch(Exception e){}
			try{if(rs!=null)rs.close();}catch(Exception e){}
			try{if(tcon!=null)tcon.close();}catch(Exception e){}		
		}	

		return message;
	}
	

	public long getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(long orderNumber) {
		this.orderNumber = orderNumber;
	}
}
