package com.luvbrite.web.controller.admin;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.luvbrite.dao.ControlRecordDAO;
import com.luvbrite.dao.LogDAO;
import com.luvbrite.dao.PriceDAO;
import com.luvbrite.dao.ProductDAO;
import com.luvbrite.services.ControlConfigService;
import com.luvbrite.services.SynchronizeCartItems;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.utils.Utility;
import com.luvbrite.web.models.AttrValue;
import com.luvbrite.web.models.ControlRecord;
import com.luvbrite.web.models.GenericResponse;
import com.luvbrite.web.models.Log;
import com.luvbrite.web.models.Price;
import com.luvbrite.web.models.Product;
import com.luvbrite.web.models.UserDetailsExt;


@Controller
@RequestMapping(value = "/admin/ctrls")
public class ControlRecordController {
	
	private static Logger logger = Logger.getLogger(ControlRecordController.class);
	
	@Autowired
	private ControlRecordDAO dao;
	
	
	@Autowired
	private ControlConfigService ccs;
	
	
	@Autowired
	private LogDAO logDao;
	

	@Autowired
	private ProductDAO prdDao;
	
	
	@Autowired
	private PriceDAO priceDao;
	
	
	@Autowired
	private SynchronizeCartItems syncCart;
	
	
	private NumberFormat nf = NumberFormat.getCurrencyInstance();
	
	@RequestMapping(method = RequestMethod.GET)
	public String mainPage(ModelMap model, @AuthenticationPrincipal 
			UserDetailsExt user){	
		
		String page = "404";
		
		if(user != null){
			Set<String> authorities = AuthorityUtils.authorityListToSet(user.getAuthorities());
			 if (authorities.contains("ROLE_ADMIN")) {

				 page = "admin/control-records";
			 }
		}	
		
		return page;
	}
	

	@RequestMapping(value = "/json/all")
	public @ResponseBody List<ControlRecord> allRecords(){
		return dao.find().asList();
	}
	
	
	@RequestMapping(value = "/saverecord", method = RequestMethod.POST)
	public @ResponseBody GenericResponse reloadConfig(@AuthenticationPrincipal 
			UserDetailsExt user, @RequestBody ControlRecord cr) {

		GenericResponse gr = new GenericResponse();
		gr.setSuccess(false);
		
		String message = "Not authorized";
		
		if(user != null){
			Set<String> authorities = AuthorityUtils.authorityListToSet(user.getAuthorities());
			 if (authorities.contains("ROLE_ADMIN")) {
				 
				 String controlRecordId = cr.get_id();
				 List<AttrValue> params = cr.getParams();
				 
				 if(controlRecordId != null && 
						 !controlRecordId.equals("") && 
						 params != null && 
						 !params.isEmpty()){


					 dao.save(cr);

					 gr.setSuccess(true);
					 message = "";




					 /**
					  * Update Log
					  * */
					 try {

						 Log log = new Log();
						 log.setCollection("controlrecords");
						 log.setDetails("Control record document updated. Key = " + controlRecordId);
						 log.setDate(Calendar.getInstance().getTime());
						 log.setKey(0);
						 log.setUser(user.getUsername());

						 logDao.save(log);					
					 }
					 catch(Exception e){
						 logger.error(Exceptions.giveStackTrace(e));
					 }
				 }
				 
				 else{

					 message = "Invalid controlRecord values";
				 }
			 }
		}
		
		
		gr.setMessage(message);
		return gr;
	}
	
	
	@RequestMapping(value = "/reloadconfig", method = RequestMethod.POST)
	public @ResponseBody GenericResponse reloadConfig(@AuthenticationPrincipal 
			UserDetailsExt user) {

		GenericResponse gr = new GenericResponse();
		gr.setSuccess(false);
		
		String message = "Not authorized";
		
		if(user != null){
			Set<String> authorities = AuthorityUtils.authorityListToSet(user.getAuthorities());
			 if (authorities.contains("ROLE_ADMIN")) {
				 
				 ccs.reConfigControl();

				 gr.setSuccess(true);
				 message = "";




				 /**
				  * Update Log
				  * */
				 try {

					 Log log = new Log();
					 log.setCollection("controlrecords");
					 log.setDetails("Control Config Reloaded");
					 log.setDate(Calendar.getInstance().getTime());
					 log.setUser(user.getUsername());

					 logDao.save(log);					
				 }
				 catch(Exception e){
					 logger.error(Exceptions.giveStackTrace(e));
				 }
			 }
		}
		
		
		gr.setMessage(message);
		return gr;
	}
	
	
	@RequestMapping(value = "/discountcontrol", method = RequestMethod.POST)
	public @ResponseBody GenericResponse discountControl(@AuthenticationPrincipal 
			UserDetailsExt user, @RequestBody AttrValue av) {

		GenericResponse gr = new GenericResponse();
		gr.setSuccess(false);
		
		String message = "Not authorized";
		
		if(user != null){
			Set<String> authorities = AuthorityUtils.authorityListToSet(user.getAuthorities());
			 if (authorities.contains("ROLE_ADMIN")) {
				 
				 if(av != null){

					 double discount = Utility.getDouble(av.getAttr());
					 String productIds = av.getValue();
					 
					 if(discount >= 0 && productIds.length()>1){
						 
						 String[] productIdA = productIds.split(",");
						 List<Integer> productIdL = new ArrayList<Integer>();
						 for(int i=0; i<productIdA.length; i++){
							 productIdL.add(Utility.getInteger(productIdA[i]));
						 }
						 
						 List<Product> products = prdDao.createQuery().field("_id").in(productIdL).asList();
						 if(products != null && !products.isEmpty()){
							 
							 int varitaionPrdsCount = 0,
									 simplePrdsCount = 0;
							 
							 for(Product product : products){
								 if(product.isVariation()){
									 
									 double lowerPrice = 999999d, 
											 higherPrice = -999999d;
									 
									 List<Price> prices = priceDao.createQuery().field("pid").equal(product.get_id()).asList();
									 for(Price price : prices){
										 
										 if(discount == 0){
											 price.setSalePrice(0d);
											 
											 if(price.getRegPrice() >= higherPrice) higherPrice = price.getRegPrice();
											 if(price.getRegPrice() <= lowerPrice) 	lowerPrice = price.getRegPrice();
										 }
										 
										 else{
											 
											 double discountPrice = price.getRegPrice() * (1 - discount/ 100);
											 price.setSalePrice(discountPrice);
											 
											 if(discountPrice >= higherPrice) 	higherPrice = discountPrice;
											 if(discountPrice <= lowerPrice) 	lowerPrice = discountPrice;
										 }
										 
										 varitaionPrdsCount++;
										 
										 priceDao.save(price);


										 /* Sync CartItems */
										 try {
											 syncCart.sync(price);				
										 }
										 catch(Exception e){
											 logger.error(Exceptions.giveStackTrace(e));
										 }
									 }
									 
									 
									 //Update the price Range									 
									 if(lowerPrice == higherPrice){
										 product.setPriceRange(nf.format(lowerPrice));
										 prdDao.save(product);
									 }
									 
									 else if(lowerPrice != 999999d && higherPrice != -999999d){
										 product.setPriceRange(nf.format(lowerPrice) + " - " + nf.format(higherPrice));		
										 prdDao.save(product);								 
									 }
									 
								 }
								 else{
									 
									 if(discount == 0){
										 product.setSalePrice(0d);
										 product.setPriceRange(nf.format(product.getPrice()));
									 }
									 
									 else{
										 
										 double discountPrice = product.getPrice() * (1 - discount/ 100);
										 product.setSalePrice(discountPrice);
										 product.setPriceRange(nf.format(discountPrice));
									 }
									 
									 simplePrdsCount++;
									 
									 prdDao.save(product);


									 /* Sync CartItems */
									 try {
										 syncCart.sync(product);				
									 }
									 catch(Exception e){
										 logger.error(Exceptions.giveStackTrace(e));
									 }
								 }
							 }

							 
							 gr.setSuccess(true);
							 message = varitaionPrdsCount + " Varations update. " +  simplePrdsCount + " simple products updated";
							 
							 
							 
							 /**
							  * Update Log
							  * */
							 try {

								 Log log = new Log();
								 log.setCollection("controlrecords");
								 log.setDetails("Discount controlled by value " + discount 
										 + ". For products with ID " + productIds);
								 log.setDate(Calendar.getInstance().getTime());
								 log.setUser(user.getUsername());

								 logDao.save(log);					
							 }
							 catch(Exception e){
								 logger.error(Exceptions.giveStackTrace(e));
							 }
						 }
						 else{
							 message = "No products found for product Ids " + productIds;
						 }
					 }
					 
					 else{
						 message = "Invalid discount and products";
					 }
				 }
				 
				 else{
					 message = "Invalid discount and products";
				 }
			 }
		}
		
		
		gr.setMessage(message);
		return gr;
	}
}
