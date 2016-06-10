package com.luvbrite.web.controller.admin;

import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.luvbrite.dao.ArticleDAO;
import com.luvbrite.dao.LogDAO;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.utils.PaginationLogic;
import com.luvbrite.web.models.Article;
import com.luvbrite.web.models.GenericResponse;
import com.luvbrite.web.models.Log;
import com.luvbrite.web.models.ResponseWithPg;
import com.luvbrite.web.models.UserDetailsExt;


@Controller
@RequestMapping(value = "/admin/articles")
public class ArticlesController {
	
	private static Logger logger = LoggerFactory.getLogger(ArticlesController.class);
	
	@Autowired
	private ArticleDAO dao;
	
	
	@Autowired
	private LogDAO logDao;
	
	
	@RequestMapping(method = RequestMethod.GET)
	public String mainPage(ModelMap model){		
		return "admin/articles";		
	}
	
	
	@RequestMapping(value = "/json/", method = RequestMethod.GET)
	public @ResponseBody ResponseWithPg users(
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
		List<Article> articles = dao.find(order, limit, offset, query);

		rpg.setSuccess(true);
		rpg .setPg(pgl.getPg());
		rpg.setRespData(articles);
		
		return rpg;	
	}
	

	@RequestMapping(value = "/json/create", method = RequestMethod.POST)
	public @ResponseBody GenericResponse createUser(
			@RequestBody Article article){
		
		GenericResponse r = new GenericResponse();		
		r.setSuccess(false);
		
		StringBuilder errMsgs = new StringBuilder();
		
		if(article == null){
			errMsgs.append("Invalid article <br />");
		}
		
		else {
			
			if(article.getTitle()==null || article.getTitle().equals(""))
				errMsgs.append("Article needs a title <br />");
			
			if(article.getBody()==null || article.getBody().equals(""))
				errMsgs.append("Article needs a body <br />");
			
			if(article.getPermalink()==null || article.getPermalink().equals(""))
				errMsgs.append("Article needs a URL <br />");
			
		}
		
		if(errMsgs.length()==0){
			
			long articleId = dao.getNextSeq();
			article.setDate(Calendar.getInstance().getTime());
			article.set_id(articleId);
			
			dao.save(article);
			
			
			/**
			 * Update Log
			 * */
			try {
				
				Log log = new Log();
				log.setCollection("articles");
				log.setDetails("Article created.");
				log.setDate(Calendar.getInstance().getTime());
				log.setKey(article.get_id());
				
				logDao.save(log);						
			}
			catch(Exception e){
				logger.error(Exceptions.giveStackTrace(e));
			}
			
			r.setSuccess(true);
			r.setMessage(article.get_id()+"");
		}
		else{
			
			r.setMessage(errMsgs.toString());
		}
		
		
		return r;		
	}
	

	
	@RequestMapping(value = "/json/save", method = RequestMethod.POST)
	public @ResponseBody GenericResponse saveUser(
			@AuthenticationPrincipal UserDetailsExt user,
			@RequestBody Article article){

		GenericResponse r = new GenericResponse();		
		r.setSuccess(false);

		StringBuilder errMsgs = new StringBuilder();

		if(article == null){
			errMsgs.append("Invalid article <br />");
		}

		else {

			if(article.get_id()==0)
				errMsgs.append("Invalid article ID <br />");

			if(article.getTitle()==null || article.getTitle().equals(""))
				errMsgs.append("Article needs a title <br />");

			if(article.getBody()==null || article.getBody().equals(""))
				errMsgs.append("Article needs a body <br />");

			if(article.getPermalink()==null || article.getPermalink().equals(""))
				errMsgs.append("Article needs a URL <br />");

		}

		if(errMsgs.length()==0){

			Article articleDb = dao.get(article.get_id());
			if(articleDb != null && 
					articleDb.get_id() == article.get_id()){		


				dao.save(article);


				/**
				 * Update Log
				 * */
				try {

					Log log = new Log();
					log.setCollection("articles");
					log.setDetails("article updated.");
					log.setDate(Calendar.getInstance().getTime());
					log.setKey(article.get_id());
					log.setUser(user.getUsername());

					logDao.save(log);						
				}
				catch(Exception e){
					logger.error(Exceptions.giveStackTrace(e));
				}

				r.setSuccess(true);
			}

			else {
				r.setMessage("No record found for article ID " + article.get_id());
			}
		}
		else{
			r.setMessage(errMsgs.toString());
		}


		return r;		
	}
}
