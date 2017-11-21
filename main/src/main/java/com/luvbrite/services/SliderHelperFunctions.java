package com.luvbrite.services;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.luvbrite.dao.ControlRecordDAO;
import com.luvbrite.dao.LogDAO;
import com.luvbrite.dao.PageSliderDAO;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.web.models.AttrValue;
import com.luvbrite.web.models.ControlRecord;
import com.luvbrite.web.models.Log;
import com.luvbrite.web.models.PageSlider;
import com.luvbrite.web.models.SliderInfo;
import com.luvbrite.web.models.SliderObject;


@Service
@PropertySource("classpath:/env.properties")
public class SliderHelperFunctions {

	private static Logger logger = Logger.getLogger(SliderHelperFunctions.class);

	@Autowired
	private PageSliderDAO pageSliderDao;
	
	@Autowired
	private ControlRecordDAO controlRecordDao;
	
	@Autowired
	private LogDAO logDao;
	
	@Autowired
	private Environment env;
	
	public SliderObject getSliderFinalObject(String sliderName){
		
		SliderObject so = new SliderObject();
		
		try{
			
			List<SliderInfo> sliderInfos = new ArrayList<>();
			List<PageSlider> pageSliders = pageSliderDao.createQuery()
						.filter("sliderName", sliderName)
						.filter("active", true)
						.order("sliderInfo.order")
						.asList();

			for(PageSlider ps: pageSliders){
				sliderInfos.add(ps.getSliderInfo());
			}
			
			if(!sliderInfos.isEmpty()) {
				
				StringBuilder styles = new StringBuilder();
				StringBuilder mediaStyles = new StringBuilder();
				StringBuilder html = new StringBuilder();
				StringBuilder modalHtml = new StringBuilder();
				
				boolean firstSlide = true;
				
				for(SliderInfo sf : sliderInfos){

					String sliderClassName = sf.getName().toLowerCase().replaceAll(" ", "-") + "-slider";
					
					
					//Setup styles
					styles.append(".")
							.append(sliderClassName)
							.append("{")
								.append("background-image:url(")
								.append(env.getProperty("cdnPath"))
								.append(sf.getSliderImg())
								.append(");");
					
					
					if(sf.getBackgroundColor() != null 
							&& !sf.getBackgroundColor().equals("")){
						
						styles.append("background-color:")
						.append(sf.getBackgroundColor())
						.append(" !important;");
					}
					

					if(sf.getSliderImgSM() != null 
							&& !sf.getSliderImgSM().equals("")){
						
						mediaStyles.append(".")
							.append(sliderClassName)
							.append("{")
								.append("background-image:url(")
								.append(env.getProperty("cdnPath"))
								.append(sf.getSliderImgSM())
								.append(");")
							.append("}");
					}
					
					
					styles.append("}");
					
					
					//setup html
					String sliderClass = sliderClassName + " item";
					if(firstSlide){
						firstSlide = false;
						sliderClass += " active ";
					}
					
					html.append("<div class=\"")
					.append(sliderClass)
					.append("\">");
					
					if(sf.isModal()){
						html.append("<a class=\"fullon-a\" data-toggle=\"modal\" data-target=\"#")
						.append(sliderClassName)
						.append("\"></a>");
						
						
						//setup modalHtml as well
						modalHtml.append("<div class=\"modal fade\" id=\"")
						.append(sliderClassName)
						.append("\" tabindex=\"-1\" role=\"dialog\">")
							.append("<div class=\"modal-dialog\" role=\"document\">")
							.append("<div class=\"modal-content\">")
							
							.append("<div class=\"modal-header\">")
							.append("<button type=\"button\" class=\"close\" data-dismiss=\"modal\"")
							.append("aria-label=\"Close\">")
							.append("<span aria-hidden=\"true\">&times;</span>")
							.append("</button>")
							.append("<h3 class=\"modal-title\">Offer Details</h3>")
							.append("</div>") //.modal-header ends
							
							.append("<div class=\"modal-body\">")
							.append(sf.getModalHtml())
							.append("</div>")  //.modal-body ends
							
							.append("</div>") //.modal-content ends
							
							.append("</div>") //.modal-dialog ends
							.append("</div>");//.modal ends
						
					}
					
					else{
						html.append("<a class=\"fullon-a\" href=\"")
						.append(sf.getLinkUrl())
						.append("\"></a>");
					}
					
					html.append("</div>");
					
				}
				
				
				so.setStyles(styles.toString() + "@media (min-width: 768px) { " + mediaStyles.toString() + "}");
				so.setHtml(html.toString());
				so.setModalHtml(modalHtml.toString());
				so.setName(sliderName);
				
			}
			else{
				so = null;
			}
			
		}catch(Exception e){
			logger.error(Exceptions.giveStackTrace(e));
		}
		
		return so;
	}
	
	
	public String publishSlider(String sliderName, String user){
		
		String resp = "";
		
		SliderObject so = getSliderFinalObject(sliderName);
		if(so != null){
			
			ControlRecord cr = controlRecordDao.findOne("_id", "slider_" + sliderName);
			if(cr == null){
				cr = new ControlRecord();
				cr.set_id("slider_" + sliderName);
			}
			
			List<AttrValue> params = new ArrayList<>();
			AttrValue ar = new AttrValue();
			
			ar.setAttr("name");
			ar.setValue(sliderName);
			params.add(ar);
			
			ar.setAttr("styles");
			ar.setValue(so.getStyles());
			params.add(ar);
			
			ar.setAttr("html");
			ar.setValue(so.getHtml());
			params.add(ar);
			
			ar.setAttr("modalHtml");
			ar.setValue(so.getModalHtml());
			params.add(ar);
			
			cr.setParams(params);
			
			controlRecordDao.save(cr);
			
			resp = "";
			

			 /**
			  * Update Log
			  * */
			 try {

				 Log log = new Log();
				 log.setCollection("controlrecords");
				 log.setDetails("Control record updated for sliders - " + sliderName);
				 log.setDate(Calendar.getInstance().getTime());
				 log.setUser(user);

				 logDao.save(log);					
			 }
			 catch(Exception e){
				 logger.error(Exceptions.giveStackTrace(e));
			 }
		}
		
		else{
			resp = "Error creating slider object";
		}
		
		
		return resp;
	}

}
