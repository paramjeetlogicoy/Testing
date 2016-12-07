package com.luvbrite.web.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.mongodb.morphia.aggregation.AggregationPipeline;
import org.mongodb.morphia.aggregation.Sort;
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
import com.luvbrite.dao.ProductDAO;
import com.luvbrite.dao.ReviewDAO;
import com.luvbrite.dao.UserDAO;
import com.luvbrite.services.EmailService;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.utils.PaginationLogic;
import com.luvbrite.web.models.Address;
import com.luvbrite.web.models.Email;
import com.luvbrite.web.models.GenericResponse;
import com.luvbrite.web.models.Log;
import com.luvbrite.web.models.Order;
import com.luvbrite.web.models.Product;
import com.luvbrite.web.models.ResponseWithPg;
import com.luvbrite.web.models.Review;
import com.luvbrite.web.models.User;
import com.luvbrite.web.models.UserDetailsExt;
import com.luvbrite.web.models.pipelines.ProductId;

import static org.mongodb.morphia.aggregation.Projection.projection;

@Controller
@RequestMapping(value = "/customer")
public class CustomerController {
	
	private static Logger logger = Logger.getLogger(CustomerController.class);
	
	@Autowired
	private UserDAO dao;
	
	@Autowired
	private OrderDAO orderDao;

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	private ReviewDAO reviewDao;

	@Autowired
	private ProductDAO productDao;

	@Autowired
	private LogDAO logDao;

	@Autowired
	private EmailService emailService;
	
	
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
	

	@RequestMapping(value = "/json/purchaselist")
	public @ResponseBody ResponseWithPg purchaseListForReview(@AuthenticationPrincipal 
			UserDetailsExt user,
			@RequestParam(value="p", required=false) Integer page){
		
		ResponseWithPg rpg = new ResponseWithPg();
		rpg.setSuccess(false);
		
		if(user==null){
			
			rpg.setMessage("login");
			return rpg;
		}
		
		int offset = 0,
				limit = 15; //itemsPerPage

		if(page==null) page = 1;
		if(page >1) offset = (page-1)*limit;
				
		AggregationPipeline pipeline = orderDao.getDatastore().createAggregation(Order.class)
		
				//Pull orders for this customer
				.match(orderDao.createQuery().filter("customer._id", user.getId()))
				
				.sort(new Sort("orderNumber", -1))
				
				.unwind("lineItems")
				
				.project(projection("_id").suppress(),
						projection("orderNumber","orderNumber"),
						projection("productId","lineItems.pid"))
				
				.group("productId");
		
		
		Iterator<ProductId> iterator = pipeline.aggregate(ProductId.class);
		List<Long> productIds = new ArrayList<Long>();
		while(iterator.hasNext()) {
			productIds.add(iterator.next().getProductId());
		}
		
		if(!productIds.isEmpty()){
			
			//get the products already reviewed
			List<Long> reviewedProductIds = new ArrayList<Long>();	
			List<Review> reviewedProducts = reviewDao.createQuery()
						.field("authorId").equal(user.getId())
						.retrievedFields(true, "productId")
						.asList();
			if(reviewedProducts != null){
				for(Review r : reviewedProducts){
					reviewedProductIds.add(r.getProductId());
				}
				
				if(!reviewedProductIds.isEmpty()){
					//remove reviewedProductIds from productIds
					productIds.removeAll(reviewedProductIds);
				}
			}
			
			PaginationLogic pgl = new PaginationLogic(
					(int)productDao.createQuery().field("_id").in(productIds).countAll(), 
					limit, 
					page);
			
			List<Product> products = productDao.createQuery()
					.field("_id").in(productIds)
					.retrievedFields(true, "name", "featuredImg", "url")
					.offset(offset)
					.limit(limit)
					.asList();
			

			rpg.setSuccess(true);
			rpg.setPg(pgl.getPg());
			rpg.setRespData(products);
		}
		
		return rpg;				
	}

	
	@RequestMapping(value = "/json/myreviewslist")
	public @ResponseBody ResponseWithPg myReviewList(@AuthenticationPrincipal 
			UserDetailsExt user,
			@RequestParam(value="p", required=false) Integer page){
		
		ResponseWithPg rpg = new ResponseWithPg();
		rpg.setSuccess(false);
		
		if(user==null){
			
			rpg.setMessage("login");
			return rpg;
		}
		
		int offset = 0,
				limit = 15; //itemsPerPage

		if(page==null) page = 1;
		if(page >1) offset = (page-1)*limit;
		
		PaginationLogic pgl = new PaginationLogic(
				(int) reviewDao.createQuery().field("authorId").equal(user.getId()).countAll(), 
					limit, 
					page);
		
		List<Review> reviews =  reviewDao.createQuery()
				.field("authorId").equal(user.getId())
				.offset(offset)
				.limit(limit)
				.asList();
		

		rpg.setSuccess(true);
		rpg.setPg(pgl.getPg());
		rpg.setRespData(reviews);
		
		return rpg;				
	}

	
	@RequestMapping(value = "/save-review", method = RequestMethod.POST)
	public @ResponseBody GenericResponse saveReview(
			@AuthenticationPrincipal UserDetailsExt principal,
			@RequestBody Review review){
		
		GenericResponse r = new GenericResponse();
		r.setSuccess(false);
		
		if(principal != null){
			
			if(review !=null && review.getProductId() != 0){				
				if(review.getTitle().equals("")){
					r.setMessage("Please provide a title for your review");
					return r;
				}
				
				
				Calendar now = Calendar.getInstance();
				
				//Set the other fields for review 
				review.setApprovalStatus("new");
				review.setAuthor(principal.getUsername());
				review.setAuthorId(principal.getId());
				review.setCreated(now.getTime());
				
				reviewDao.save(review);
				
				r.setSuccess(true);
				
				
				/**
				 * Update Log
				 * */
				try {
					
					Log log = new Log();
					log.setCollection("reviews");
					log.setDetails("New review created for productId - " + review.getProductId() );
					log.setDate(Calendar.getInstance().getTime());
					log.setKey(review.getProductId());
					log.setUser(principal.getUsername());
					
					logDao.save(log);					
				}
				catch(Exception e){
					logger.error(Exceptions.giveStackTrace(e));
				}
				

				

				
				/* Send email if needed */
				try {
					
					Email email = new Email();
					email.setEmailTemplate("new-review");
					email.setFromName("Luvbrite Notifications");
					email.setFromEmail("no-reply@luvbrite.com");
					email.setRecipientEmail("product_reviews@luvbrite.com");
					email.setRecipientName("CSR");					
					email.setEmailTitle("New product review notification");
					email.setSubject("New product review for  " + review.getProductName());
					email.setEmailInfo("new product review.");						
					
					emailService.sendEmail(email);
					
				}catch(Exception e){
					logger.error(Exceptions.giveStackTrace(e));
				}
			}
			
			else{
				
				r.setMessage("Invalid review parameters. Please try again after refreshing the page");
			}
		}
		
		else{
			
			r.setMessage("Seems like you have logged out of your account. Please login back to submit review.");
		}
		
		return r;
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
