package com.luvbrite.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.luvbrite.dao.OrderDAO;
import com.luvbrite.services.PostOrderMeta;
import com.luvbrite.web.models.GenericResponse;
import com.luvbrite.web.models.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = "/testTookanWebhook")
public class TestController {

//    @Autowired
//    private OrderDAO orderDao;
//
//    @Autowired
//    private PostOrderMeta postService;

//	@RequestMapping(value = "/test/meta/{orderNumber}")
//	public @ResponseBody GenericResponse emailTemplateTest(@PathVariable Long orderNumber){	
//
//		GenericResponse gr  = new GenericResponse();
//		gr.setSuccess(false);
//		gr.setMessage("");
//		
//		if(orderNumber != null){
//			Order order = orderDao.findOne("orderNumber", orderNumber);
//			if(order != null){
//				
//				String resp = postService.postOrder(order);
//				if(resp.equals("success")){
//					gr.setSuccess(true);
//				}
//				else{
//					gr.setMessage(resp);
//				}					
//			}
//		}
//		else{
//			gr.setMessage("Ordernumber is null");
//		}
//
//		return gr;		
//	}
    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public ResponseEntity<String> getTookanNotification(@RequestBody String tookanNotification) {

        System.out.println("tookanNotification:--->" + tookanNotification);

        return new ResponseEntity<String>("Recieved Tookan Response", HttpStatus.OK);
    }

}
