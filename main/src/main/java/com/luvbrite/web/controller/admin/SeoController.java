package com.luvbrite.web.controller.admin;

import java.util.Calendar;
import java.util.List;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luvbrite.dao.LogDAO;
import com.luvbrite.dao.SeoDAO;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.web.models.GenericResponse;
import com.luvbrite.web.models.Log;
import com.luvbrite.web.models.Seo;
import com.luvbrite.web.models.UserDetailsExt;


@Controller
@RequestMapping(value = "/admin/seo")
public class SeoController {
	
	private static Logger logger = Logger.getLogger(SeoController.class);
		
	@Autowired
	private LogDAO logDao;
	
	@Autowired
	private SeoDAO seoDao;
	
	
	@RequestMapping(method = RequestMethod.GET)
	public String mainPage(ModelMap model){		
		return "admin/seo";		
	}
	
	
	@RequestMapping(value = "/json/", method = RequestMethod.GET)
	public @ResponseBody GenericResponse elements(){
		
		GenericResponse r = new GenericResponse();
			
		List<Seo> seos = seoDao.createQuery().order("url").asList();
		
		r.setSuccess(true);
		r.setResults(seos);
		
		return r;		
	}
	
	
	@RequestMapping(value = "/json/{stringId}", method = RequestMethod.GET)
	public @ResponseBody Seo elementDetails(@PathVariable String stringId){
				
		return seoDao.get(new ObjectId(stringId));		
	}
	

	@RequestMapping(value = "/json/save", method = RequestMethod.POST)
	public @ResponseBody GenericResponse saveElem(
			@RequestBody Seo seo,
			@AuthenticationPrincipal UserDetailsExt user){
		
		
		GenericResponse r = new GenericResponse();
		r.setSuccess(false);
		
		if(seo != null){
			
			Seo seoDb = seoDao.get(new ObjectId(seo.get_id()));
			if(seoDb != null){
				
				String seoDbString = "";
				try {
					/* Convert productDb to JSON */
					ObjectMapper mapper = new ObjectMapper();
					seoDbString = mapper.writeValueAsString(seoDb);
				} catch (Exception e1) {}
				
				
				seoDb.setDescription(seo.getDescription());
				seoDb.setKeywords(seo.getKeywords());
				seoDb.setNobots(seo.isNobots());
				seoDb.setTitle(seo.getTitle());
				
				seoDao.save(seoDb);
				r.setSuccess(true);
				
				/**
				 * Update Log
				 * */
				try {
					
					Log log = new Log();
					log.setCollection("seo");
					log.setDetails("seo document updated. Old doc - " + seoDbString);
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
				r.setMessage("No corresponding entry found in DB");
			}
		}
		
		else{
			r.setMessage("Invalid update parameters.");
		}

		
		return r;		
	}
}
