package com.luvbrite.web.controller;

import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import com.luvbrite.dao.UserDAO;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.utils.PaginationLogic;
import com.luvbrite.web.models.Address;
import com.luvbrite.web.models.GenericResponse;
import com.luvbrite.web.models.Log;
import com.luvbrite.web.models.Order;
import com.luvbrite.web.models.ResponseWithPg;
import com.luvbrite.web.models.User;
import com.luvbrite.web.models.UserDetailsExt;


@Controller
@RequestMapping(value = "/customer")
public class CustomerController {
	
	private static Logger logger = LoggerFactory.getLogger(CustomerController.class);
	
	@Autowired
	private UserDAO dao;
	
	@Autowired
	private OrderDAO orderDao;

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	private LogDAO logDao;
	
	
	@RequestMapping(method = RequestMethod.GET)
	public String homePage(@AuthenticationPrincipal 
			UserDetailsExt user, 
			ModelMap model){
		
		if(user==null) return "redirect:login";
		if(!user.isEnabled()) return "redirect:pending-registration";
		
		model.addAttribute("userId", user.getId());
		return "customer/profile";		
	}

	@RequestMapping(value = "/json/{id}")
	public @ResponseBody User profile(@PathVariable long id){			
		return dao.get(id);		
	}	

	@RequestMapping(value = "/json/{id}/orders")
	public @ResponseBody ResponseWithPg orders(
			@PathVariable long id,
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
		
		PaginationLogic pgl = new PaginationLogic((int)orderDao.count(query, id), limit, page);
		List<Order> orders = orderDao.find(order, limit, offset, query, id);

		rpg.setSuccess(true);
		rpg.setPg(pgl.getPg());
		rpg.setRespData(orders);
		
		return rpg;	
	}
	

	@RequestMapping(value = "/{id}/saveaddr", method = RequestMethod.POST)
	public @ResponseBody GenericResponse saveAddress(
			@PathVariable long id,
			@RequestBody Address address){

		GenericResponse gr = new GenericResponse();
		gr.setSuccess(false);
		gr.setMessage("");

		if(address==null
				|| address.getFirstName() == null
				|| address.getLastName() == null){
			
			gr.setMessage("Unable to extract any valid address information");
		}
		else{
			
			User user = dao.get(id);
			if(user!=null){
			

				String mode = " added ";
				if(user.getBilling()!=null)
					mode = " changed ";
				
				user.setBilling(address);
				dao.save(user);
				
				gr.setSuccess(true);
				
				
				
				/**
				 * Update Log
				 * */
				try {
					
					Log log = new Log();
					log.setCollection("users");
					log.setDetails("Billing address" + mode + "by User.");
					log.setDate(Calendar.getInstance().getTime());
					log.setKey(user.get_id());
					log.setUser(user.getUsername());
					
					logDao.save(log);					
				}
				catch(Exception e){
					logger.error(Exceptions.giveStackTrace(e));
				}
			}
			else{
				gr.setMessage("Invalid user id. Please refresh the screen and try again");
			}
		}
		
		return gr;	
	}
	

	@RequestMapping(value = "/json/orders/{orderNumber}")
	public @ResponseBody Order orderDetails(@PathVariable long orderNumber, ModelMap model){	
		
		return orderDao.findOne("orderNumber", orderNumber);				
	}

	
	@RequestMapping(value = "/savep", method = RequestMethod.POST)
	public @ResponseBody GenericResponse savePassword(
			@AuthenticationPrincipal UserDetailsExt principal,
			@RequestBody User user){
		
		GenericResponse r = new GenericResponse();
			
		if(user.getPassword().trim().equals("")){
			r.setMessage("Password is empty");			
		}
		else{
			
			User userDb = dao.get(principal.getId());			
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
					log.setDetails("user.password changed by User.");
					log.setDate(Calendar.getInstance().getTime());
					log.setKey(user.get_id());
					log.setUser(userDb.getUsername());
					
					logDao.save(log);					
				}
				catch(Exception e){
					logger.error(Exceptions.giveStackTrace(e));
				}
				
				
				r.setSuccess(true);
			}
			
			else {
				r.setMessage("Invalid User ID");
			}
		}
		
		
		return r;		
	}
}
