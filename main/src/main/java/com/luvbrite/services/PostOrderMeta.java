package com.luvbrite.services;

import java.net.URL;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luvbrite.dao.LogDAO;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.utils.GenericConnection;
import com.luvbrite.web.models.Address;
import com.luvbrite.web.models.AttrValue;
import com.luvbrite.web.models.Log;
import com.luvbrite.web.models.Order;
import com.luvbrite.web.models.OrderLineItem;
import com.luvbrite.web.models.PaymentMethod;
import com.luvbrite.web.models.ordermeta.BillingAddress;
import com.luvbrite.web.models.ordermeta.LineItem;
import com.luvbrite.web.models.ordermeta.OrderMain;
import com.luvbrite.web.models.ordermeta.itemmeta.Meta;

@Service
public class PostOrderMeta {
	
	private static Logger logger = Logger.getLogger(PostOrderMeta.class);
	
	@Autowired
	private LogDAO logDao;
	
	private final String newOrderURL = "https://www.luvbrite.com/inventory/apps/a-ordermeta?json";
	private final String updateOrderURL = "https://www.luvbrite.com/inventory/apps/a-c-ordermeta?json";
	private NumberFormat nf = NumberFormat.getCurrencyInstance();
	
	public String postOrder(Order order){
		
		String response = "";
		String postURL = newOrderURL;
		
		SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		dt.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		try {
			
			if(order == null) return "Order is NULL";

			
			com.luvbrite.web.models.ordermeta.Order o = new com.luvbrite.web.models.ordermeta.Order();
			
			o.setOrder_number(Long.valueOf(order.getOrderNumber()).intValue());
			
			if(order.getNotes()!=null) {
				o.setDelivery_notes(order.getNotes().getDeliveryNotes());
				o.setNote(order.getNotes().getAdditonalNotes());
			}
			
			if(order.getStatus().equals("cancelled")){
				o.setStatus("cancelled");
				postURL = updateOrderURL;
			}
			else if(order.getStatus().equals("new")) {
				o.setStatus("new");
			}
			
			
			Date completed = order.getDate();			
			o.setCompleted_at(dt.format(completed));
			
			
			o.setTotal(order.getTotal()+"");
			o.setTotal_discount((order.getSubTotal() - order.getTotal()) + "");			
			
			Address billing = order.getShipping().getAddress();
			if(billing != null){
				BillingAddress bi = new BillingAddress();
				
				bi.setAddress_1(billing.getAddress1());
				bi.setAddress_2(billing.getAddress2());
				bi.setCity(billing.getCity());
				bi.setFirst_name(billing.getFirstName());
				bi.setLast_name(billing.getLastName());
				bi.setPhone(billing.getPhone());
				bi.setPostcode(billing.getZip());
				
				o.setBilling_address(bi);
			}
			
			PaymentMethod pmtMthd = order.getBilling() != null ? order.getBilling().getPmtMethod() : null;
			if(pmtMthd != null){
				if(pmtMthd.getMethod().equals("cc")){
					o.setPaymentMethod("Credit Card - " 
							+ pmtMthd.getCardData().getCard_brand() 
							+ " ending in " + pmtMthd.getCardData().getLast_4());
				}
				else {
					o.setPaymentMethod(pmtMthd.getType());
				}
			}
			
			List<LineItem> line_items = new ArrayList<LineItem>();
			List<OrderLineItem> olis = order.getLineItems();
			if(olis != null){
				
				for(OrderLineItem oli : olis){
					if(oli.getType().equals("item")){
						LineItem li = new LineItem();
						
						String name = oli.getName();
						String promo = oli.getPromo();
						if(promo != null && !"".equals(promo)){
							name += (" (" + promo +")");
						}
						
						String unitPrice = " (Unit price: " + nf.format(oli.getCost()) + ")";
						
						li.setName(name + unitPrice);
						li.setQuantity(oli.getQty());
						
						List<Meta> meta = new ArrayList<Meta>();
						
						List<AttrValue> specs = oli.getSpecs();
						if(specs != null && specs.size() !=0){
							AttrValue spec = specs.get(0);
							
							meta.add(new Meta(spec.getAttr(), spec.getAttr(), spec.getValue()));
						}
						
						li.setMeta(meta);
						
						line_items.add(li);
					}
					
					else if(oli.getType().equals("coupon")){
						LineItem li = new LineItem();
						
						String name = " ** Coupon ** (" + oli.getName() + ") - " + nf.format(oli.getPrice());
						
						li.setName(name);
						li.setQuantity(oli.getQty());
						li.setMeta(new ArrayList<Meta>());
						
						line_items.add(li);
					}
				}
				
			}
			
			o.setLine_items(line_items);
			
			OrderMain om = new OrderMain();
			om.setOrder(o);
			
			
			/* Convert OrderMain to JSON */
			ObjectMapper mapper = new ObjectMapper();
			String orderMainString = mapper.writeValueAsString(om); 
			
			
			/* POST TO INVENTORY SERVER */
			GenericConnection conn = new GenericConnection();
			String resp = conn.contactService(orderMainString, new URL(postURL), false);
			
			
			/* Update Log */
			try {
				
				Log log = new Log();
				log.setCollection("orders");
				log.setDetails("Meta send. " + resp);
				log.setDate(Calendar.getInstance().getTime());
				log.setKey(order.get_id());
				log.setUser("System");
				
				logDao.save(log);					
			}
			catch(Exception e){
				logger.error(Exceptions.giveStackTrace(e));
			}
			
			
			response = "success";
			
		}catch(Exception e){
			
			response = "There was some error creating the order, please try later.";
			logger.error(Exceptions.giveStackTrace(e));
		}

		return response;
	}
	
}
