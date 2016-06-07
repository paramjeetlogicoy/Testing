package com.luvbrite.web.controller;

import java.util.Calendar;
import java.util.List;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.luvbrite.dao.LogDAO;
import com.luvbrite.dao.PasswordResetDAO;
import com.luvbrite.dao.UserDAO;
import com.luvbrite.services.EmailService;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.web.models.Email;
import com.luvbrite.web.models.GenericResponse;
import com.luvbrite.web.models.Log;
import com.luvbrite.web.models.PasswordReset;
import com.luvbrite.web.models.User;


@Controller
public class LoginController {
	
	private static Logger logger = LoggerFactory.getLogger(LoginController.class);
	
	@Autowired
	private UserDAO dao;
	
	@Autowired
	private PasswordResetDAO pwdresetDAO;

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	private LogDAO logDao;
	
	@Autowired
	private EmailService emailService;


	@RequestMapping(value = "/login")
	public ModelAndView login(
			
			@RequestParam(value = "error", required = false) String error,
			@RequestParam(value = "ret", required = false) String returnURL,
			@RequestParam(value = "logout", required = false) String logout) {

			ModelAndView model = new ModelAndView();
			
			if (error != null) {
				model.addObject("error", "Invalid username and password!");
			}

			if (logout != null) {
				model.addObject("msg", "You've been logged out successfully.");
			}


			if (returnURL != null) {
				model.addObject("ret", returnURL);
			}
			
			model.setViewName("login");

			return model;	
	}
	

	@RequestMapping(value = "/register", method = RequestMethod.GET)
	public String register(){
		return "register";
	}

	
	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public @ResponseBody GenericResponse createUser(
			@Validated @RequestBody User user, 
			BindingResult result){
		
		GenericResponse r = new GenericResponse();		
		r.setSuccess(false);
		
		if(result.hasErrors()){
			
			StringBuilder errMsg = new StringBuilder();
			
			List<FieldError> errors = result.getFieldErrors();
			for (FieldError error : errors ) {
				 errMsg
				 .append(" - ")
				 .append(error.getDefaultMessage())
				 .append("<br />");
			}
			
			r.setMessage(errMsg.toString());
		
		}
		
		else {
			
			/**
			 * Before creating the user, we need to make sure that the 
			 * email and username are unique
			 **/
			
			boolean usernameUnique = false,
					emailUnique = false,
					proceed = true;
			
			User u1 = dao.findOne("username", user.getUsername());
			if(u1==null) usernameUnique = true;
			

			User u2 = dao.findOne("email", user.getEmail());
			if(u2==null) emailUnique = true;
			
			
			if(user.getIdentifications() == null){
				proceed = false;
				r.setMessage("Please provide your ID card and doctors recommendation letter.");
			}
			
			else if(user.getIdentifications().getIdCard() == null 
					|| user.getIdentifications().getIdCard().equals("")){
				
				proceed = false;
				r.setMessage("Please provide your ID card.");
				
			}
			
			else if(user.getIdentifications().getRecomendation() == null 
					|| user.getIdentifications().getRecomendation().equals("")){
				
				proceed = false;
				r.setMessage("Please provide doctors recommendation letter.");
				
			}
			
			
			if(proceed && emailUnique && usernameUnique){
				
				//Generate userId
				long userId = dao.getNextSeq();
				if(userId != 0l){
					
					user.set_id(userId);
					user.setActive(false);
					user.setRole("customer");
					user.setDateRegistered(Calendar.getInstance().getTime());
					
					//Encode the password before saving it
					String encodedPwd = encoder.encode(user.getPassword());
					user.setPassword(encodedPwd);

					dao.save(user);
					
					
					/* Update Log */
					try {
						
						Log log = new Log();
						log.setCollection("users");
						log.setDetails("user created.");
						log.setDate(Calendar.getInstance().getTime());
						log.setKey(userId);
						
						logDao.save(log);						
					}
					catch(Exception e){
						logger.error(Exceptions.giveStackTrace(e));
					}
					
					
					
					/* Email */
					try {
						
						Email email = new Email();
						email.setEmailTemplate("registration");
						email.setFromName("Luvbrite Security");
						email.setFromEmail("no-reply@luvbrite.com");
						email.setRecipientEmail(user.getEmail());
						email.setRecipientName(user.getFname());
						
						//email.setBccs(Arrays.asList(new String[]{"new-users@luvbrite.com"}));
						
						email.setSubject("Luvbrite Registration Details");
						email.setEmailTitle("Registration Email");
						email.setEmailInfo("Info about your recent registration at www.luvbrite.com");	
						
						emailService.sendEmail(email);
					}
					catch(Exception e){
						logger.error(Exceptions.giveStackTrace(e));
					}
					
					
					r.setSuccess(true);
					r.setMessage(userId + "");
				}

				else {
					
					r.setMessage("Error generating new User.");
				}			
			}

			
			else {

				if(proceed){
					
					if(!emailUnique && !usernameUnique){
						r.setMessage("User exist with this username and email");
					}

					else if(!emailUnique){
						r.setMessage("User exist with this email");
					}

					else {
						r.setMessage("User already exist with this username. Please provide a different username.");
					}
				}
			}
			
		}
		
		
		return r;		
	}
	

	@RequestMapping(value = "/resetrequest", method = RequestMethod.GET)
	public String requestReset(ModelMap model){

		model.addAttribute("type", "request");
		
		return "reset";
	}
	

	@RequestMapping(value = "/createreset", method = RequestMethod.GET)
	public @ResponseBody GenericResponse createResetCode(
			@RequestParam(value="u", required=false) String usernameEmail){
			
		GenericResponse r = new GenericResponse();
		r.setSuccess(false);
		
		if(usernameEmail == null){
			r.setMessage("Invalid username/email");
			return r;
		}
		
		ObjectId code = null;
		
		if(usernameEmail != null && !usernameEmail.trim().equals("")){
			
			User u = dao.createQuery().field("username")
					.equal(usernameEmail).retrievedFields(true, "username", "email", "fname")
					.get();
			
			if(u!=null){
				
				PasswordReset pr = new PasswordReset();
				pr.setEmail(u.getEmail());
				pr.setUsername(u.getUsername());
				pr.setDate(Calendar.getInstance().getTime());
				
				pwdresetDAO.save(pr);
				
				code = pr.get_id();
				
				//System.out.println(" Code u - " + code);
				r.setSuccess(true);
				
			}
			else{

				u = dao.createQuery().field("email")
						.equal(usernameEmail).retrievedFields(true, "username", "email", "fname")
						.get();

				if(u!=null){

					PasswordReset pr = new PasswordReset();
					pr.setEmail(u.getEmail());
					pr.setUsername(u.getUsername());
					pr.setDate(Calendar.getInstance().getTime());

					pwdresetDAO.save(pr);

					code = pr.get_id();

					//System.out.println(" Code e - " + code);				
					r.setSuccess(true);
				}
				else{

					r.setMessage("No valid user found");
				}
			}
			
			
			if(r.isSuccess()){
				
				//Sent Email
				try {
					
					Email email = new Email();
					email.setEmailTemplate("password-reset");
					email.setFromName("Luvbrite Security");
					email.setFromEmail("no-reply@luvbrite.com");
					email.setRecipientEmail(u.getEmail());
					email.setRecipientName(u.getFname());
					email.setSubject("Luvbrite Password Reset Request");
					email.setEmailTitle("Password Reset Email");
					email.setEmailInfo("Info about changing your password");
					
					User user = new User();
					user.setPassword(code.toString());
					
					email.setEmail(user);
					
					emailService.sendEmail(email);
					
				}catch(Exception e){
					logger.error(Exceptions.giveStackTrace(e));
					r.setSuccess(false);
					r.setMessage("There was some error sending the reset email.");
				}				
			}
		}
		
		else {

			r.setMessage("No valid user found");
		}
		
		return r;
	}
	

	@RequestMapping(value = "/reset/{code}", method = RequestMethod.GET)
	public String resetPassword(ModelMap model, @PathVariable ObjectId code){
		
		if(code == null){
			model.addAttribute("msg", "There was some error accessing this page");
			return "403";
		}
		
		model.addAttribute("type", "password");
		
		PasswordReset pr = pwdresetDAO.get(code);
		if(pr == null){
			model.addAttribute("msg", "Not a valid reset code");
		}
		
		else{
			
			model.addAttribute("username", pr.getUsername());
			model.addAttribute("email", pr.getEmail());
		}
		
		return "reset";
	}

	
	@RequestMapping(value = "/reset/savep", method = RequestMethod.POST)
	public @ResponseBody GenericResponse savePassword(@RequestBody User user){
		
		GenericResponse r = new GenericResponse();
			
		if(user.getPassword().trim().equals("")){
			r.setMessage("Password is empty");			
		}
		else{
			
			User userDb = dao.findOne("email", user.getEmail());			
			if(userDb != null && userDb.getUsername().equals(user.getUsername())){		
				
				//Encode the password before saving it
				String encodedPwd = encoder.encode(user.getPassword());
				userDb.setPassword(encodedPwd);
				
				/**
				 * Here we are saving only the password.
				 * Remaining information is same as that 
				 * pulled from DB
				 **/			
				dao.save(userDb);
				

				
				
				/**
				 * Update Log
				 * */
				try {
					
					Log log = new Log();
					log.setCollection("users");
					log.setDetails("user.password changed by reset password.");
					log.setDate(Calendar.getInstance().getTime());
					log.setKey(userDb.get_id());
					log.setUser("system");
					
					logDao.save(log);					
				}
				catch(Exception e){
					logger.error(Exceptions.giveStackTrace(e));
				}
				
				
				
				//Sent Email
				try {
					
					Email email = new Email();
					email.setEmailTemplate("password-changed");
					email.setFromName("Luvbrite Security");
					email.setFromEmail("no-reply@luvbrite.com");
					email.setRecipientEmail(userDb.getEmail());
					email.setRecipientName(userDb.getFname());
					email.setSubject(userDb.getFname() + ", your password was successfully changed");
					email.setEmailTitle("Your password was successfully changed");
					email.setEmailInfo("Your password was changed");
					
					
					emailService.sendEmail(email);
					
				}catch(Exception e){
					logger.error(Exceptions.giveStackTrace(e));
					r.setSuccess(false);
					r.setMessage("There was some error sending the reset email.");
				}
				
				
					
				r.setSuccess(true);
			}
			
			else {
				r.setMessage("No user found for this email and username");
			}
		}
		
		
		return r;		
	}
}
