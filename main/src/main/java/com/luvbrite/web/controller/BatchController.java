package com.luvbrite.web.controller;

import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.luvbrite.dao.UserDAO;
import com.luvbrite.services.EmailService;
import com.luvbrite.web.models.Email;
import com.luvbrite.web.models.RecoExpiry;
import com.luvbrite.web.models.User;


@Controller
@RequestMapping(value = "/batch")
public class BatchController {
	
	@Autowired
	private EmailService emailService;

	@Autowired
	private UserDAO userDao;
	
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
