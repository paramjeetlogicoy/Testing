package com.luvbrite.web.controller.admin;

import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.ResponseBody;

import com.luvbrite.dao.CategoryDAO;
import com.luvbrite.dao.LogDAO;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.web.models.Category;
import com.luvbrite.web.models.GenericResponse;
import com.luvbrite.web.models.Log;
import com.luvbrite.web.validator.CategoryValidator;


@Controller
@RequestMapping(value = {"/admin/categories","/admin/category"})
public class CategoriesController {
	
	private static Logger logger = LoggerFactory.getLogger(CategoriesController.class);

	@Autowired
	private CategoryDAO dao;
	
	@Autowired
	private LogDAO logDao;
	
	@Autowired
	private CategoryValidator validator;
	
	@InitBinder("category")
	protected void InitBinder(WebDataBinder binder){
		binder.setValidator(validator);
	}

	@RequestMapping(value = {"", "/json/list"})
	public @ResponseBody List<Category> mainPage(ModelMap model){
		
		return dao.find(
					dao.getDs()
					.createQuery(dao.getEntityClass())
					.order("name")
				).asList();		
	}	

	@RequestMapping(value = "/json/save", method = RequestMethod.POST)
	public @ResponseBody GenericResponse save(
			@Validated @RequestBody Category category, 
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
		}
		
		else {
			
			dao.save(category);
			
			
			/**
			 * Update Log
			 * */
			try {
				
				Log log = new Log();
				log.setCollection("categories");
				log.setDetails("Category document updated");
				log.setDate(Calendar.getInstance().getTime());
				log.setKey(category.get_id());
				
				logDao.save(log);					
			}
			catch(Exception e){
				logger.error(Exceptions.giveStackTrace(e));
			}
			
			r.setSuccess(true);			
		}
		
		return r;
	}

	@RequestMapping(value = "/json/create", method = RequestMethod.POST)
	public @ResponseBody Category create(
			@RequestBody Category category, 
			BindingResult result){
		
		//Generate CategoryId
		long Id = dao.getNextSeq();
		if(Id != 0l){
			category.set_id(Id);
			dao.save(category);
			
			
			/**
			 * Update Log
			 * */
			try {
				
				Log log = new Log();
				log.setCollection("categories");
				log.setDetails("Category created");
				log.setDate(Calendar.getInstance().getTime());
				log.setKey(Id);
				
				logDao.save(log);					
			}
			catch(Exception e){
				logger.error(Exceptions.giveStackTrace(e));
			}
		}
		
		else{
			category.set_id(0l);
			category.setDescription("Error generating new category ID.");
		}
		
		return category;
	}

	@RequestMapping(value = "/json/delete/{catId}", method = RequestMethod.POST)
	public @ResponseBody GenericResponse create(
			@PathVariable long catId){
		
		GenericResponse r = new GenericResponse();
		if(catId!=0l) {
			dao.deleteById(catId);
			
			
			/**
			 * Update Log
			 * */
			try {
				
				Log log = new Log();
				log.setCollection("categories");
				log.setDetails("Category deleted");
				log.setDate(Calendar.getInstance().getTime());
				log.setKey(catId);
				
				logDao.save(log);					
			}
			catch(Exception e){
				logger.error(Exceptions.giveStackTrace(e));
			}
			
			r.setSuccess(true);
		}
		
		else{
			r.setSuccess(false);
			r.setMessage("Invalid Category Id");
		}
		
		return r;
	}
}
