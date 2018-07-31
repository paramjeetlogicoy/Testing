package com.luvbrite.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.luvbrite.dao.CategoryDAO;
import com.luvbrite.dao.PriceDAO;
import com.luvbrite.dao.ProductDAO;
import com.luvbrite.dao.ReviewDAO;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.web.models.AttrValue;
import com.luvbrite.web.models.Category;
import com.luvbrite.web.models.Price;
import com.luvbrite.web.models.ProdCatResponse;
import com.luvbrite.web.models.Product;
import com.luvbrite.web.models.Review;
import com.luvbrite.web.models.UserDetailsExt;

import org.apache.log4j.Logger;


@Controller
@RequestMapping(value = {"/products", "/product"})
public class ProductController {
	
	private static Logger logger = Logger.getLogger(ProductController.class);
	
	@Autowired
	private ProductDAO prdDao;
	
	@Autowired
	private CategoryDAO catDao;
	
	@Autowired
	private PriceDAO priceDao;
	
	@Autowired
	private ReviewDAO reviewDao;
	
	
	private List<Product> returnActiveProducts(String sortOrder){
		
		return prdDao.createQuery()
				.filter("status", "publish")
				.filter("stockStat", "instock")
				.order(sortOrder)
				.asList();
	}
	

	@RequestMapping(method = RequestMethod.GET)
	public String home(@AuthenticationPrincipal 
			UserDetailsExt user, 
			ModelMap model){
		
		if(user!=null && user.isEnabled())
			model.addAttribute("userId", user.getId());
		
		String sortOrder = "-newBatchArrival";

		List<Product> products = returnActiveProducts(sortOrder);
		
		model.addAttribute("products", products);
		model.addAttribute("page", "product");
		model.addAttribute("sortOrder", sortOrder);
		
		return "products";	
	}
	
	
	@RequestMapping(value = "/")
	public String homePage(@AuthenticationPrincipal 
			UserDetailsExt user, 
			ModelMap model) {		
		return home(user, model);		
	}
	
	
	@RequestMapping(value = "/product-search")
	public String productSearch(@AuthenticationPrincipal 
			UserDetailsExt user, 
			ModelMap model, @RequestParam(value="s", required=false) String query) {	

		List<Product> products = new ArrayList<Product>();
		String sortOrder = "productFilters.price";
		
		try {
			
			if(user!=null && user.isEnabled())
				model.addAttribute("userId", user.getId());
			
			Pattern regExp = Pattern.compile(query + "*", Pattern.CASE_INSENSITIVE);
			
			products = prdDao.createQuery()
					.filter("status", "publish")
					.filter("stockStat", "instock")
					.field("name").equal(regExp)
					.order(sortOrder)
					.asList();
			
			model.addAttribute("products", products);
			model.addAttribute("page", "search");
			model.addAttribute("query", query);
			model.addAttribute("sortOrder", sortOrder);
			
		} catch(Exception e){
			logger.error("Search - $" + query + "$" + Exceptions.giveStackTrace(e));
		}
		
		return "products";
	}		
	
	
	@RequestMapping(value = "/json/prod-cat/published")
	public @ResponseBody ProdCatResponse ListPublishedProductsCategories() {		
		ProdCatResponse pcr = new ProdCatResponse();
		
		List<Product> products = returnActiveProducts("-newBatchArrival");
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
		
		if(p == null) return "404";
		
		/**
		 * For discontinued products, redirect them to the first category they are under.
		 * If no category found, send them to all products 
		 **/
		if(p.getStatus().equals("discontinued")){
			List<String> categories = p.getCategories();
			if(categories == null || 
					categories.isEmpty()){
				return "redirect:/products";	
			}
			
			Category c = catDao.findOne("name", categories.get(0));
			if(c!=null){
				return "redirect:/category/" + c.getUrl();
			}
			
			return "redirect:/products";	
		}

		model.addAttribute("url", productUrl);
		model.addAttribute("product", p);
		/* Find related products */
		List<Product> relatedProducts = new ArrayList<Product>();
		
		List<Integer> ids = p.getRps();
		if(ids != null && ids.size()>0){			
			relatedProducts = prdDao.createQuery()
					.field("_id").in(ids)
					.asList();
			
			model.addAttribute("rps", relatedProducts);
		}
		
		/*Get double value of average rating*/
		if(p.getRating() != -1 && p.getReviewCount() > 0){			
			double avgRating = p.getRating()/2d;
			model.addAttribute("avgRating", avgRating);
		}
		
		return "product-page";	
			
	}	
	

	@RequestMapping(value = "/json/{productId}/price")
	public @ResponseBody List<Price> price(@PathVariable long productId){			
		return priceDao.findPriceByProduct(productId);		
	}
	

	@RequestMapping(value = "/json/{productId}/topreviews")
	public @ResponseBody List<Review> reviews(
			@PathVariable long productId, 
			@RequestParam(value="s", required=false) String sort){
		
		String orderBy = "-created";
		if(sort != null){
			
			if(sort.equals("toprated")){
				orderBy = "-rating";
			}
			
			else if(sort.equals("lowrated")){
				orderBy = "rating";
			}

			else if(sort.equals("old")){
				orderBy = "created";
			}
			
			else if(sort.equals("latest")){
				orderBy = "-created";
			}
		}
		
		
		List<Review> reviews =  reviewDao.createQuery()
				.field("productId").equal(productId)
				.field("approvalStatus").equal("approved")
				.order(orderBy)
				.limit(10)
				.asList();
		
		return reviews;		
	}
	

	@RequestMapping(value = "/json/getspecialprds")
	public @ResponseBody List<Product> specials(){	
		List<String> cats = new ArrayList<>();
		cats.add("Special");
		cats.add("Super Special");
		
		return prdDao.createQuery().retrievedFields(true, "name", "url").field("categories").in(cats).asList();
	}
	

	@RequestMapping(value = "/json/get5gspecials")
	public @ResponseBody List<Product> fiveGramSpecials(){
		return prdDao.createQuery().retrievedFields(true, "name", "url", "priceRange")
				.field("categories").equal("5g Specials")
				.filter("status", "publish")
				.filter("stockStat", "instock")
				.asList();
	}

	@RequestMapping(value = "/json/get-popularthismonth")
	public @ResponseBody List<Product> getPopularProducts(){
		
		return prdDao.createQuery().retrievedFields(true, "name", "url", "priceRange", "featuredImg")
			    .field("categories").equal("Popular This Month")
				.filter("status", "publish")
				.filter("stockStat", "instock")
				.limit(3)
				.asList();
	}

	@RequestMapping(value = "/json/getCategoryProducts", method = RequestMethod.POST)
	public @ResponseBody List<Product> getCategoryProducts(@RequestBody AttrValue Category){
		
		return prdDao.createQuery().retrievedFields(true, "name", "url", "priceRange", "featuredImg")
			    .field("categories").equal(Category.getValue())
				.filter("status", "publish")
				.filter("stockStat", "instock")
				.order("-newBatchArrival")
				.limit(3)
				.asList();
	}
}
