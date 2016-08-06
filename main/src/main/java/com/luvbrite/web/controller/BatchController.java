package com.luvbrite.web.controller;

import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.luvbrite.dao.OrderDAO;
import com.luvbrite.dao.UserDAO;
import com.luvbrite.services.EmailService;
import com.luvbrite.web.models.Email;
import com.luvbrite.web.models.GenericResponse;
import com.luvbrite.web.models.Order;
import com.luvbrite.web.models.RecoExpiry;
import com.luvbrite.web.models.User;
import com.luvbrite.web.models.UserDetailsExt;
import com.luvbrite.web.models.UserIdentity;


@Controller
@RequestMapping(value = "/batch")
public class BatchController {
	
	@Autowired
	private EmailService emailService;

	@Autowired
	private UserDAO userDao;

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

	
	@RequestMapping(value = "/learning-center")
	public String learningCenter(
			@AuthenticationPrincipal UserDetailsExt user, 
			ModelMap model) {
		
		if(user!=null && user.isEnabled())
			model.addAttribute("userId", user.getId());
		
		return "learning-center";		
	}
	
	
	@RequestMapping(value = "/testemail")
	public @ResponseBody GenericResponse testemail(){
		GenericResponse r = new GenericResponse();
		r.setSuccess(true);
		
		Email email = new Email();
		email.setEmailTemplate("password-reset");
		email.setFromEmail("no-reply@luvbrite.com");
		email.setRecipientEmail("admin@codla.com");
		email.setRecipientName("Gautam Krishna");
		email.setSubject("Spring Email Simple");
		email.setEmailTitle("Password Reset Email");
		email.setEmailInfo("Info about changing your password");
		
		User user = new User();
		user.setPassword("876472364876234");
		
		email.setEmail(user);
		
		try {
			
			emailService.sendEmail(email);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return r;	
	}	
	
	
	
	@RequestMapping(value = "/pending-registration")
	public String pendingRegistration(@AuthenticationPrincipal 
			UserDetailsExt user, ModelMap model){	
		
		if(user!=null && user.isEnabled()) {
			model.addAttribute("userName", user.getUsername());
		}
		
		return "pending-registration";		
	}	
	
	
	
	@RequestMapping(value = "/contact-us")
	public String contact(@AuthenticationPrincipal 
			UserDetailsExt user, ModelMap model){	
		
		if(user!=null && user.isEnabled())
			model.addAttribute("userId", user.getId());
		
		return "contact-us";		
	}
	
	
	
	@RequestMapping(value = "/service-area")
	public String serviceArea(@AuthenticationPrincipal 
			UserDetailsExt user, ModelMap model){	
		
		if(user!=null && user.isEnabled())
			model.addAttribute("userId", user.getId());
		
		return "service-area";		
	}	
	
	
	
	@RequestMapping(value = "/localbox")
	public String locabox(@AuthenticationPrincipal 
			UserDetailsExt user, ModelMap model){	
		
		if(user!=null && user.isEnabled())
			model.addAttribute("userId", user.getId());
		
		return "localbox";		
	}
	
	
	
	@RequestMapping(value = "/generic-error")
	public String genericError(){			
		return "generic-error";		
	}
	
	
	
	@RequestMapping(value = "/403")
	public String accessDenied(@AuthenticationPrincipal 
			UserDetailsExt user, ModelMap model){	
		
		if(user!=null && user.isEnabled())
			model.addAttribute("userId", user.getId());
		
		model.addAttribute("msg", "There was some error accessing this page");
		
		return "403";		
	}
	
	
	
	@RequestMapping(value = "/not-found")
	public String accessDenied(){
		
		return "404";		
	}
	

	@RequestMapping(value = "/testemail/{templateName}")
	public String emailTemplateTest(@PathVariable String templateName, ModelMap model){	

		String template = "";
		if(templateName != null){
			template = templateName.indexOf(".html") > 0 ? 
					templateName.replace(".html", "") : templateName;


					Email email = new Email();
					email.setEmailTemplate(template);
					email.setFromEmail("");
					email.setRecipientEmail("info@luvbrite.com");
					email.setRecipientName("Luvbrite Collection");
					email.setSubject("");
					email.setEmailTitle(template + " Email");
					email.setEmailInfo("Test Info");
					
					
					if(templateName.indexOf("password") > -1){
						User user = new User();
						user.setPassword("876472364876234");

						email.setEmail(user);
					}
					
					else if(templateName.indexOf("registration-admin") > -1){
						User user = new User();
						
						user.set_id(1l);
						user.setFname("Main");
						user.setLname("Admin");
						user.setEmail("some@email.com");
						
						UserIdentity ids = new UserIdentity();
						ids.setIdCard("/user/IMG_8471-150x150.jpg");
						ids.setRecomendation("/user/IMG_8471-150x150.jpg");
						
						user.setIdentifications(ids);

						email.setEmail(user);
					}
					
					else if(templateName.indexOf("order-confirmation") > -1){
						Order order = orderDao.findOne("orderNumber", 8773);

						email.setEmail(order);
					}

					model.addAttribute("emailObj", email);		

					return "layout";

		}

		return "404";		
	}
}
