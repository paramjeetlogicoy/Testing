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

import com.luvbrite.dao.ControlRecordDAO;
import com.luvbrite.dao.LogDAO;
import com.luvbrite.services.ControlConfigService;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.web.models.AttrValue;
import com.luvbrite.web.models.ControlRecord;
import com.luvbrite.web.models.GenericResponse;
import com.luvbrite.web.models.Log;
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
}
