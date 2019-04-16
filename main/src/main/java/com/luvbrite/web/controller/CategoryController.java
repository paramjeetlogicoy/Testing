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
import com.luvbrite.dao.ProductDAO;
import com.luvbrite.web.models.Category;
import com.luvbrite.web.models.Product;
import com.luvbrite.web.models.UserDetailsExt;


@Controller
@RequestMapping(value = {"/category", "/categories"})
public class CategoryController {
	
	@Autowired
	private ProductDAO prdDao;
	
	@Autowired
	private CategoryDAO catDao;
	

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
		
		model.addAttribute("cUrl", categoryUrl);
		
		Category c = catDao.findOne("url", categoryUrl);		
		if(c != null){
			String categoryName = c.getName();
			String sortOrder = c.getSortOrder();
			
			List<Product> products = prdDao.createQuery()
					.field("categories").equal(categoryName)
					.filter("status", "publish")
					.filter("stockStat", "instock")
					.order(sortOrder)
					.asList();
			List<Product> prodFromInv 	=	new AvailableProducts().getAvailProdsFromInv(products);
			model.addAttribute("products", prodFromInv);
			//model.addAttribute("products", products);
			model.addAttribute("category", categoryName);
			model.addAttribute("page", "category");
			model.addAttribute("sortOrder", sortOrder);
			
                       /* System.out.print("==============================");
                       for(int i =0 ;i<products.size();i++){
                        System.out.println("product Detail==============="+products.get(i));
                       } */
                       
                       
			return "products";
		}		
		else{
			return "404";
		}
	}	
	

	@RequestMapping(value = "/listproducts/{categoryName}")
	public @ResponseBody List<Product> listProductsByCategory(@PathVariable String categoryName){
		
		return prdDao.createQuery()
				.field("categories").equal(categoryName)
				.asList();		
	}
}
