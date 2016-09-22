package com.luvbrite.web.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

	
	@RequestMapping(value = "/onetime/update-dispatch-info")
	public @ResponseBody String updateDispatch() throws Exception{
		
		String query = "SELECT ds.*, d.driver_name "
				+ "FROM dispatch_sales_info ds "
					+ "LEFT JOIN drivers d ON d.id = ds.driver_id"
				
				+ "WHERE ds.id = "
				+ "(SELECT dispatch_sales_id FROM online_order_info WHERE order_number = ?)";
		
		Connection tcon = null;
		PreparedStatement pst = null;
		ResultSet rs = null;	
		
		String driverName = "";
		Date dateFinished = null;
		
		try {
			
			tcon = ds.getConnection();			
			List<Order> orders = orderDao.find().asList();
			if(orders != null && orders.size() > 0){
				
				for(Order o: orders){
					
					pst = tcon.prepareStatement(query);
					pst.setString(1, o.getOrderNumber()+"");
					
					rs = pst.executeQuery();
					if(rs.next()){
						
						driverName = rs.getString("driver_name")==null ? "No Driver" : rs.getString("driver_name");
						dateFinished = rs.getTimestamp("date_finished")==null ? null : rs.getTimestamp("date_finished");
					}
					
					else {
						System.out.println("No dispatch_sales_info found for ordernumber " + o.getOrderNumber());
					}
					
				}				
			}
			
			if(rs!=null){rs.close();rs=null;}
			if(pst!=null){pst.close();pst=null;}
			
			tcon.close();tcon=null;
			
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
