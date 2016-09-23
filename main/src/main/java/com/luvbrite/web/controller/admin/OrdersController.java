package com.luvbrite.web.controller.admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.luvbrite.dao.LogDAO;
import com.luvbrite.dao.OrderDAO;
import com.luvbrite.services.EmailService;
import com.luvbrite.services.PostOrderMeta;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.utils.PaginationLogic;
import com.luvbrite.web.models.Email;
import com.luvbrite.web.models.GenericResponse;
import com.luvbrite.web.models.Log;
import com.luvbrite.web.models.Order;
import com.luvbrite.web.models.OrderDispatchInfo;
import com.luvbrite.web.models.ResponseWithPg;
import com.luvbrite.web.models.UserDetailsExt;
import com.zaxxer.hikari.HikariDataSource;


@Controller
@RequestMapping(value = {"/admin/orders","/admin/order"})
public class OrdersController {
	
	private static Logger logger = Logger.getLogger(OrdersController.class);

	@Autowired
	private OrderDAO dao;
	
	@Autowired
	private LogDAO logDao;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private PostOrderMeta postOrderMeta;
	
	@Autowired
	private HikariDataSource ds;
	
	@RequestMapping(method = RequestMethod.GET)
	public String mainPage(ModelMap model){		
		return "admin/orders";		
	}	
	
	
	@RequestMapping(value = "/json/", method = RequestMethod.GET)
	public @ResponseBody ResponseWithPg orders(
			@RequestParam(value="p", required=false) Integer page,
			@RequestParam(value="q", required=false) String query,
			@RequestParam(value="o", required=false) String order){

		ResponseWithPg rpg = new ResponseWithPg();		

		if(query==null) query = "";
		if(order==null) order = "-orderNumber";
		
		int offset = 0,
			limit = 15; //itemsPerPage
		
		if(page==null) page = 1;
		if(page >1) offset = (page-1)*limit;
		
		PaginationLogic pgl = new PaginationLogic((int)dao.count(query), limit, page);
		List<Order> orders = dao.find(order, limit, offset, query);

		rpg.setSuccess(true);
		rpg.setPg(pgl.getPg());
		rpg.setRespData(orders);
		
		return rpg;	
	}
	

	@RequestMapping(value = "/json/{orderNumber}")
	public @ResponseBody Order orderDetails(@PathVariable long orderNumber, ModelMap model){	
		
		return dao.findOne("orderNumber", orderNumber);				
	}
	

	@RequestMapping(value = "/json/savestatus")
	public @ResponseBody GenericResponse saveStatus(@RequestBody Order order, @AuthenticationPrincipal 
			UserDetailsExt user){	

		GenericResponse r = new GenericResponse();
		if(order.getStatus().trim().equals("")){
			r.setMessage("Invalid order status.");			
		}
		else{
			
			Order orderDb = dao.get(order.get_id());			
			if(orderDb.get_id()==order.get_id()){		
				
				String prevStatus = orderDb.getStatus();
				
				orderDb.setStatus(order.getStatus());
				
				/**
				 * Here we are saving only the status.
				 * Remaining information is same as that 
				 * pulled from DB
				 **/			
				dao.save(orderDb);
				
				
				/**
				 * Update Log
				 * */
				try {
					
					Log log = new Log();
					log.setCollection("orders");
					log.setDetails("order.status changed. previous value: " + prevStatus);
					log.setDate(Calendar.getInstance().getTime());
					log.setKey(order.get_id());
					log.setUser(user.getUsername());
					
					logDao.save(log);					
				}
				catch(Exception e){
					logger.error(Exceptions.giveStackTrace(e));
				}
				
				if(order.getStatus().equals("cancelled")){

					/* Post Meta if required */
					try {							
					
						postOrderMeta.postOrder(orderDb);							
					
					}catch(Exception e){
						logger.error(Exceptions.giveStackTrace(e));
					}
					

					
					/* Send email if needed */
					try {
						
						Email email = new Email();
						email.setEmailTemplate("order-cancelled");
						email.setFromName("Luvbrite Orders");
						email.setFromEmail("no-reply@luvbrite.com");
						email.setRecipientEmail(orderDb.getCustomer().getEmail());
						email.setRecipientName(orderDb.getCustomer().getName());
						
						email.setBccs(Arrays.asList(new String[]{"orders-notify@luvbrite.com"}));
						
						email.setEmailTitle("Order Cancellation Email");
						email.setSubject("Luvbrite Order#" + orderDb.getOrderNumber() + " has been cancelled");
						email.setEmailInfo("cancellation confirmation.");						
						
						emailService.sendEmail(email);
						
					}catch(Exception e){
						logger.error(Exceptions.giveStackTrace(e));
					}
				}
				
				r.setSuccess(true);
			}
			
			else {
				r.setMessage("Invalid order Id");
			}
		}
		return r;		
	}
	

	@RequestMapping(value = "/{orderNumber}")
	public String admin(@PathVariable long orderNumber, ModelMap model){	
		model.addAttribute("orderNumber", orderNumber);
		
		return "admin/order/details";		
	}
	

	@RequestMapping(value = "/json/getsalesid/{orderNumber}")
	public @ResponseBody GenericResponse getSalesId(@PathVariable long orderNumber){
		
		GenericResponse gr = new GenericResponse();
		gr.setSuccess(false);
		gr.setMessage("");
		
		Connection tcon = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		
		try {
			
			tcon = ds.getConnection();			
			pst = tcon.prepareStatement("SELECT ds.id, ds.date_finished, d.driver_name "
				+ "FROM dispatch_sales_info ds "
					+ "JOIN online_order_info ooi ON ooi.dispatch_sales_id = ds.id "
					+ "LEFT JOIN drivers d ON d.id = ds.driver_id "
				
				+ "WHERE ooi.order_number = ? "
					+ "ORDER BY ds.id DESC LIMIT 1");
			
			pst.setString(1, orderNumber+"");
			rs = pst.executeQuery();
			if(rs.next()){
				
				String driverName = rs.getString("driver_name")==null ? "No Driver" : rs.getString("driver_name");
				Date dateFinished = rs.getTimestamp("date_finished")==null ? null : rs.getTimestamp("date_finished");
				long salesId = rs.getLong("id");
				
				Order o = dao.findOne("orderNumber", orderNumber);				
				if(o!=null) {
					
					OrderDispatchInfo d = new OrderDispatchInfo();
					if(o.getDispatch() != null){
						d = o.getDispatch();
					}
					
					// If there is no dateFinished, there is a chance that order was 
					// not processed and hence may not have a driver assigned!
					if(dateFinished!=null) {
						d.setDriver(driverName);
						d.setDateFinished(dateFinished);
						
						o.setStatus("delivered");
					}
										
					d.setSalesId(salesId);
					
					o.setDispatch(d);
					
					dao.save(o);
					
					List<Order> os = new ArrayList<Order>();
					os.add(o);					
					gr.setResults(os);
					
					gr.setSuccess(true);
				}	
			}
			
			else{
				
				gr.setMessage("No order found on the inventory end!");
			}
			
			rs.close();rs=null;		
			pst.close();pst=null;
			tcon.close();tcon=null;
			
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
			try{if(rs!=null)rs.close();}catch(Exception e){}
			try{if(pst!=null)pst.close();}catch(Exception e){}
			try{if(tcon!=null)tcon.close();}catch(Exception e){}
		}
		
		return gr;		
	}
	

	@RequestMapping(value = "/json/firstordercheck/{orderNumber}/{customerId}")
	public @ResponseBody GenericResponse firstorder(
			@PathVariable long orderNumber,
			@PathVariable long customerId){	
		
		GenericResponse gr = new GenericResponse();
		gr.setSuccess(true);
		gr.setMessage("N");
		
		Order order = dao.createQuery()
				.field("status").notEqual("cancelled")
				.field("customer._id").equal(customerId)
				.order("_id")
				.limit(1)
				.retrievedFields(true, "orderNumber")
				.get();
		
		if(order != null && order.getOrderNumber()==orderNumber){
			gr.setMessage("Y");
		}
		
		return gr;		
	}
	

	@RequestMapping(value = "/email-confirmation/{orderNumber}")
	public @ResponseBody GenericResponse emailConfirmation(@PathVariable Long orderNumber){	
		
		GenericResponse gr = new GenericResponse();
		gr.setSuccess(false);
		gr.setMessage("");
		
		if(orderNumber != null){
			
			Order order = dao.findOne("orderNumber", orderNumber);
			if(order !=null){
				
				//Sent Confirmation Email
				try {
					
					Email email = new Email();
					email.setEmailTemplate("order-confirmation");
					email.setFromName("Luvbrite Orders");
					email.setFromEmail("no-reply@luvbrite.com");
					email.setRecipientEmail(order.getCustomer().getEmail());
					email.setRecipientName(order.getCustomer().getName());
					email.setEmailTitle("Order Confirmation Email");
					email.setSubject("Luvbrite Order#" + order.getOrderNumber() + " placed successfully");
					email.setEmailInfo("Your order with Luvbrite.");
					
					email.setEmail(order);
					
					emailService.sendEmail(email);
					
					gr.setSuccess(true);
					
				}catch(Exception e){
					logger.error(Exceptions.giveStackTrace(e));
					gr.setMessage(e.getMessage());
				}
			}
			else{

				gr.setMessage("No order found.");
			}
		}
		
		else{

			gr.setMessage("Invalid Order Number.");
		}
		
		
		return gr;		
	}
}
