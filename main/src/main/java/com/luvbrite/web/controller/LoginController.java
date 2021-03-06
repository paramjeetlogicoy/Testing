package com.luvbrite.web.controller;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.query.Query;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.luvbrite.dao.CouponDAO;
import com.luvbrite.dao.LogDAO;
import com.luvbrite.dao.PasswordResetDAO;
import com.luvbrite.dao.UserDAO;
import com.luvbrite.services.ControlConfigService;
import com.luvbrite.services.EmailService;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.web.models.Coupon;
import com.luvbrite.web.models.Email;
import com.luvbrite.web.models.GenericResponse;
import com.luvbrite.web.models.Log;
import com.luvbrite.web.models.PasswordReset;
import com.luvbrite.web.models.User;
import com.luvbrite.web.models.UserDetailsExt;


@Controller
public class LoginController {
	
	private static Logger logger = Logger.getLogger(LoginController.class);
	
	@Autowired
	private UserDAO dao;
	
	@Autowired
	private ControlConfigService ccs;
	
	@Autowired
	private PasswordResetDAO pwdresetDAO;

	@Autowired
	private PasswordEncoder encoder;

	@Autowired
	private LogDAO logDao;
	
	@Autowired
	private EmailService emailService;

	@Autowired
	private CouponDAO couponDao;

	@RequestMapping(value = "/login")
	public String login(
			ModelMap model,
			@AuthenticationPrincipal UserDetailsExt user,
			@RequestParam(value = "error", required = false) String error,
			@RequestParam(value = "ret", required = false) String returnURL,
			@RequestParam(value = "logout", required = false) String logout,
		    RedirectAttributes ra) {

		if(user!=null){
			return "redirect:customer";
		}

		if (error != null) {
			model.addAttribute("error", "Invalid username and password!");
		}

		if (logout != null) {
			model.addAttribute("msg", "You've been logged out successfully.");
		}


		if (returnURL != null) {
			model.addAttribute("ret", returnURL);
		}


		return "login";	
	}


	@RequestMapping(value = "/login/customer")
	public String loginCorrection(
			ModelMap model,
			@AuthenticationPrincipal UserDetailsExt user,
		    RedirectAttributes ra) {

		if(user!=null){
			return "redirect:/customer";
		}
		
		return "login";	
	}
	

	@RequestMapping(value = "/register", method = RequestMethod.GET)
	public String register(@AuthenticationPrincipal 
			UserDetailsExt user){
		
		if(user!=null){
			return "redirect:customer";
		}
		
		return "register";
	}

	
	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public @ResponseBody GenericResponse createUser(
			@Validated @RequestBody User user, 
			BindingResult result){
		
		boolean activeUserOnRegistrationFlag = true;
		
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
			
			User u1 = dao.findOne("username", user.getUsername().toLowerCase());
			if(u1==null) usernameUnique = true;
			

			User u2 = dao.findOne("email", user.getEmail().toLowerCase());
			if(u2==null) emailUnique = true;
			
			
			if(user.getIdentifications() == null){
				proceed = false;
				r.setMessage("Please provide a valid ID card");
			}
			
			else if(user.getIdentifications().getIdCard() == null 
					|| user.getIdentifications().getIdCard().equals("")){
				
				proceed = false;
				r.setMessage("Please provide your ID card.");
				
			}
			
//			else if(user.getMemberType().equals("medical") && 
//					(user.getIdentifications().getRecomendation() == null 
//					|| user.getIdentifications().getRecomendation().equals(""))){
//				
//				proceed = false;
//				r.setMessage("Please provide doctors recommendation letter.");
//				
//			}
			
			
			if(proceed && emailUnique && usernameUnique){
				
				//Generate userId
				long userId = dao.getNextSeq();
				if(userId != 0l){
					
					user.set_id(userId);
					user.setActive(false);
					user.setStatus("pending");
					user.setRole("customer");
					user.setDateRegistered(Calendar.getInstance().getTime());
					
					//force username and email to lowercase
					user.setEmail(user.getEmail().toLowerCase());
					user.setUsername(user.getUsername().toLowerCase());
					
					//Encode the password before saving it
					String encodedPwd = encoder.encode(user.getPassword());
					user.setPassword(encodedPwd);
					
					
					if(activeUserOnRegistrationFlag){
						
						activeUserOnRegistration(user);
						
						
						r.setSuccess(true);
						r.setMessage("activated");
						
					}
					
					else{
						


						dao.save(user);
						
						
						/* Update Log */
						try {
							
							Log log = new Log();
							log.setCollection("users");
							log.setDetails("user created.");
							log.setDate(Calendar.getInstance().getTime());
							log.setKey(userId);
							log.setUser("System");
							
							logDao.save(log);						
						}
						catch(Exception e){
							logger.error(Exceptions.giveStackTrace(e));
						}
						
						
						
						/* Email User */
						try {
							
							Email email = new Email();
							email.setEmailTemplate("registration");
							email.setFromName("Luvbrite Security");
							email.setFromEmail("no-reply@luvbrite.com");
							email.setRecipientEmail(user.getEmail());
							email.setRecipientName(user.getFname());
							
							email.setSubject("Luvbrite Registration Details");
							email.setEmailTitle("Registration Email");
							email.setEmailInfo("Info about your recent registration at www.luvbrite.com");	
							
							emailService.sendEmail(email);
						}
						catch(Exception e){
							logger.error(Exceptions.giveStackTrace(e));
						}
						
						
						if(!ccs.getcOps().isDev()){

							/* Email Admin */
							try {

								Email email = new Email();
								email.setEmailTemplate("registration-admin");
								email.setFromName("Luvbrite Security");
								email.setFromEmail("no-reply@luvbrite.com");
								email.setRecipientEmail("new-users@luvbrite.com");

								email.setSubject("Luvbrite New User Registration");
								email.setEmailTitle("Registration Admin Email");
								email.setEmailInfo("Info about new user registration at www.luvbrite.com");	

								email.setEmail(user);

								emailService.sendEmail(email);
							}
							catch(Exception e){
								logger.error(Exceptions.giveStackTrace(e));
							}
						}
						
						
						r.setSuccess(true);
						r.setMessage(userId + "");
						
					}
				}

				else {
					
					r.setMessage("Error generating new User.");
				}			
			}

			
			else {

				if(proceed){
					
					if(!emailUnique && !usernameUnique){
						r.setMessage("User already exists with this username and email. "
								+ "If you forgot your password, please reset your "
								+ "password by visiting <a href='/resetrequest'>this page</a>.");
					}

					else if(!emailUnique){
						r.setMessage("User already exists with this email");
					}

					else {
						r.setMessage("User already exists with this username. Please provide a different username.");
					}
				}
			}
			
		}
		
		
		return r;		
	}
	
	
	@RequestMapping(value = "/register/validateuser", method = RequestMethod.POST)
	public @ResponseBody GenericResponse validateUsernameEmail(@RequestBody String usernameEmail){
		
		GenericResponse r = new GenericResponse();		
		r.setSuccess(false);
		
		Query<User> query = dao.createQuery();
		query.or(query.criteria("username").equal(usernameEmail),
				query.criteria("email").equal(usernameEmail));
		
		User user = query.get();
		if(user != null) {
			r.setSuccess(true);
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
	
	
	@RequestMapping(value = "/pending-registration")
	public String pendingRegistration(ModelMap model){	
		
		model.addAttribute("title", "Pending Registration");
		model.addAttribute("message", "registering with Luvbrite Collective");
		
		
		return "pending-registration";		
	}
	
	@RequestMapping(value = "/account-activated")
	public String accountActivated(ModelMap model){	
		return "account-activated";		
	}
	
	
	@RequestMapping(value = "/pending-verification")
	public String pendingVerification(ModelMap model){	

		
		model.addAttribute("title", "Pending Verification");
		model.addAttribute("message", "uploading your recent recommendation letter.");		
		
		
		return "pending-registration";		
	}	
	
	
	@RequestMapping(value = "/account-expired/{expiredUserId}")
	public String accountExpired(ModelMap model, @PathVariable long expiredUserId){
		
		Query<User> query = dao.createQuery();
		
		query
		.filter("_id", expiredUserId)
		.retrievedFields(true, "_id", "fname", "lname", "username", "email");
		
		//System.out.println(" Query - " + query.getQueryObject().toString());
		
		User user = query.get();
		if(user != null){
			model.addAttribute("expiredUser", user);			
		}
		
		return "account-expired";		
	}
	
	private String activeUserOnRegistration(User user){
		
		String resp = "";
		
		user.setActive(true);
		user.setStatus("active");
		
		//Save user
		dao.save(user);
		
		
		String newCouponCode = (user.getUsername() + "rewards10").toLowerCase();
		Coupon cp = couponDao.get(newCouponCode);
		if(cp==null){
			cp = new Coupon();
			
			Calendar now = Calendar.getInstance();
			now.add(Calendar.MONTH, 6);
			
			cp.set_id(newCouponCode);
			cp.setActive(true);
			cp.setCouponValue(10d);
			cp.setDescription("First time patient gift.");
			cp.setEmails(Arrays.asList(new String[] { user.getEmail()} ));
			cp.setExpiry(now.getTime());
			cp.setMaxDiscAmt(0);
			cp.setMaxUsageCount(10);
			cp.setMinAmt(0);
			cp.setType("F");
			cp.setUsageCount(0);
			
			couponDao.save(cp);
			
		}
		else{
			
			logger.error("CP:Coupon " + newCouponCode + " already exists. So not created!");
		}
		
		
		/* Update Log */
		try {
			
			Log log = new Log();
			log.setCollection("users");
			log.setDetails("user created, activated and emailed" + 
					(cp!=null? " with coupon " + cp.get_id() + "." : "."));
			log.setDate(Calendar.getInstance().getTime());
			log.setKey(user.get_id());
			log.setUser(user.getUsername());
			
			logDao.save(log);					
		}
		catch(Exception e){
			logger.error(Exceptions.giveStackTrace(e));
		}
		
		
		
		/* Email */
		try {
			
			Email email = new Email();
			email.setEmailTemplate("activation");
			email.setFromName("Luvbrite Security");
			email.setFromEmail("no-reply@luvbrite.com");
			email.setRecipientEmail(user.getEmail());
			email.setRecipientName(user.getFname());					
			email.setSubject("Luvbrite Account Activation Details");
			email.setEmailTitle("Acount Activation Email");
			email.setEmailInfo("your www.luvbrite.com account activated");	
			
			email.setEmail(cp);
			
			emailService.sendEmail(email);
		}
		catch(Exception e){
			logger.error(Exceptions.giveStackTrace(e));
		}

		
		
		if(!ccs.getcOps().isDev()){

			/* Email Admin */
			try {

				Email email = new Email();
				email.setEmailTemplate("registration-admin");
				email.setFromName("Luvbrite Security");
				email.setFromEmail("no-reply@luvbrite.com");
				email.setRecipientEmail("new-users@luvbrite.com");

				email.setSubject("Luvbrite New User Registration");
				email.setEmailTitle("Registration Admin Email");
				email.setEmailInfo("Info about new user registration at www.luvbrite.com");	

				email.setEmail(user);

				emailService.sendEmail(email);
			}
			catch(Exception e){
				logger.error(Exceptions.giveStackTrace(e));
			}
		}
		
		return resp;
	}
	
	
	@RequestMapping(value = "/reco-reupload")
	public @ResponseBody GenericResponse recoReupload(@RequestBody User user){
		
		GenericResponse r = new GenericResponse();
		r.setSuccess(false);
		
		if(user != null && user.getIdentifications() != null){
			
			if(user.getIdentifications().getRecomendation() == null 
					|| user.getIdentifications().getRecomendation().equals("")){
				
				r.setMessage("Please provide doctors recommendation letter.");
				return r;
			}
			
			
			User userDb = dao.get(user.get_id());
			if(userDb != null){
				
				String oldRecoFileLoc = userDb.getIdentifications() != null ? 
								userDb.getIdentifications().getRecomendation() : "";
				
				userDb.getIdentifications().setRecomendation(user.getIdentifications().getRecomendation());
				userDb.getIdentifications().setRecoExpiry(user.getIdentifications().getRecoExpiry());
				userDb.setStatus("new-reco-uploaded");

				dao.save(userDb);
				
				
				/* Update Log */
				try {
					
					Log log = new Log();
					log.setCollection("users");
					log.setDetails("user new reco upload. Old reco - " + oldRecoFileLoc);
					log.setDate(Calendar.getInstance().getTime());
					log.setKey(userDb.get_id());
					log.setUser("customer");
					
					logDao.save(log);						
				}
				catch(Exception e){
					logger.error(Exceptions.giveStackTrace(e));
				}
				
				
				
				/* Email User */
				try {
					
					Email email = new Email();
					email.setEmailTemplate("reco-reupload");
					email.setFromName("Luvbrite Security");
					email.setFromEmail("no-reply@luvbrite.com");
					email.setRecipientEmail(userDb.getEmail());
					email.setRecipientName(userDb.getFname());
					
					email.setSubject("Luvbrite received your recommendation letter");
					email.setEmailTitle("New Recommendation Letter");
					email.setEmailInfo("Info about your recent upload at www.luvbrite.com");	
					
					emailService.sendEmail(email);
				}
				catch(Exception e){
					logger.error(Exceptions.giveStackTrace(e));
				}
				
				
				if(!ccs.getcOps().isDev()){

					/* Email Admin */
					try {

						Email email = new Email();
						email.setEmailTemplate("reco-reupload-admin");
						email.setFromName("Luvbrite Security");
						email.setFromEmail("no-reply@luvbrite.com");
						email.setRecipientEmail("new-users@luvbrite.com");

						email.setSubject("Luvbrite New Recommendation Uploaded");
						email.setEmailTitle("Recommendation Update Admin Email");
						email.setEmailInfo("Info about new recommendation uploaded at www.luvbrite.com");	

						email.setEmail(userDb);

						emailService.sendEmail(email);
					}
					catch(Exception e){
						logger.error(Exceptions.giveStackTrace(e));
					}
				}
				
				
				r.setSuccess(true);
			}
			
		}
		
		return r;		
	}
}
