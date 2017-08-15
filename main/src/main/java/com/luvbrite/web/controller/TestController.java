package com.luvbrite.web.controller;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.luvbrite.dao.LogDAO;
import com.luvbrite.dao.OrderDAO;
import com.luvbrite.dao.ProductDAO;
import com.luvbrite.services.PostOrderMeta;
import com.luvbrite.web.models.GenericResponse;
import com.luvbrite.web.models.Log;
import com.luvbrite.web.models.Order;
import com.luvbrite.web.models.Product;


@Controller
public class TestController {
	
	@Autowired
	private OrderDAO orderDao;
	
	
	@Autowired
	private LogDAO logDao;
	
	@Autowired
	private PostOrderMeta postService;	

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
	
	
	@RequestMapping(value = "/onetime/product-arrival-date")
	public @ResponseBody GenericResponse productArrivalDate(){	

		GenericResponse gr  = new GenericResponse();
		gr.setSuccess(false);
		gr.setMessage("");
		
		int processCounter = 0;
		StringBuilder sb = new StringBuilder();
		
		List<Product> products = prdDao.createQuery().order("-_id").asList();
		if(products != null){
			
			for(Product p : products){					
				
				String title = p.getName();
				Date date = p.getDateCreated();
				
				if(title != null && !title.equals("")){
					if(date != null){
						
						p.setNewBatchArrival(date);
						prdDao.save(p);
					}	
					else{
						
						Log log = logDao.createQuery()
								.field("details").equal("product created")
								.field("key").equal(p.get_id())
								.field("collection").equal("products")
								.get();
						
						if(log != null){
							
							p.setNewBatchArrival(log.getDate());
							p.setDateCreated(log.getDate());
							prdDao.save(p);
						}
						else {
							sb.append("Date information not found for product - " + title);
						}
					}

					processCounter++;
				}				
			}			
			
			sb.append(processCounter + " products arrival date information updated ");
		}

		gr.setMessage(sb.toString());
		return gr;		
	}
	
}
