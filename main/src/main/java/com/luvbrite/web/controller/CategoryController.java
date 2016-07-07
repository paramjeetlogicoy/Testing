package com.luvbrite.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.luvbrite.dao.CategoryDAO;
import com.luvbrite.dao.PriceDAO;
import com.luvbrite.dao.ProductDAO;
import com.luvbrite.web.models.Category;
import com.luvbrite.web.models.Product;
import com.luvbrite.web.models.UserDetailsExt;


@Controller
@RequestMapping(value = {"/category"})
public class CategoryController {
	
	@Autowired
	private ProductDAO prdDao;
	
	@Autowired
	private CategoryDAO catDao;
	
	@Autowired
	private PriceDAO priceDao;
	

	@RequestMapping(method = RequestMethod.GET)
	public String home(@AuthenticationPrincipal 
			UserDetailsExt user, 
			ModelMap model){
		
		if(user!=null && user.isEnabled())
			model.addAttribute("userId", user.getId());
		
		model.addAttribute("categories", catDao.find());
		
		return "category";				
	}
	
	
	@RequestMapping(value = "/")
	public String homePage(@AuthenticationPrincipal 
			UserDetailsExt user, 
			ModelMap model) {		
		return home(user, model);		
	}	
	
	
	@RequestMapping(value = "/{categoryUrl}")
	public String category(@AuthenticationPrincipal 
			UserDetailsExt user, 
			ModelMap model, 
			@PathVariable String categoryUrl) {		

		if(user!=null && user.isEnabled())
			model.addAttribute("userId", user.getId());
		
		Category c = catDao.findOne("url", categoryUrl);		
		if(c != null){
			model.addAttribute("category", c);
			return "category-page";
		}		
		else{
			return "404";
		}
	}	
	

	@RequestMapping(value = "/listproducts/{categoryName}")
	public @ResponseBody List<Product> price(@PathVariable String categoryName){
		
		return prdDao.createQuery()
				.field("categories").equal(categoryName)
				.asList();		
	}
}