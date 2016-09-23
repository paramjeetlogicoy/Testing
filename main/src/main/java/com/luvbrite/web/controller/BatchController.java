package com.luvbrite.web.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.luvbrite.dao.OrderDAO;
import com.luvbrite.dao.UserDAO;
import com.luvbrite.services.EmailService;
import com.luvbrite.web.models.Email;
import com.luvbrite.web.models.Order;
import com.luvbrite.web.models.OrderDispatchInfo;
import com.luvbrite.web.models.RecoExpiry;
import com.luvbrite.web.models.User;
import com.zaxxer.hikari.HikariDataSource;


@Controller
@RequestMapping(value = "/batch")
public class BatchController {
	
	@Autowired
	private EmailService emailService;

	@Autowired
	private UserDAO userDao;
	
	@Autowired
	private HikariDataSource ds;
	
	@Autowired
	private OrderDAO orderDao;
	
	@RequestMapping(value = "/")
	public String homePage() {
		return "404";		
	}	

	
	@RequestMapping(value = "/reco-expiry")
	public @ResponseBody String recommendationExpiryCheck(){
		
		RecoExpiry rc = new RecoExpiry();
		
		Calendar now = Calendar.getInstance();
		Calendar day4 = Calendar.getInstance();
		day4.add(Calendar.DAY_OF_MONTH, 4);
		
		//Expiring in next 3 days
		List<User> users = userDao.createQuery()
				.field("active").equal(true)
				.field("identifications.recoExpiry").greaterThan(now.getTime())
				.field("identifications.recoExpiry").lessThan(day4.getTime())
				.order("identifications.recoExpiry")
				.asList();		
		
		//Expired
		List<User> usersE = userDao.createQuery()
				.field("active").equal(true)
				.field("identifications.recoExpiry").lessThan(now.getTime())
				.order("identifications.recoExpiry")
				.asList();
		
		//No expiry Date
		List<User> usersN = userDao.createQuery()
				.field("active").equal(true)
				.field("identifications.recoExpiry").doesNotExist()
				.asList();
		

		rc.setInvalid(usersN);
		rc.setExpired(usersE);
		rc.setExpiring(users);
		
		Email email = new Email();
		email.setEmailTemplate("reco-expiry");
		email.setFromEmail("no-reply@luvbrite.com");
		email.setRecipientEmail("payam@luvbrite.com");
		email.setRecipientName("Payam");
		email.setSubject("Recommendation Expiry");
		email.setEmailInfo("recommendation expiry info");
		email.setEmail(rc);
		
		try {
			
			emailService.sendEmail(email);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return "layout";
	}

	
	@RequestMapping(value = "/onetime/update-dispatch-cancelled")
	public @ResponseBody String updateDispatchCancelled() throws Exception{
		
		String query = "SELECT ds.id, ds.cancellation_reason, ds.status, "
				+ "ooi.order_number::numeric AS order_number "
				
				+ "FROM dispatch_sales_info ds "
					+ " JOIN online_order_info ooi ON ooi.dispatch_sales_id = ds.id "
				
				+ "WHERE ds.cancellation_reason "
					+ "NOT IN ('', 'Order updated. New dispatch created and this one is cancelled', "
					+ "'Duplicate', 'DUPLICATE', 'copy', 'duplicate', 'c', 'C')";
		
		Connection tcon = null;
		PreparedStatement pst = null;
		ResultSet rs = null;	
		
		String comments = "";
		long orderNumber = 0;
		
		int counter = 0;
		
		try {
			
			tcon = ds.getConnection();			
			pst = tcon.prepareStatement(query);
			rs = pst.executeQuery();
			while(rs.next()){
				comments = rs.getString("cancellation_reason");
				orderNumber = rs.getLong("order_number");
				
				Order o = orderDao.findOne("orderNumber", orderNumber);
				if(o != null &&
						(o.getDispatch() == null || o.getDispatch().getDateFinished() == null)){
					
					if(o.getStatus().equals("delivered")){
						System.out.println("Order already marked delivered. ordernumber " + orderNumber);
					}
					else {
						
						OrderDispatchInfo d = new OrderDispatchInfo();
						if(o.getDispatch() != null){
							d = o.getDispatch();
						}
						
						d.setComments(comments);
						d.setLockStatus(rs.getString("status"));
						o.setDispatch(d);
						
						//Set order status to Cancelled
						o.setStatus("cancelled");
						
						orderDao.save(o);
						
						counter++;
					}
				}
				
			}
			
			rs.close();rs=null;
			pst.close();pst=null;			
			tcon.close();tcon=null;

			System.out.println("************************");
			System.out.println(counter + " orders updated.");
			System.out.println("************************");
			
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
			try{if(rs!=null)rs.close();}catch(Exception e){}
			try{if(pst!=null)pst.close();}catch(Exception e){}
			try{if(tcon!=null)tcon.close();}catch(Exception e){}
		}
		
		return "layout";
	}

	
	@RequestMapping(value = "/onetime/update-dispatch-info")
	public @ResponseBody String updateDispatchNew() throws Exception{
		
		String query = "SELECT ds.id, ds.date_finished, ds.status, "
				+ "d.driver_name, ooi.order_number::numeric AS order_number "
				
				+ "FROM dispatch_sales_info ds "
					+ "JOIN online_order_info ooi ON ooi.dispatch_sales_id = ds.id "
					+ "LEFT JOIN drivers d ON d.id = ds.driver_id "
				
				+ "WHERE ds.date_finished IS NOT NULL "
					+ "AND ds.cancellation_reason = ''";
		
		Connection tcon = null;
		PreparedStatement pst = null;
		ResultSet rs = null;	
		
		String driverName = "";
		Date dateFinished = null;
		
		long salesId = 0, 
				orderNumber = 0;
		
		int counter = 0;
		
		try {
			
			tcon = ds.getConnection();	
			pst = tcon.prepareStatement(query);	
			rs = pst.executeQuery();
			while(rs.next()){
				
				driverName = rs.getString("driver_name")==null ? "No Driver" : rs.getString("driver_name");
				dateFinished = rs.getTimestamp("date_finished")==null ? null : rs.getTimestamp("date_finished");
				salesId = rs.getLong("id");
				orderNumber = rs.getLong("order_number");
				
				if(dateFinished!=null){
					
					Order o = orderDao.findOne("orderNumber", orderNumber);					
					if(o!=null) {
						
						OrderDispatchInfo d = new OrderDispatchInfo();
						if(o.getDispatch() != null){
							d = o.getDispatch();
						}
						
						d.setDateFinished(dateFinished);
						d.setDriver(driverName);
						d.setSalesId(salesId);
						d.setLockStatus(rs.getString("status"));	
						
						o.setDispatch(d);
						
						//Set order status to Delivered
						o.setStatus("delivered");
						
						orderDao.save(o);
						
						counter++;				
					}
					
					else {
						System.out.println("No mongo order found for ordernumber " + orderNumber);
					}
				}
			}
			
			rs.close();rs=null;
			pst.close();pst=null;			
			tcon.close();tcon=null;

			System.out.println("************************");
			System.out.println(counter + " orders updated.");
			System.out.println("************************");
			
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
			try{if(rs!=null)rs.close();}catch(Exception e){}
			try{if(pst!=null)pst.close();}catch(Exception e){}
			try{if(tcon!=null)tcon.close();}catch(Exception e){}
		}
		
		return "layout";
	}

}
