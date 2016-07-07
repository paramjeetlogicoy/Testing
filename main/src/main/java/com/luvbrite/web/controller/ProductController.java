package com.luvbrite.web.controller;

import java.util.ArrayList;
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
import com.luvbrite.web.models.Price;
import com.luvbrite.web.models.ProdCatResponse;
import com.luvbrite.web.models.Product;
import com.luvbrite.web.models.UserDetailsExt;


@Controller
@RequestMapping(value = {"/products", "/product"})
public class ProductController {
	
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

		List<Product> products = prdDao.createQuery()
				.filter("status", "publish")
				.filter("stockStat", "instock")
				.order("-_id")
				.asList();
		
		model.addAttribute("products", products);
		
		return "products";				
	}
	
	
	@RequestMapping(value = "/")
	public String homePage(@AuthenticationPrincipal 
			UserDetailsExt user, 
			ModelMap model) {		
		return home(user, model);		
	}		
	
	
	@RequestMapping(value = "/json/prod-cat/published")
	public @ResponseBody ProdCatResponse ListPublishedProductsCategories() {		
		ProdCatResponse pcr = new ProdCatResponse();
		
		List<Product> products = prdDao.createQuery().filter("status", "publish").asList();
		List<Category> categories = catDao.find().asList();
		
		pcr.setSuccess(true);
		pcr.setProducts(products);
		pcr.setCategories(categories);
		
		return pcr;		
	}
	
	
	@RequestMapping(value = "/{productUrl}")
	public String product(@AuthenticationPrincipal 
			UserDetailsExt user,ModelMap model, @PathVariable String productUrl) {		

		if(user!=null && user.isEnabled())
			model.addAttribute("userId", user.getId());
		
		Product p = prdDao.findOne("url", productUrl);
		model.addAttribute("url", productUrl);
		model.addAttribute("product", p);
		
		/* Find related products */
		if(p != null){
			
			List<Product> relatedProducts = new ArrayList<Product>();
			
			List<Integer> ids = p.getRps();
			if(ids != null && ids.size()>0){			
				relatedProducts = prdDao.createQuery()
						.field("_id").in(ids)
						.asList();
				
				model.addAttribute("rps", relatedProducts);
			}
		}
		
		return "product-page";		
	}	
	

	@RequestMapping(value = "/json/{productId}/price")
	public @ResponseBody List<Price> price(@PathVariable long productId){			
		return priceDao.findPriceByProduct(productId);		
	}
}
