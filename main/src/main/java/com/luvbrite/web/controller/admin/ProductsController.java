package com.luvbrite.web.controller.admin;

import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
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

import com.luvbrite.dao.LogDAO;
import com.luvbrite.dao.PriceDAO;
import com.luvbrite.dao.ProductDAO;
import com.luvbrite.services.SynchronizeCartItems;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.utils.PaginationLogic;
import com.luvbrite.web.models.GenericResponse;
import com.luvbrite.web.models.Log;
import com.luvbrite.web.models.Price;
import com.luvbrite.web.models.Product;
import com.luvbrite.web.models.ResponseWithPg;
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
		
		rpg.setSuccess(true);
		rpg .setPg(pgl.getPg());
		rpg.setRespData(products);
		
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

	
	@RequestMapping(value = "/json/savepdtls", method = RequestMethod.POST)
	public @ResponseBody GenericResponse saveProductDetails(
			@Validated @RequestBody Product product, 
			BindingResult result, @AuthenticationPrincipal 
			UserDetailsExt user){
		
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
			
			Product productDb = prdDao.get(product.get_id());
			if(productDb.get_id()==product.get_id()){
				
				productDb.setName(product.getName());
				productDb.setDescription(product.getDescription());
				productDb.setUrl(product.getUrl());
				productDb.setStatus(product.getStatus());
				productDb.setStockStat(product.getStockStat());
				productDb.setCategories(product.getCategories());
				productDb.setVariation(product.isVariation());
				productDb.setFeaturedImg(product.getFeaturedImg());
				productDb.setRps(product.getRps());
				
				if(product.isVariation()){
					productDb.setAttrs(product.getAttrs());
					productDb.setDefaultAttr(product.getDefaultAttr());
					productDb.setPriceRange(product.getPriceRange());
				}
				else{
					productDb.setPrice(product.getPrice());
					productDb.setSalePrice(product.getSalePrice());
					productDb.setStock(product.getStock());
				}
				
				
				prdDao.save(productDb);
				
				
				/* Update Log */
				try {
					
					Log log = new Log();
					log.setCollection("products");
					log.setDetails("product document updated");
					log.setDate(Calendar.getInstance().getTime());
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
			product.set_id(productId);
			
			
			/**
			 * Update Log
			 * */
			try {
				
				Log log = new Log();
				log.setCollection("products");
				log.setDetails("product created");
				log.setDate(Calendar.getInstance().getTime());
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
}
