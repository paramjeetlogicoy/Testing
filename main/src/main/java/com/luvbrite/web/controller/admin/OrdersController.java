package com.luvbrite.web.controller.admin;

import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.luvbrite.dao.LogDAO;
import com.luvbrite.dao.OrderDAO;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.utils.PaginationLogic;
import com.luvbrite.web.models.GenericResponse;
import com.luvbrite.web.models.Log;
import com.luvbrite.web.models.Order;
import com.luvbrite.web.models.ResponseWithPg;


@Controller
@RequestMapping(value = {"/admin/orders","/admin/order"})
public class OrdersController {
	
	private static Logger logger = LoggerFactory.getLogger(OrdersController.class);

	@Autowired
	private OrderDAO dao;
	
	@Autowired
	private LogDAO logDao;
	
	@RequestMapping(method = RequestMethod.GET)
	public String mainPage(ModelMap model){		
		return "admin/orders";		
	}	
	
	
	@RequestMapping(value = "/json/", method = RequestMethod.GET)
	public @ResponseBody ResponseWithPg orders(
			@RequestParam(value="p", required=false) Integer page,
			@RequestParam(value="q", required=false) String query,
			@RequestParam(value="o", required=false) String order){

		ResponseWithPg rpg = new ResponseWithPg();		

		if(query==null) query = "";
		if(order==null) order = "-orderNumber";
		
		int offset = 0,
			limit = 15; //itemsPerPage
		
		if(page==null) page = 1;
		if(page >1) offset = (page-1)*limit;
		
		PaginationLogic pgl = new PaginationLogic((int)dao.count(query), limit, page);
		List<Order> orders = dao.find(order, limit, offset, query);

		rpg.setSuccess(true);
		rpg.setPg(pgl.getPg());
		rpg.setRespData(orders);
		
		return rpg;	
	}
	

	@RequestMapping(value = "/json/{orderNumber}")
	public @ResponseBody Order orderDetails(@PathVariable long orderNumber, ModelMap model){	
		
		return dao.findOne("orderNumber", orderNumber);				
	}
	

	@RequestMapping(value = "/json/savestatus")
	public @ResponseBody GenericResponse saveStatus(@RequestBody Order order){	

		GenericResponse r = new GenericResponse();
		if(order.getStatus().trim().equals("")){
			r.setMessage("Invalid order status.");			
		}
		else{
			
			Order orderDb = dao.get(order.get_id());			
			if(orderDb.get_id()==order.get_id()){		
				
				String prevStatus = orderDb.getStatus();
				
				orderDb.setStatus(order.getStatus());
				
				/**
				 * Here we are saving only the status.
				 * Remaining information is same as that 
				 * pulled from DB
				 **/			
				dao.save(orderDb);
				
				
				/**
				 * Update Log
				 * */
				try {
					
					Log log = new Log();
					log.setCollection("orders");
					log.setDetails("order.status changed. previous value: " + prevStatus);
					log.setDate(Calendar.getInstance().getTime());
					log.setKey(order.get_id());
					
					logDao.save(log);					
				}
				catch(Exception e){
					logger.error(Exceptions.giveStackTrace(e));
				}
				
				r.setSuccess(true);
			}
			
			else {
				r.setMessage("Invalid order Id");
			}
		}
		return r;		
	}
	

	@RequestMapping(value = "/{orderNumber}")
	public String admin(@PathVariable long orderNumber, ModelMap model){	
		model.addAttribute("orderNumber", orderNumber);
		
		return "admin/order/details";		
	}
}
