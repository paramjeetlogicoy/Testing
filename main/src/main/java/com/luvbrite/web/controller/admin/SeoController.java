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
import com.luvbrite.dao.CategoryDAO;
import com.luvbrite.dao.LogDAO;
import com.luvbrite.dao.ProductDAO;
import com.luvbrite.dao.SeoDAO;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.web.models.Category;
import com.luvbrite.web.models.GenericResponse;
import com.luvbrite.web.models.Log;
import com.luvbrite.web.models.Product;
import com.luvbrite.web.models.Seo;
import com.luvbrite.web.models.SeoElem;
import com.luvbrite.web.models.UserDetailsExt;


@Controller
@RequestMapping(value = "/admin/seo")
public class SeoController {
	
	private static Logger logger = Logger.getLogger(SeoController.class);
		
	@Autowired
	private LogDAO logDao;
	
	@Autowired
	private SeoDAO seoDao;

	@Autowired
	private ProductDAO prdDao;

	@Autowired
	private CategoryDAO catDao;
	
	
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
		
		Seo seo = seoDao.get(new ObjectId(stringId));
		if(seo != null){
			
			String pageType = seo.getPageType();
			if(pageType.equals("product")){
				Product p = prdDao.createQuery()
						.filter("url", seo.getUrl())
						.retrievedFields(true, "seoElem").get();
				
				seo.setSeoElem(p.getSeoElem());
			}
			
			else if(pageType.equals("category")){
				Category c = catDao.createQuery()
						.filter("url", seo.getUrl().replace("category/", ""))
						.retrievedFields(true, "seoElem").get();

				seo.setSeoElem(c.getSeoElem());
			}
		}
		
		return seo;		
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
				
				String seoElemString = "",
						logDetailsString = "";
				
				if(seo.getPageType().equals("product")){
					Product p = prdDao.findOne("url", seo.getUrl());
					if(p != null){
						
						SeoElem seoElem = p.getSeoElem();
						if(seoElem == null) seoElem = new SeoElem();

						try {
							/* Convert seoElem to JSON */
							ObjectMapper mapper = new ObjectMapper();
							seoElemString = mapper.writeValueAsString(seoElem);
						} catch (Exception e1) {}


						seoElem.setDescription(seo.getSeoElem().getDescription());
						seoElem.setKeywords(seo.getSeoElem().getKeywords());
						seoElem.setNobots(seo.getSeoElem().isNobots());
						seoElem.setTitle(seo.getSeoElem().getTitle());
						seoElem.setMainImgAlt(seo.getSeoElem().getMainImgAlt());

						p.setSeoElem(seoElem);
						prdDao.save(p);
						
						logDetailsString = "Product Seo Element update. ProductId - " + p.get_id();

						r.setSuccess(true);
					}
					
					else{
						r.setMessage("No corresponding Product found in DB for this Seo Element");
					}
				}

				else if(seo.getPageType().equals("category")){
					Category c = catDao.findOne("url", seo.getUrl().replace("category/", ""));
					if(c != null){

						SeoElem seoElem = c.getSeoElem();
						if(seoElem == null) seoElem = new SeoElem();

						try {
							/* Convert seoElem to JSON */
							ObjectMapper mapper = new ObjectMapper();
							seoElemString = mapper.writeValueAsString(seoElem);
						} catch (Exception e1) {}


						seoElem.setDescription(seo.getSeoElem().getDescription());
						seoElem.setKeywords(seo.getSeoElem().getKeywords());
						seoElem.setNobots(seo.getSeoElem().isNobots());
						seoElem.setTitle(seo.getSeoElem().getTitle());
						seoElem.setMainImgAlt(seo.getSeoElem().getMainImgAlt());

						c.setSeoElem(seoElem);
						catDao.save(c);
						
						logDetailsString = "Category Seo Element update. CategoryId - " + c.get_id();

						r.setSuccess(true);
					}
					
					else{
						r.setMessage("No corresponding category entry found in DB for this Seo Element");
					}

				}

				else {


					SeoElem seoElem = seoDb.getSeoElem();
					if(seoElem == null) seoElem = new SeoElem();

					try {
						/* Convert seoElem to JSON */
						ObjectMapper mapper = new ObjectMapper();
						seoElemString = mapper.writeValueAsString(seoElem);
					} catch (Exception e1) {}


					seoElem.setDescription(seo.getSeoElem().getDescription());
					seoElem.setKeywords(seo.getSeoElem().getKeywords());
					seoElem.setNobots(seo.getSeoElem().isNobots());
					seoElem.setTitle(seo.getSeoElem().getTitle());
					seoElem.setMainImgAlt(seo.getSeoElem().getMainImgAlt());

					seoDb.setSeoElem(seoElem);
					seoDao.save(seoDb);

					logDetailsString = "Seo Element update. Url - " + seoDb.getUrl();
					
					r.setSuccess(true);

				}

				/**
				 * Update Log
				 * */
				try {

					Log log = new Log();
					log.setCollection("seo");
					log.setDetails(logDetailsString + ". Seo ID - " + seo.get_id());
					log.setPreviousDoc(seoElemString);
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
