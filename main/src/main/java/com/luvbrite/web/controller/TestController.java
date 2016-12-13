package com.luvbrite.web.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
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
import com.luvbrite.web.models.OrderDispatchInfo;
import com.luvbrite.web.models.Price;
import com.luvbrite.web.models.Product;
import com.luvbrite.web.models.ProductFilters;
import com.zaxxer.hikari.HikariDataSource;


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
	
	@Autowired
	private HikariDataSource ds;

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
	

	@RequestMapping(value = "/onetime/populateprices")
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


	
	@RequestMapping(value = "/onetime/update-dispatch-cancelled")
	public @ResponseBody String updateDispatchCancelled() throws Exception{
		
		String query = "SELECT ds.id, ds.cancellation_reason, ds.status, "
				+ "ooi.order_number::numeric AS order_number "
				
				+ "FROM dispatch_sales_info ds "
					+ " JOIN online_order_info ooi ON ooi.dispatch_sales_id = ds.id "
				
				+ "WHERE ds.cancellation_reason "
					+ "NOT IN ('', 'Order updated. New dispatch created and this one is cancelled', "
					+ "'Duplicate', 'DUPLICATE', 'copy', 'duplicate', 'c', 'C')";
		
		Connection tcon = null;
		PreparedStatement pst = null;
		ResultSet rs = null;	
		
		String comments = "";
		long orderNumber = 0;
		
		int counter = 0;
		
		try {
			
			tcon = ds.getConnection();			
			pst = tcon.prepareStatement(query);
			rs = pst.executeQuery();
			while(rs.next()){
				comments = rs.getString("cancellation_reason");
				orderNumber = rs.getLong("order_number");
				
				Order o = orderDao.findOne("orderNumber", orderNumber);
				if(o != null &&
						(o.getDispatch() == null || o.getDispatch().getDateFinished() == null)){
					
					if(o.getStatus().equals("delivered")){
						System.out.println("Order already marked delivered. ordernumber " + orderNumber);
					}
					else {
						
						OrderDispatchInfo d = new OrderDispatchInfo();
						if(o.getDispatch() != null){
							d = o.getDispatch();
						}
						
						d.setComments(comments);
						d.setLockStatus(rs.getString("status"));
						o.setDispatch(d);
						
						//Set order status to Cancelled
						o.setStatus("cancelled");
						
						orderDao.save(o);
						
						counter++;
					}
				}
				
			}
			
			rs.close();rs=null;
			pst.close();pst=null;			
			tcon.close();tcon=null;

			System.out.println("************************");
			System.out.println(counter + " orders updated.");
			System.out.println("************************");
			
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
			try{if(rs!=null)rs.close();}catch(Exception e){}
			try{if(pst!=null)pst.close();}catch(Exception e){}
			try{if(tcon!=null)tcon.close();}catch(Exception e){}
		}
		
		return "layout";
	}

	
	@RequestMapping(value = "/onetime/update-dispatch-info")
	public @ResponseBody String updateDispatchNew() throws Exception{
		
		String query = "SELECT ds.id, ds.date_finished, ds.status, "
				+ "d.driver_name, ooi.order_number::numeric AS order_number "
				
				+ "FROM dispatch_sales_info ds "
					+ "JOIN online_order_info ooi ON ooi.dispatch_sales_id = ds.id "
					+ "LEFT JOIN drivers d ON d.id = ds.driver_id "
				
				+ "WHERE ds.date_finished IS NOT NULL "
					+ "AND ds.cancellation_reason = ''";
		
		Connection tcon = null;
		PreparedStatement pst = null;
		ResultSet rs = null;	
		
		String driverName = "";
		Date dateFinished = null;
		
		long salesId = 0, 
				orderNumber = 0;
		
		int counter = 0;
		
		try {
			
			tcon = ds.getConnection();	
			pst = tcon.prepareStatement(query);	
			rs = pst.executeQuery();
			while(rs.next()){
				
				driverName = rs.getString("driver_name")==null ? "No Driver" : rs.getString("driver_name");
				dateFinished = rs.getTimestamp("date_finished")==null ? null : rs.getTimestamp("date_finished");
				salesId = rs.getLong("id");
				orderNumber = rs.getLong("order_number");
				
				if(dateFinished!=null){
					
					Order o = orderDao.findOne("orderNumber", orderNumber);					
					if(o!=null) {
						
						OrderDispatchInfo d = new OrderDispatchInfo();
						if(o.getDispatch() != null){
							d = o.getDispatch();
						}
						
						d.setDateFinished(dateFinished);
						d.setDriver(driverName);
						d.setSalesId(salesId);
						d.setLockStatus(rs.getString("status"));	
						
						o.setDispatch(d);
						
						//Set order status to Delivered
						o.setStatus("delivered");
						
						orderDao.save(o);
						
						counter++;				
					}
					
					else {
						System.out.println("No mongo order found for ordernumber " + orderNumber);
					}
				}
			}
			
			rs.close();rs=null;
			pst.close();pst=null;			
			tcon.close();tcon=null;

			System.out.println("************************");
			System.out.println(counter + " orders updated.");
			System.out.println("************************");
			
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
			try{if(rs!=null)rs.close();}catch(Exception e){}
			try{if(pst!=null)pst.close();}catch(Exception e){}
			try{if(tcon!=null)tcon.close();}catch(Exception e){}
		}
		
		return "layout";
	}
}
