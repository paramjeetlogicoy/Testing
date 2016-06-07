package com.luvbrite.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.luvbrite.dao.OrderDAO;
import com.luvbrite.services.EmailService;
import com.luvbrite.services.PostOrderMeta;
import com.luvbrite.web.models.GenericResponse;
import com.luvbrite.web.models.Order;


@Controller
public class TestController {
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private OrderDAO orderDao;
	
	@Autowired
	private PostOrderMeta postService;
	

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
}
