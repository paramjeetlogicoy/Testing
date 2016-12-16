package com.luvbrite.web.controller;

import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.luvbrite.dao.LogDAO;
import com.luvbrite.dao.UserDAO;
import com.luvbrite.services.EmailService;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.web.models.Email;
import com.luvbrite.web.models.Log;
import com.luvbrite.web.models.RecoExpiry;
import com.luvbrite.web.models.User;


@Controller
@RequestMapping(value = "/batch")
public class BatchController {
	
	private static Logger logger = Logger.getLogger(BatchController.class);
	
	@Autowired
	private EmailService emailService;

	@Autowired
	private UserDAO userDao;	
	
	@Autowired
	private LogDAO logDao;
	
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
		
		/** IMPORTANT - We need the full User object here. 
		 * The same object is passed to deactivateUser() method
		 * Don't user projection or retrieveFields in the query. **/
		List<User> usersE = userDao.createQuery()
				.field("active").equal(true)
				.field("identifications.recoExpiry").lessThan(now.getTime())
				.order("identifications.recoExpiry")
				.asList();

		//Deactivate these expired users
		deactivateUser(usersE);
		
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
		email.setRecipientEmail("reco_expiry_notice@luvbrite.com");
		email.setRecipientName("Reco Expiry");
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
	
	
	private void deactivateUser(List<User> users){
		
		try {
			
			if(users != null){
				
				for(User user : users){

					user.setActive(false);
					user.setStatus("reco-expired");

					userDao.save(user);

					/**
					 * Update Log
					 * */
					try {

						Log log = new Log();
						log.setCollection("users");
						log.setDetails("(Batch Job) user status changed to reco-expired.");
						log.setDate(Calendar.getInstance().getTime());
						log.setKey(user.get_id());
						log.setUser("system");

						logDao.save(log);					
					}
					catch(Exception e){
						logger.error(Exceptions.giveStackTrace(e));
					}
				}
				
				logger.error(users.size() + " Users marked as reco-expired! ");
			}
			
		} catch(Exception e){
			logger.error(Exceptions.giveStackTrace(e));
		}
	}

	
	@RequestMapping(value = "/customers-expiring-in-6-months")
	public @ResponseBody List<User> customerList(){
		
		Calendar month6 = Calendar.getInstance();
		month6.add(Calendar.MONTH, 6);
		
		//Expiring in next 3 days
		List<User> users = userDao.createQuery()
				.field("active").equal(true)
				.field("identifications.recoExpiry").greaterThan(month6.getTime())
				.order("_id")
				.retrievedFields(true, "identifications.recoExpiry", "fname", "lname", "phone")
				.asList();		
		
		return users;
	}
}
