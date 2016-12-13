package com.luvbrite.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.luvbrite.dao.OrderDAO;
import com.luvbrite.dao.PriceDAO;
import com.luvbrite.dao.ProductDAO;
import com.luvbrite.services.PostOrderMeta;
import com.luvbrite.web.models.GenericResponse;
import com.luvbrite.web.models.Order;
import com.luvbrite.web.models.Price;
import com.luvbrite.web.models.Product;
import com.luvbrite.web.models.ProductFilters;


@Controller
public class TestController {
	
	@Autowired
	private OrderDAO orderDao;
	
	@Autowired
	private PostOrderMeta postService;	

	@Autowired
	private PriceDAO priceDao;

	@Autowired
	private ProductDAO prdDao;
	

	@RequestMapping(value = "/test/meta/{orderNumber}")
	public @ResponseBody GenericResponse emailTemplateTest(@PathVariable Long orderNumber){	

		GenericResponse gr  = new GenericResponse();
		gr.setSuccess(false);
		gr.setMessage("");
		
		if(orderNumber != null){
			Order order = orderDao.findOne("orderNumber", orderNumber);
			if(order != null){
				
				String resp = postService.postOrder(order);
				if(resp.equals("success")){
					gr.setSuccess(true);
				}
				else{
					gr.setMessage(resp);
				}					
			}
		}
		else{
			gr.setMessage("Ordernumber is null");
		}

		return gr;		
	}
	

	@RequestMapping(value = "/onetimes/populateprices")
	public @ResponseBody GenericResponse populateLowestPrice(){	

		GenericResponse gr  = new GenericResponse();
		gr.setSuccess(false);
		gr.setMessage("");
		
		int processCounter = 0;
		StringBuilder sb = new StringBuilder();
		
		List<Product> products = prdDao.createQuery().asList();
		if(products != null){
			
			for(Product p : products){
				
				long productId = p.get_id();
				
				if(p.isVariation()){
				
					List<Price> prices = priceDao.findPriceByProduct(productId);
					if(prices != null){
						
						double floorPrice = 9999d;
						
						for(Price z : prices){
							
							//Floor Price
							if(z.getSalePrice() > 0 && z.getSalePrice() < floorPrice){
								floorPrice = z.getSalePrice();
							}
							
							else if(z.getRegPrice() <= floorPrice){
								floorPrice = z.getRegPrice();
							}
						}
						
						
						if(floorPrice != 9999d){
							
							ProductFilters pf = p.getProductFilters();
							if(pf == null) pf = new ProductFilters();
								
							pf.setPrice(floorPrice);
							p.setProductFilters(pf);
							
							prdDao.save(p);
							processCounter++;
						}
						
						else {
							sb.append("Product id " + productId + ". No price found!");
						}
					}
				}
				
				else{
					
					ProductFilters pf = p.getProductFilters();
					if(pf == null) pf = new ProductFilters();
					
					if(p.getSalePrice() > 0){
						pf.setPrice(p.getSalePrice());
					}
					else{
						pf.setPrice(p.getPrice());
					}
					
					p.setProductFilters(pf);
					
					prdDao.save(p);
					processCounter++;
				}
				
			}			
			
			sb.append(processCounter + " products updated!");
			gr.setMessage(sb.toString());
		}

		return gr;		
	}
}
