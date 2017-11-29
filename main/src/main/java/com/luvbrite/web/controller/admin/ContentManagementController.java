package com.luvbrite.web.controller.admin;

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

import com.luvbrite.dao.LogDAO;
import com.luvbrite.dao.PageSliderDAO;
import com.luvbrite.services.ControlConfigService;
import com.luvbrite.services.SliderHelperFunctions;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.web.models.GenericResponse;
import com.luvbrite.web.models.Log;
import com.luvbrite.web.models.PageSlider;
import com.luvbrite.web.models.UserDetailsExt;


@Controller
@RequestMapping(value = "/admin/cms")
public class ContentManagementController {
	
	private static Logger logger = Logger.getLogger(ContentManagementController.class);
	
	@Autowired
	private PageSliderDAO dao;
	
	@Autowired
	private SliderHelperFunctions shf;
	
	@Autowired
	private LogDAO logDao;
	
	@Autowired
	private ControlConfigService ccs;
	
	
	@RequestMapping(method = RequestMethod.GET)
	public String mainPage(ModelMap model, @AuthenticationPrincipal 
			UserDetailsExt user){	
		
		String page = "404";
		
		if(user != null){
			Set<String> authorities = AuthorityUtils.authorityListToSet(user.getAuthorities());
			 if (authorities.contains("ROLE_ADMIN")) {

				 page = "admin/cms";
			 }
		}	
		
		return page;
	}
	

	@RequestMapping(value = "/slides/json/all")
	public @ResponseBody List<PageSlider> allRecords(){
		return dao.find().asList();
	}
	
	
	@RequestMapping(value = "/slides/saverecord", method = RequestMethod.POST)
	public @ResponseBody GenericResponse saveSlider(@AuthenticationPrincipal 
			UserDetailsExt user, @RequestBody PageSlider pg) {

		GenericResponse gr = new GenericResponse();
		gr.setSuccess(false);
		
		String message = "Not authorized";
		
		if(user != null){
			
			Set<String> authorities = AuthorityUtils.authorityListToSet(user.getAuthorities());
			 if (authorities.contains("ROLE_ADMIN")) {
				 
				 if(pg.getSliderName() != null && 
						 !pg.getSliderName().equals("") && 
						 pg.getTitle() != null && 
						 !pg.getTitle().equals("")){

					 dao.save(pg);

					 gr.setSuccess(true);
					 message = "";


					 /**
					  * Update Log
					  * */
					 try {

						 Log log = new Log();
						 log.setCollection("pagesliders");
						 log.setDetails("Slider document updated. Key = " + pg.get_id());
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

					 message = "Invalid slider values";
				 }
			 }
		}
		
		
		gr.setMessage(message);
		return gr;
	}
	
	
	@RequestMapping(value = "/slides/publish", method = RequestMethod.POST)
	public @ResponseBody GenericResponse publishSlider(@AuthenticationPrincipal 
			UserDetailsExt user, @RequestBody PageSlider pg) {

		GenericResponse gr = new GenericResponse();
		gr.setSuccess(false);
		
		String message = "Not authorized";
		
		if(user != null){
			
			Set<String> authorities = AuthorityUtils.authorityListToSet(user.getAuthorities());
			 if (authorities.contains("ROLE_ADMIN")) {
				 
				 if(pg.getSliderName() != null && 
						 !pg.getSliderName().equals("")){

					 message = shf.publishSlider(pg.getSliderName(), user.getUsername());
					 if(message.equals("")){
						 gr.setSuccess(true);
						 
						 
						 //reload configuration after publishing
						 ccs.reConfigControl();
					 }
				 }
				 
				 else{

					 message = "Invalid slider name";
				 }
			 }
		}
		
		gr.setMessage(message);
		return gr;
	}
	
	
	@RequestMapping(value = "/slides/changestatus", method = RequestMethod.POST)
	public @ResponseBody GenericResponse changeStatus(@AuthenticationPrincipal 
			UserDetailsExt user, @RequestBody PageSlider pg) {

		GenericResponse gr = new GenericResponse();
		gr.setSuccess(false);
		
		String message = "Not authorized";
		
		if(user != null){
			
			Set<String> authorities = AuthorityUtils.authorityListToSet(user.getAuthorities());
			 if (authorities.contains("ROLE_ADMIN")) {
				 
				 if(pg.get_id() != null){
					 PageSlider pgDb = dao.get(pg.get_id());
					 if(pgDb != null){
						 pgDb.setActive(pg.isActive());
						 
						 dao.save(pgDb);
						 gr.setSuccess(true);
						 message = "";
						 

						 /**
						  * Update Log
						  * */
						 try {

							 Log log = new Log();
							 log.setCollection("pagesliders");
							 log.setDetails("Slider status changed to active = " 
									 + pg.isActive() + " for slider id = " + pg.get_id());
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
						 message = "Not a valid slide!";
					 }
				 }
				 
				 else{

					 message = "Invalid slider name";
				 }
			 }
		}
		
		gr.setMessage(message);
		return gr;
	}
}
