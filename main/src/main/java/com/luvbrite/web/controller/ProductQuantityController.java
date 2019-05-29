package com.luvbrite.web.controller;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.luvbrite.web.models.productsavailable.ProductsAvailable;


@Controller
@RequestMapping(value = {"/recieve"})
public class ProductQuantityController {
	private static Logger logger = Logger.getLogger(ProductQuantityController.class);
	
	
	
	
	@RequestMapping(value= "/productAvaibleInfo" ,method=RequestMethod.POST)
	public List<ProductsAvailable>  recieveProduct(@RequestBody String productAvailableJsonString) {
		logger.info("ProductListFromInventory"+productAvailableJsonString);
		return null;
		
	}
	
	
}
