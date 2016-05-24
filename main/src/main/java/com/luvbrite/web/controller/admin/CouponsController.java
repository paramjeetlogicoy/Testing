package com.luvbrite.web.controller.admin;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
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

import com.luvbrite.dao.CouponDAO;
import com.luvbrite.utils.CouponGen;
import com.luvbrite.utils.PaginationLogic;
import com.luvbrite.web.models.Coupon;
import com.luvbrite.web.models.GenericResponse;
import com.luvbrite.web.models.ResponseWithPg;
import com.luvbrite.web.validator.CouponValidator;


@Controller
@RequestMapping(value = {"/admin/coupons","/admin/coupon"})
public class CouponsController {

	@Autowired
	private CouponDAO dao;
	
	
	@Autowired
	private CouponValidator validator;
	
	
	@InitBinder
	protected void InitBinder(WebDataBinder binder){
		binder.setValidator(validator);
	}
	
	
	@RequestMapping(method = RequestMethod.GET)
	public String mainPage(ModelMap model){		
		return "admin/coupons";		
	}
	
	
	@RequestMapping(value = "/json/", method = RequestMethod.GET)
	public @ResponseBody ResponseWithPg orders(
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
		
		PaginationLogic pgl = new PaginationLogic((int)dao.count(query), limit, page);
		List<Coupon> coupons = dao.find(order, limit, offset, query);

		rpg.setSuccess(true);
		rpg.setPg(pgl.getPg());
		rpg.setRespData(coupons);
		
		return rpg;	
	}	
	
	
	@RequestMapping(value = "/json/check/", method = RequestMethod.GET)
	public @ResponseBody Coupon checkIfExists(@RequestParam(value="c", required=true) String coupon){
		return dao.get(coupon);
	}
	
	
	@RequestMapping(value = "/json/save", method = RequestMethod.POST)
	public @ResponseBody GenericResponse saveCoupon(
			@Validated @RequestBody Coupon coupon, 
			BindingResult result){
		
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
		
		} else {
			
			r.setSuccess(true);
			
			coupon.set_id(coupon.get_id().toLowerCase());
			dao.save(coupon);
		}
		
		return r;
	}
	
	
	@RequestMapping(value = "/json/bulk/{count}", method = RequestMethod.POST)
	public @ResponseBody GenericResponse bulkCoupon(
			@PathVariable int count,
			@Validated @RequestBody Coupon coupon, 
			BindingResult result){
		
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
		
		} else {
			
			if(count>50 || count<1){
				
				r.setMessage("Count cannot be less than 1 or more than 50");
				
			}
			else {
				
				//Generate multiple coupon
				boolean createCoupon = true;
				String couponPrefix = coupon.get_id();
				
				int couponCounter = 0,
						safetyCounter = 0;
				
				List<String> coupons = new ArrayList<String>();
				
				while(createCoupon){
					String newCouponCode = (couponPrefix + CouponGen.getNewCoupon(7)).toLowerCase();
					
					coupon.set_id(newCouponCode);
					try {
						
						//System.out.println("Coupon Code - " + newCouponCode);
						
						dao.save(coupon);
						
						coupons.add(newCouponCode);
						couponCounter++;
						
					}catch (Exception e){}
					
					safetyCounter++;
					
					if(safetyCounter > 200 || couponCounter >= count) createCoupon = false; 
						
				}
				
				r.setSuccess(true);		
				r.setMessage(StringUtils.collectionToCommaDelimitedString(coupons));
			}
		}
		
		return r;
	}
}
