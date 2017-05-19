package com.luvbrite.web.controller.admin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luvbrite.dao.CouponDAO;
import com.luvbrite.dao.LogDAO;
import com.luvbrite.dao.OrderDAO;
import com.luvbrite.dao.UserDAO;
import com.luvbrite.services.EmailService;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.utils.PaginationLogic;
import com.luvbrite.web.models.Coupon;
import com.luvbrite.web.models.Email;
import com.luvbrite.web.models.GenericResponse;
import com.luvbrite.web.models.Log;
import com.luvbrite.web.models.Order;
import com.luvbrite.web.models.OrderCustomer;
import com.luvbrite.web.models.ResponseWithPg;
import com.luvbrite.web.models.User;
import com.luvbrite.web.models.UserDetailsExt;
import com.luvbrite.web.validator.UserValidator;


@Controller
@RequestMapping(value = {"/admin/users","/admin/user"})
public class UsersController {
	
	private static Logger logger = Logger.getLogger(UsersController.class);
	
	@Autowired
	private UserDAO dao;
	
	
	@Autowired
	private LogDAO logDao;
	

	@Autowired
	private OrderDAO orderDao;
	
	
	@Autowired
	private UserValidator userValidator;
	

	@Autowired
	private CouponDAO couponDao;
	
	
	@Autowired
	PasswordEncoder encoder;
	
	@Autowired
	private EmailService emailService;
	
	
	@InitBinder("user")
	protected void InitBinder(WebDataBinder binder){
		binder.setValidator(userValidator);
	}
	
	
	@RequestMapping(method = RequestMethod.GET)
	public String mainPage(ModelMap model){		
		return "admin/users";		
	}
	
	
	@RequestMapping(value = "/json/", method = RequestMethod.GET)
	public @ResponseBody ResponseWithPg users(
			@RequestParam(value="p", required=false) Integer page,
			@RequestParam(value="q", required=false) String query,
			@RequestParam(value="s", required=false) String status,
			@RequestParam(value="o", required=false) String order){

		ResponseWithPg rpg = new ResponseWithPg();		

		if(query==null) query = "";
		if(order==null) order = "-_id";
		if(status==null) status = "all";
		
		int offset = 0,
			limit = 15; //itemsPerPage
		
		if(page==null) page = 1;
		if(page >1) offset = (page-1)*limit;
		
		PaginationLogic pgl = new PaginationLogic((int)dao.count(query, status), limit, page);
		List<User> users = dao.find(order, limit, offset, query, status);

		rpg.setSuccess(true);
		rpg.setPg(pgl.getPg());
		rpg.setRespData(users);
		
		return rpg;	
	}

	@RequestMapping(value = "/json/{id}")
	public @ResponseBody User admin(@PathVariable long id){			
		return dao.get(id);		
	}

	@RequestMapping(value = "/json/create", method = RequestMethod.POST)
	public @ResponseBody GenericResponse createUser(
			@Validated @RequestBody User user, 
			BindingResult result, @AuthenticationPrincipal 
			UserDetailsExt userExt){
		
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
					emailUnique = false;
			
			User u1 = dao.findOne("username", user.getUsername());
			if(u1==null) usernameUnique = true;
			

			User u2 = dao.findOne("email", user.getEmail());
			if(u2==null) emailUnique = true;
			
			
			if(emailUnique && usernameUnique){
				
				//Generate userId
				long userId = dao.getNextSeq();
				if(userId != 0l){
					user.set_id(userId);
					user.setDateRegistered(Calendar.getInstance().getTime());
					user.setStatus("new");
					
					//Encode the password before saving it
					String encodedPwd = encoder.encode(user.getPassword());
					user.setPassword(encodedPwd);

					dao.save(user);
					
					
					/**
					 * Update Log
					 * */
					try {
						
						Log log = new Log();
						log.setCollection("users");
						log.setDetails("user created.");
						log.setDate(Calendar.getInstance().getTime());
						log.setKey(userId);
						log.setUser(userExt.getUsername());
						
						logDao.save(log);						
					}
					catch(Exception e){
						logger.error(Exceptions.giveStackTrace(e));
					}
					
					
					r.setSuccess(true);
					r.setMessage(userId + "");
				}

				else {
					
					r.setMessage("Error generating new User ID.");
				}			
			}

			
			else {

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
		
		
		return r;		
	}

	
	@RequestMapping(value = "/json/savep", method = RequestMethod.POST)
	public @ResponseBody GenericResponse savePassword(
			@RequestBody User user, @AuthenticationPrincipal 
			UserDetailsExt userExt){
		
		GenericResponse r = new GenericResponse();
			
		if(user.getPassword().trim().equals("")){
			r.setMessage("Password is empty");			
		}
		else{
			
			User userDb = dao.get(user.get_id());			
			if(userDb.get_id()==user.get_id()){		
				
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
					log.setDetails("user.password changed.");
					log.setDate(Calendar.getInstance().getTime());
					log.setKey(user.get_id());
					log.setUser(userExt.getUsername());
					
					logDao.save(log);					
				}
				catch(Exception e){
					logger.error(Exceptions.giveStackTrace(e));
				}
				
				
				r.setSuccess(true);
				r.setMessage(encodedPwd);
			}
			
			else {
				r.setMessage("Invalid User ID");
			}
		}
		
		
		return r;		
	}

	
	@RequestMapping(value = "json/activate-n-email/{userId}", method = RequestMethod.GET)
	public @ResponseBody GenericResponse activateNemail(
			@PathVariable Long userId, 
			@AuthenticationPrincipal UserDetailsExt user, 
			@RequestParam(value="c", required=false) String coupon){
		
		GenericResponse r = new GenericResponse();
		r.setSuccess(false);
		r.setMessage("");
			
		if(userId == null){
			r.setMessage("Invalid user ID");			
		}
		else{
			
			User userDb = dao.get(userId);			
			if(userDb!=null){		
				
				userDb.setActive(true);
				userDb.setStatus("active");
				
				/**
				 * Here we are only changing the status to active.
				 * Remaining information is same as that 
				 * pulled from DB
				 **/			
				dao.save(userDb);
				
				Coupon cp = null;
				if(coupon != null && coupon.equals("coupon")){
					String newCouponCode = (userDb.getUsername() + "rewards10").toLowerCase();
					cp = couponDao.get(newCouponCode);
					if(cp==null){
						cp = new Coupon();
						
						Calendar now = Calendar.getInstance();
						now.add(Calendar.MONTH, 6);
						
						cp.set_id(newCouponCode);
						cp.setActive(true);
						cp.setCouponValue(10d);
						cp.setDescription("First time patient gift.");
						cp.setEmails(Arrays.asList(new String[] { userDb.getEmail()} ));
						cp.setExpiry(now.getTime());
						cp.setMaxDiscAmt(0);
						cp.setMaxUsageCount(10);
						cp.setMinAmt(0);
						cp.setType("F");
						cp.setUsageCount(0);
						
						couponDao.save(cp);
						
						r.setMessage(newCouponCode);
					}
					else{
						
						r.setMessage("CP:Coupon " + newCouponCode + " already exists. So not created!");
					}
				}
				
				
				/* Update Log */
				try {
					
					Log log = new Log();
					log.setCollection("users");
					log.setDetails("user activated and emailed" + (cp!=null? " with coupon " + cp.get_id() + "." : "."));
					log.setDate(Calendar.getInstance().getTime());
					log.setKey(userDb.get_id());
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
					email.setRecipientEmail(userDb.getEmail());
					email.setRecipientName(userDb.getFname());					
					email.setSubject("Luvbrite Account Activation Details");
					email.setEmailTitle("Acount Activation Email");
					email.setEmailInfo("your www.luvbrite.com account activated");	
					
					email.setEmail(cp);
					
					emailService.sendEmail(email);
				}
				catch(Exception e){
					logger.error(Exceptions.giveStackTrace(e));
				}
				
				
				r.setSuccess(true);
			}
			
			else {
				r.setMessage("No valid user found with ID - " + userId);
			}
		}
		
		
		return r;		
	}

	
	@RequestMapping(value = "json/deny/{userId}", method = RequestMethod.GET)
	public @ResponseBody GenericResponse denyUser(
			@PathVariable Long userId, 
			@AuthenticationPrincipal UserDetailsExt user){
		
		GenericResponse r = new GenericResponse();
		r.setSuccess(false);
		r.setMessage("");
			
		if(userId == null){
			r.setMessage("Invalid user ID");			
		}
		else{
			
			User userDb = dao.get(userId);			
			if(userDb!=null){		
				
				userDb.setActive(false);
				userDb.setStatus("declined");
				
				/**
				 * Here we are only changing the status to declined
				 * and setting active flag to false.
				 * Remaining information is same as that 
				 * pulled from DB
				 **/			
				dao.save(userDb);
				
				/* Update Log */
				try {
					
					Log log = new Log();
					log.setCollection("users");
					log.setDetails("user declines via direct URL");
					log.setDate(Calendar.getInstance().getTime());
					log.setKey(userDb.get_id());
					log.setUser(user.getUsername());
					
					logDao.save(log);					
				}
				catch(Exception e){
					logger.error(Exceptions.giveStackTrace(e));
				}
				
				r.setSuccess(true);
			}
			
			else {
				r.setMessage("No valid user found with ID - " + userId);
			}
		}
		
		
		return r;		
	}

	
	@RequestMapping(value = "/json/save", method = RequestMethod.POST)
	public @ResponseBody GenericResponse saveUser(
			@Validated @RequestBody User user, 
			BindingResult result, @AuthenticationPrincipal 
			UserDetailsExt userExt){
		
		GenericResponse r = new GenericResponse();
		
		if(result.hasErrors()){
			
			r.setSuccess(false);
			
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
		else{
			
			User userDb = dao.get(user.get_id());
			if(userDb.get_id()==user.get_id()){		

				
				/**
				 * Before saving the user, we need to make sure that the 
				 * email and username haven't changed. If it changed, then we 
				 * need to make sure that it remains unique
				 **/
				
				boolean usernameUnique = false,
						emailUnique = false,
						updateOrder = false;
				
				if(userDb.getEmail().equals(user.getEmail())){ //Email haven't changed
					emailUnique = true;
				}
				else {

					User u2 = dao.findOne("email", user.getEmail());
					if(u2==null) emailUnique = true;
				}
				
				
				if(userDb.getUsername().equals(user.getUsername())){ //Username haven't changed
					usernameUnique = true;
				}
				else {
					
					User u1 = dao.findOne("username", user.getUsername());
					if(u1==null) usernameUnique = true;					
				}
				
				if(emailUnique && usernameUnique){
					

					/* Check if the name or email has changed. If yes, update corresponding Orders */
					if(    ( userDb.getFname() != null && !userDb.getFname().equals(user.getFname()) )
						|| ( userDb.getLname() != null && !userDb.getLname().equals(user.getLname()) )
						|| ( userDb.getEmail() != null && !userDb.getEmail().equals(user.getEmail()) )
							){
						
						updateOrder = true;
					}
					
					
					/* Save User */
					dao.save(user);
					
					
					/* Update Order */
					if(updateOrder){
						
						List<Order> orders = orderDao.createQuery()
								.filter("customer._id", userDb.get_id())
								.asList();
						if(orders != null){
							for(Order order : orders){
								
								OrderCustomer oc = order.getCustomer();
								oc.setEmail(user.getEmail());
								oc.setName(user.getFname() + " " + user.getLname());
								
								orderDao.save(order);
								
								
								/* Update Log */
								try {
									
									Log log = new Log();
									log.setCollection("orders");
									log.setDetails("order.customer changed during user collection update");
									log.setDate(Calendar.getInstance().getTime());
									log.setKey(order.get_id());
									log.setUser("System");
									
									logDao.save(log);					
								}
								catch(Exception e){
									logger.error(Exceptions.giveStackTrace(e));
								}
							}
						}
					}

					
					String userDbString = "";
					try {
						/* Convert productDb to JSON */
						User tempUser = userDb;
						tempUser.setPassword("");
						ObjectMapper mapper = new ObjectMapper();
						userDbString = mapper.writeValueAsString(tempUser);
					} catch (Exception e1) {} 
					
					/**
					 * Update Log
					 * */
					try {
						
						Log log = new Log();
						log.setCollection("users");
						log.setDetails("user document updated");
						log.setPreviousDoc(userDbString);
						log.setDate(Calendar.getInstance().getTime());
						log.setKey(user.get_id());
						log.setUser(userExt.getUsername());
						
						logDao.save(log);						
					}
					catch(Exception e){
						logger.error(Exceptions.giveStackTrace(e));
					}
					
					r.setSuccess(true);
				}
				
				else {

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
			
			else {
				r.setMessage("Invalid User ID");
			}
		}
		
		
		return r;		
	}	

	
	@RequestMapping(value = "/json/getcpromos/{userId}")
	public @ResponseBody GenericResponse getCustomerPromos(@PathVariable Long userId){
		
		GenericResponse gr = new GenericResponse();
		gr.setSuccess(false);
		gr.setMessage("");		
		
		final User u = dao.createQuery()
				.field("_id").equal(userId)
				.retrievedFields(true, "email")
				.get();
		
		List<Coupon> coupons = couponDao.createQuery()
				.field("emails").equalIgnoreCase(Pattern.quote(u.getEmail()))
				.field("active").equal(true)
				.field("expiry").greaterThan(Calendar.getInstance().getTime())
				.retrievedFields(true, "_id")
				.asList();
		
		List<String> promos = new ArrayList<String>();
		if(coupons != null && !coupons.isEmpty()){
			for(Coupon c : coupons){
				promos.add(c.get_id());
			}
			
			gr.setSuccess(true);
			gr.setResults(promos);
		}
		
		return gr;		
	}
}
