package com.luvbrite.config;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import com.luvbrite.dao.ControlRecordDAO;
import com.luvbrite.utils.Utility;
import com.luvbrite.web.models.AttrValue;
import com.luvbrite.web.models.ControlOptions;
import com.luvbrite.web.models.ControlRecord;
import com.luvbrite.web.models.ModifiedDate;


@Configuration
@PropertySource("classpath:/env.properties")
public class ControlConfig {
	
	@Autowired
	private Environment env;
	
	@Autowired
	private ControlRecordDAO dao;
	
	@Bean
	public ControlOptions configureControl(){
		
		ControlOptions cOps = new ControlOptions();
		
		List<ControlRecord> crs = dao.find().asList();
		if(crs != null && crs.size()>0){
			for(ControlRecord cr : crs)
				if(cr != null){

					
					if(cr.get_id().equals("order_minimum")){
						boolean active = false;
						double value = 0d;
						
						List<AttrValue> params = cr.getParams();
						if(params !=null){
							for(AttrValue ar : params){
								
								if(ar.getAttr().equals("active") 
										&& ar.getValue().equals("1"))
									active = true;
								
								else if(ar.getAttr().equals("minvalue"))
									value = Utility.getDouble(ar.getValue());
							}							
							
							if(active && value >0d){
								cOps.setOrderMinimum(value);
							}
						}
					}
					
					
					
					
					else if(cr.get_id().equals("double_down_offer")){
						boolean active = false;
						double value = 0d;
						
						List<AttrValue> params = cr.getParams();
						if(params !=null){
							for(AttrValue ar : params){
								
								if(ar.getAttr().equals("active") 
										&& ar.getValue().equals("1"))
									active = true;
								
								
								else if(ar.getAttr().equals("minvalue"))
									value = Utility.getDouble(ar.getValue());
								
								
								else if(ar.getAttr().equals("eligible_products")
										&& !ar.getValue().equals("")){
									
									String[] products = ar.getValue().split(",");
									List<Integer> list = new ArrayList<Integer>();
									for(String s : products){
										list.add(Utility.getInteger(s));
									}
									
									cOps.setDoubleDownEligibleProducts(list);
								}
								
								
								else if(ar.getAttr().equals("offer_value")
										&& !ar.getValue().equals("")
										&& !ar.getValue().equals("0")){
									
									cOps.setDoubleDownOfferValue(
											Utility.getDouble(ar.getValue()));
								}
							}							
							
							if(active && value >0d){
								cOps.setDoubleDown(value);
							}
						}
						
					}
					
					
					
					
					else if(cr.get_id().equals("local_delivery_zipcodes")){
						
						List<Integer> localZipcodes = new ArrayList<Integer>();
						List<AttrValue> params = cr.getParams();
						if(params !=null && params.get(0) != null){
							
							AttrValue param = params.get(0);
							String[] zips = param.getValue().split(",");
							for(int i=0; i<zips.length; i++){
								localZipcodes.add(Utility.getInteger(zips[i].trim()));
							}
						}
						
						cOps.setLocalZipcodes(localZipcodes);						
					}
					
					
					
					
					else if(cr.get_id().equals("shipping_zipcodes")){
						
						List<Integer> shippingZipcodes = new ArrayList<Integer>();
						List<AttrValue> params = cr.getParams();
						if(params !=null && params.get(0) != null){
							
							AttrValue param = params.get(0);
							String[] zips = param.getValue().split(",");
							for(int i=0; i<zips.length; i++){
								shippingZipcodes.add(Utility.getInteger(zips[i].trim()));
							}
						}
						
						cOps.setShippingZipcodes(shippingZipcodes);						
					}
					
					
				}
		}
		
		cOps.setDev(false);
		if(env.getProperty("mode").equalsIgnoreCase("dev")){
			cOps.setDev(true);
		}
		
		return cOps;
	}
	
	@Bean(name = "ModifiedDate")
	public ModifiedDate modifiedDates(){
	
		ModifiedDate md = new ModifiedDate();
		String contextPath = env.getProperty("contextPath");
		if(contextPath != null){
			SimpleDateFormat sdf = new SimpleDateFormat("yyMMddhhmm");
			
            File f1 = new File(contextPath + "resources/css/main.css");
            long datetime = f1.lastModified();
            Date d = new Date(datetime);
            md.setCssDate(sdf.format(d));
			
            File f2 = new File(contextPath + "resources/js/comb.min.js");
            datetime = f2.lastModified();
            d = new Date(datetime);
            md.setJsDate(sdf.format(d));
			
            File f3 = new File(contextPath + "resources/js/admin/inv/admin-comb.min.js");
            datetime = f3.lastModified();
            d = new Date(datetime);
            md.setAdminInvJsDate(sdf.format(d));
            
            System.out.println("ModifiedDate called");
		}
		
		
		return md;
	}
}
