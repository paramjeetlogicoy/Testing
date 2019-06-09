package com.luvbrite.web.controller.admin;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import com.luvbrite.dao.LogDAO;
import com.luvbrite.dao.PriceDAO;
import com.luvbrite.dao.ProductDAO;
import com.luvbrite.dao.ReviewDAO;
import com.luvbrite.services.AvailableProducts;
import com.luvbrite.services.SynchronizeCartItems;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.utils.PaginationLogic;
import com.luvbrite.web.models.GenericResponse;
import com.luvbrite.web.models.Log;
import com.luvbrite.web.models.Price;
import com.luvbrite.web.models.Product;
import com.luvbrite.web.models.ResponseWithPg;
import com.luvbrite.web.models.Review;
import com.luvbrite.web.models.UserDetailsExt;
import com.luvbrite.web.validator.ProductDetailsFormValidator;


@Controller
@RequestMapping(value = {"/admin/products","/admin/product"})
public class ProductsController {
	
	private static Logger logger = Logger.getLogger(ProductsController.class);
	

	@Autowired
	private ProductDAO prdDao;
	
	
	@Autowired
	private LogDAO logDao;
	

	@Autowired
	private ProductDetailsFormValidator pdFormValidator;
	

	@Autowired
	private PriceDAO priceDao;
	
	
	@Autowired
	private ReviewDAO reviewDao;
	
	
	@Autowired
	private SynchronizeCartItems syncCart;
	
	
	@InitBinder("product")
	protected void InitBinder(WebDataBinder binder){
		binder.setValidator(pdFormValidator);
	}
	
	
	@RequestMapping(method = RequestMethod.GET)
	public String mainPage(ModelMap model){	
		return "admin/products";		
	}
	
	
	@RequestMapping(value = "/json/", method = RequestMethod.GET)
	public @ResponseBody ResponseWithPg products(
			@RequestParam(value="p", required=false) Integer page,
			@RequestParam(value="q", required=false) String query,
			@RequestParam(value="o", required=false) String order){
		
		ResponseWithPg rpg = new ResponseWithPg();
		if(query==null) query = "";
		if(order==null) order = "-_id";
		
		int offset = 0,
			limit = 15; //itemsPerPage
		
		if(page==null) page = 1;
		if(page >1) offset = (page-1)*limit;
		
		PaginationLogic pgl = new PaginationLogic((int)prdDao.count(query), limit, page);
			
		List<Product> products = prdDao.find(order, limit, offset, query);
		
//		List<Product> prodFromInv 	=	new AvailableProducts().getAllAvailProdsFromInv(products);
//		
//		/**Set Stock Status to 'outOfStock' For the products whose remaining quantity =0**/
//		for(int i = 0 ; i <prodFromInv.size();i++) {
//			Product prod=prodFromInv.get(i);
//			if(prod.isFromInv() && prod.getTotal_remain_qty()==0) {
//				prod.setStockStat("outofstock");
//			}
//		}
		
		rpg.setSuccess(true);
		rpg .setPg(pgl.getPg());
		rpg.setRespData(products);
		//rpg.setRespData(prodFromInv);
		
		return rpg;		
	}
	

	@RequestMapping(value = "/json/all")
	public @ResponseBody List<Product> allProducts(@RequestParam(value="q", required=false) String query){	
	
		if(query==null) query = "";
		return prdDao.findAll(query);
	}
	

	@RequestMapping(value = "/json/{productId}")
	public @ResponseBody Product productDetails(@PathVariable long productId){	
			return prdDao.get(productId);
	}

	
	@RequestMapping(value = "/json/list-reviews")
	public @ResponseBody ResponseWithPg listReviews(
			@RequestParam(value="p", required=false) Integer page,
			@RequestParam(value="s", required=false) String reviewStatus){
	
		ResponseWithPg rpg = new ResponseWithPg();
		rpg.setSuccess(false);
		
		int offset = 0,
				limit = 8; //itemsPerPage

		if(page==null) page = 1;
		if(page >1) offset = (page-1)*limit;
		
		Query<Review> query = reviewDao.createQuery();		
		if(reviewStatus != null && !reviewStatus.equals("")){
			query.field("approvalStatus").equal(reviewStatus);
		}
		
		PaginationLogic pgl = new PaginationLogic(
				(int) reviewDao.count(query), 
					limit, 
					page);
		
		List<Review> reviews =  query
				.offset(offset)
				.limit(limit)
				.order("-created")
				.asList();
		

		rpg.setSuccess(true);
		rpg.setPg(pgl.getPg());
		rpg.setRespData(reviews);
		
		return rpg;				
	}

	
	@RequestMapping(value = "/json/savepdtls", method = RequestMethod.POST)
	public @ResponseBody GenericResponse saveProductDetails(
			@Validated @RequestBody Product product, 
			BindingResult result, @AuthenticationPrincipal 
			UserDetailsExt user){
		
		GenericResponse r = new GenericResponse();
		
		try {
			
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
				
				Product productDb = prdDao.get(product.get_id());
				if(productDb != null){
					
					/**
					 * Before saving the product, we need to make sure that the 
					 * URL haven't changed. If it changed, then we 
					 * need to make sure that it remains unique
					 **/
					
					boolean urlUnique = false;
					String otherProductName = "";
					if(product.getUrl() != null && 
							productDb.getUrl() != null && 
							productDb.getUrl().equals(product.getUrl())){
						urlUnique = true;
					}
					else{
						Product p2 = prdDao.findOne("url", product.getUrl());
						if(p2 == null) urlUnique = true;
						else otherProductName = p2.getName();
					}
					
					if(!urlUnique){
						r.setMessage("Product URL is not unique. Product - " + otherProductName + " - has same URL.");
						return r;
					}
					
					String productDbString = "";
					try {
						/* Convert productDb to JSON */
						ObjectMapper mapper = new ObjectMapper();
						productDbString = mapper.writeValueAsString(productDb);
					} catch (Exception e1) {} 
					
					productDb.setName(product.getName());
					productDb.setDescription(product.getDescription());
					productDb.setUrl(product.getUrl());
					productDb.setStatus(product.getStatus());
					productDb.setStockStat(product.getStockStat());
					productDb.setCategories(product.getCategories());
					productDb.setVariation(product.isVariation());
					productDb.setFeaturedImg(product.getFeaturedImg());
					productDb.setRps(product.getRps());
					productDb.setProductFilters(product.getProductFilters());
					productDb.setPrdVisuals(product.getPrdVisuals());
					productDb.setNewBatchArrival(product.getNewBatchArrival());
					
					if(product.isVariation()){
						productDb.setAttrs(product.getAttrs());
						productDb.setDefaultAttr(product.getDefaultAttr());
						productDb.setPriceRange(product.getPriceRange());
					}
					else{
						productDb.setPrice(product.getPrice());
						productDb.setSalePrice(product.getSalePrice());
						productDb.setStock(product.getStock());
						productDb.setPriceRange(product.getPriceRange());
					}
					
                                        productDb.setMinStockLimit(product.getMinStockLimit());
					
					prdDao.save(productDb);
					
					
					
					/* Update Log */
					try {
						
						Log log = new Log();
						log.setCollection("products");
						log.setDetails("product document updated.");
						log.setDate(Calendar.getInstance().getTime());
						log.setPreviousDoc(productDbString);
						log.setKey(product.get_id());
						log.setUser(user.getUsername());
						
						logDao.save(log);					
					}
					catch(Exception e){
						logger.error(Exceptions.giveStackTrace(e));
					}
					
					
					
					
					/* Sync CartItems */
					try {
						syncCart.sync(productDb);				
					}
					catch(Exception e){
						logger.error(Exceptions.giveStackTrace(e));
					}
					
					
					
					
					r.setSuccess(true);				
				}
				
				else {
					r.setMessage("Invalid Product ID");
				}
			}
			
		}catch(Exception e){
			logger.error(Exceptions.giveStackTrace(e));
			r.setSuccess(false);
			r.setMessage("There was some error updating the product, please contact G.");
		}
		
		
		return r;		
	}

	@RequestMapping(value = "/json/createproduct", method = RequestMethod.POST)
	public @ResponseBody Product createProduct(
			@RequestBody Product product, 
			BindingResult result, @AuthenticationPrincipal 
			UserDetailsExt user){
		
		//Generate productId
		long productId = prdDao.getNextSeq();
		if(productId != 0l){
			
			Date dateCreated = Calendar.getInstance().getTime();
			
			product.set_id(productId);
			product.setDateCreated(dateCreated);
			product.setNewBatchArrival(dateCreated);
			
			
			/**
			 * Update Log
			 * */
			try {
				
				Log log = new Log();
				log.setCollection("products");
				log.setDetails("product created");
				log.setDate(dateCreated);
				log.setKey(productId);
				log.setUser(user.getUsername());
				
				logDao.save(log);					
			}
			catch(Exception e){
				logger.error(Exceptions.giveStackTrace(e));
			}
			
			
			prdDao.save(product);
		}
		
		else{
			product.set_id(0l);
			product.setDescription("Error generating new product ID.");
		}
		
		return product;		
	}

	@RequestMapping(value = "/json/duplicateproduct/{productId}", method = RequestMethod.POST)
	public @ResponseBody GenericResponse duplicateProduct(
			@PathVariable long productId, 
			@AuthenticationPrincipal UserDetailsExt user){
		
		GenericResponse gr = new GenericResponse();
		gr.setSuccess(false);
		
		if(productId != 0l){
			
			Product existingProduct = prdDao.get(productId);
			if(existingProduct != null){
				
				//Generate new productId
				long newProductId = prdDao.getNextSeq();
				existingProduct.set_id(newProductId);
				existingProduct.setName("Copy of " + existingProduct.getName());
				
				String productUrl = existingProduct.getName().toLowerCase()
						.replaceAll("[^a-z0-9 ]", "").replaceAll("\\s+", "-");
				existingProduct.setUrl(productUrl);
				
				prdDao.save(existingProduct);
				/**
				 * Update Log
				 * */
				try {
					
					Log log = new Log();
					log.setCollection("products");
					log.setDetails("New product created. Duplicated from " + productId);
					log.setDate(Calendar.getInstance().getTime());
					log.setKey(newProductId);
					log.setUser(user.getUsername());
					
					logDao.save(log);					
				}
				catch(Exception e){
					logger.error(Exceptions.giveStackTrace(e));
				}
				
				gr.setSuccess(true);
				gr.setMessage(newProductId+"");
			}
			
			else{
				
				gr.setMessage("No product found for id " + productId);
			}
		}
		
		else{
			
			gr.setMessage("Invalid product Id " + productId);
		}
		
		return gr;		
	}

	@RequestMapping(value = "/json/{productId}/price")
	public @ResponseBody List<Price> price(@PathVariable long productId){	
			
		return priceDao.findPriceByProduct(productId);		
	}

	@RequestMapping(value = "/json/savepricedtls", method = RequestMethod.POST)
	public @ResponseBody GenericResponse savePriceDetails(
			@RequestBody List<Price> prices, @AuthenticationPrincipal 
			UserDetailsExt user){
		
		GenericResponse r = new GenericResponse();
		StringBuilder errMsg = new StringBuilder();
		
		//Validation		
		if(prices.size() == 0){
			
		}
		else{
			
			for(Price price: prices){
				if(price.getRegPrice()<0){
					errMsg
					 .append(" - Regular price is empty<br />");
				}
				
				if(price.getProductId()==0){
					errMsg
					 .append(" - Invalid Product ID<br />");
				}
				
				if(price.getVariation().size()==0){
					errMsg
					 .append(" - Invalid Variation<br />");
				}
				
			}			
		}
		
		if(errMsg.length()>0){
			
			
			r.setSuccess(false);			
			r.setMessage(errMsg.toString());
		
		}
		else{
			
			for(Price price: prices){
				
				if(price.get_id()==0){
					price.set_id(priceDao.getNextSeq());
				}				
			}
			
			
			//Delete existing prices by productId
			priceDao.deleteByQuery(priceDao.createQuery()
					.field("pid").equal(prices.get(0).getProductId()));
			

			
			//Add new prices 
			for(Price p : prices){
				priceDao.save(p);
			}	
			
			
			/**
			 * Sync corrects price changes, stock status changes and any priceline deletions 
			 * */
			try {
				syncCart.sync(prices);				
			}
			catch(Exception e){
				logger.error(Exceptions.giveStackTrace(e));
			}
			
			
			
			/**
			 * Update Log
			 * */
			try {
				
				Log log = new Log();
				log.setCollection("prices");
				log.setDetails("prices changed for product Id " + prices.get(0).getProductId());
				log.setDate(Calendar.getInstance().getTime());
				log.setKey(prices.get(0).getProductId());
				log.setUser(user.getUsername());
				
				logDao.save(log);					
			}
			catch(Exception e){
				logger.error(Exceptions.giveStackTrace(e));
			}
			
			r.setSuccess(true);
		}
		
		
		return r;		
	}

	
	@RequestMapping(value = "/update-review", method = RequestMethod.POST)
	public @ResponseBody GenericResponse updateReview(
			@RequestBody Review review, @AuthenticationPrincipal 
			UserDetailsExt user){
		
		GenericResponse r = new GenericResponse();
		r.setSuccess(false);
		
		//Validation		
		if(review != null && review.get_id() != null){
			
			Review reviewDb = reviewDao.get(review.get_id());
			if(reviewDb != null){
				String prevStat = reviewDb.getApprovalStatus();
				
				reviewDb.setApprovalStatus(review.getApprovalStatus());
				reviewDao.save(reviewDb);
				
				
				//Update product with latest rating if there is a new approved review, or 
				//existing approved review has been updated!
				if(review.getApprovalStatus().equals("approved") || 
						prevStat.equals("approved")){
					try{
						updateProductOverallRating(reviewDb.getProductId());
					}
					catch(Exception e){
						logger.error(Exceptions.giveStackTrace(e));
					}
				}
				
				/**
				 * Update Log
				 * */
				try {
					
					Log log = new Log();
					log.setCollection("reviews");
					log.setDetails("Approval status update for productId - " + 
								review.getProductId() +  
								". Previous value " + prevStat);
					log.setDate(Calendar.getInstance().getTime());
					log.setKey(reviewDb.getProductId());
					log.setUser(user.getUsername());
					
					logDao.save(log);					
				}
				catch(Exception e){
					logger.error(Exceptions.giveStackTrace(e));
				}
				
				r.setSuccess(true);
				
			}
			else {
				r.setMessage("No corresponding review found in Database.");
			}
			
		}
		else{
			r.setMessage("Invalid update parameters.");
		}
		
		
		return r;		
	}
	
	private String updateProductOverallRating(long productId){
		
		String response = "";
		
		double sumRating = 0d;
		int reviewCount = 0;
		
		List<Review> reviews =  reviewDao.createQuery()
				.field("approvalStatus").equal("approved")
				.field("productId").equal(productId)
				.retrievedFields(true, "rating")
				.asList();
		
		if(reviews != null){
			for(Review review : reviews){
				reviewCount++;
				
				sumRating+= review.getRating();
			}
			
			//Find average rating
			int newRating = (int) Math.round(sumRating/reviewCount);
			
			Product p = prdDao.get(productId);
			int prevRating = p.getRating();
			int prevReviewCount = p.getReviewCount();
			
			p.setRating(newRating);
			p.setReviewCount(reviewCount);
			prdDao.save(p);

			
			/**
			 * Update Log
			 * */
			try {
				
				Log log = new Log();
				log.setCollection("products");
				log.setDetails("Review updated. Previous values " + prevRating + " star, " + prevReviewCount + " reviews.");
				log.setDate(Calendar.getInstance().getTime());
				log.setKey(p.get_id());
				log.setUser("system");
				
				logDao.save(log);					
			}
			catch(Exception e){
				logger.error(Exceptions.giveStackTrace(e));
			}
			
			response = "success";			
		}
		
		else {
			response = "no reviews found for this product";
		}
		
		return response;		
	}
}
