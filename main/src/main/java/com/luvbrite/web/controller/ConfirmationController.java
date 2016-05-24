package com.luvbrite.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.luvbrite.dao.OrderDAO;
import com.luvbrite.web.models.Order;


@Controller
@RequestMapping(value = "/confirmation")
public class ConfirmationController {

	@Autowired
	private OrderDAO dao;
	
	@RequestMapping(value = "/{orderNumber}", method = RequestMethod.GET)
	public String homePage(@PathVariable Long orderNumber, 
			ModelMap model){
		
		if(orderNumber != null){
			
			Order order = dao.findOne(dao.getDs()
					.createQuery(dao.getEntityClass())
					.field("orderNumber").equal(orderNumber)
					.retrievedFields(true, "customer.name", "orderNumber"));
			
			if(order != null){
				model.addAttribute("order", order);
			}
		}
		
		return "confirmation";		
	}
}
