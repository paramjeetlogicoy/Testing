package com.luvbrite.web.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.luvbrite.dao.CouponDAO;
import com.luvbrite.dao.OrderDAO;
import com.luvbrite.services.ControlConfigService;
import com.luvbrite.services.EmailService;
import com.luvbrite.services.InOfficeOrderLogic;
import com.luvbrite.services.SliderHelperFunctions;
import com.luvbrite.utils.CouponGen;
import com.luvbrite.web.models.Coupon;
import com.luvbrite.web.models.Email;
import com.luvbrite.web.models.GenericResponse;
import com.luvbrite.web.models.Order;
import com.luvbrite.web.models.SliderObject;
import com.luvbrite.web.models.User;
import com.luvbrite.web.models.UserDetailsExt;
import com.luvbrite.web.models.UserIdentity;
import com.luvbrite.web.models.ordermeta.OrderMain;


@Controller
public class MainController {
	
	@Autowired
	private EmailService emailService;

	@Autowired
	private OrderDAO orderDao;
	
	//@Autowired
	//private ControlConfigService ccs;
	
	@Autowired
	private SliderHelperFunctions shf;
	
	@RequestMapping(value = "/")
	public String homePage(
			@AuthenticationPrincipal UserDetailsExt user, 
			ModelMap model) {
		
		if(user!=null && user.isEnabled())
			model.addAttribute("userId", user.getId());
		

//		Map<String, SliderObject> sliderObjs = ccs.getcOps().getSliderObjs();
//		if(sliderObjs != null){
//			SliderObject so = sliderObjs.get("homepage");
//			if(so != null){
//				model.addAttribute("sliders", so);
//			}
//		}
		
		return "welcome";		
	}	
	
	
	@RequestMapping(value = "/previewslides/{sliderName}")
	public String homePagePreview(
			@PathVariable String sliderName, 
			@AuthenticationPrincipal UserDetailsExt user, 
			ModelMap model) {
		
		if(user!=null && user.isEnabled()){
			model.addAttribute("userId", user.getId());
			
			SliderObject so = shf.getSliderFinalObject(sliderName);
			if(so != null){
				model.addAttribute("sliders", so);
			}
			
			return "welcome";
		}
		
		return "redirect:/";
	}

	
	@RequestMapping(value = "/home")
	public String home(){	
		
		return "redirect:/";		
	}

	
	@RequestMapping(value = "/admin")
	public String admin(){	
		
		return "redirect:/admin/orders";		
	}

	
	@RequestMapping(value = {"/wp-admin","/wp-admin/"})
	public String wpadmin(){
		
		return "redirect:/admin/orders";		
	}

	
	@RequestMapping(value = "/check")
	public @ResponseBody GenericResponse check(HttpServletRequest req){
		GenericResponse r = new GenericResponse();
		try {
			CsrfToken token = new HttpSessionCsrfTokenRepository().loadToken(req);
			r.setMessage(token.getToken());
			r.setSuccess(true);
		} catch(Exception e){
			r.setSuccess(false);
		}
		
		return r;	
	}

	
	@RequestMapping(value = "/learning-center")
	public String learningCenter(
			@AuthenticationPrincipal UserDetailsExt user, 
			ModelMap model) {
		
		if(user!=null && user.isEnabled())
			model.addAttribute("userId", user.getId());
		
		return "learning-center";		
	}

	
	@RequestMapping(value = "/test-page")
	public String testPage() {
		return "test-page";		
	}
	
	
	@RequestMapping(value = "/testemail")
	public @ResponseBody GenericResponse testemail(){
		GenericResponse r = new GenericResponse();
		r.setSuccess(true);
		
		Email email = new Email();
		email.setEmailTemplate("password-reset");
		email.setFromEmail("no-reply@luvbrite.com");
		email.setRecipientEmail("admin@codla.com");
		email.setRecipientName("Gautam Krishna");
		email.setSubject("Spring Email Simple");
		email.setEmailTitle("Password Reset Email");
		email.setEmailInfo("Info about changing your password");
		
		User user = new User();
		user.setPassword("876472364876234");
		
		email.setEmail(user);
		
		try {
			
			emailService.sendEmail(email);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return r;	
	}	
	
	
	
	@RequestMapping(value = "/contact-us")
	public String contact(@AuthenticationPrincipal 
			UserDetailsExt user, ModelMap model){	
		
		if(user!=null && user.isEnabled())
			model.addAttribute("userId", user.getId());
		
		return "contact-us";		
	}
	
	
	
	@RequestMapping(value = "/service-area")
	public String serviceArea(@AuthenticationPrincipal 
			UserDetailsExt user, ModelMap model){	
		
		if(user!=null && user.isEnabled())
			model.addAttribute("userId", user.getId());
		
		return "service-area";		
	}	
	
	
	
	@RequestMapping(value = "/localbox")
	public String locabox(@AuthenticationPrincipal 
			UserDetailsExt user, ModelMap model){	
		
		if(user!=null && user.isEnabled())
			model.addAttribute("userId", user.getId());
		
		return "localbox";		
	}
	
	
	
	@RequestMapping(value = "/generic-error")
	public String genericError(){			
		return "generic-error";		
	}
	
	
	
	@RequestMapping(value = "/403")
	public String accessDenied(@AuthenticationPrincipal 
			UserDetailsExt user, ModelMap model){	
		
		if(user!=null && user.isEnabled())
			model.addAttribute("userId", user.getId());
		
		model.addAttribute("msg", "There was some error accessing this page");
		
		return "403";		
	}
	
	
	
	@RequestMapping(value = "/not-found")
	public String accessDenied(){
		
		return "404";		
	}
	

	@RequestMapping(value = "/testemail/{templateName}")
	public String emailTemplateTest(@PathVariable String templateName, ModelMap model){	

		String template = "";
		if(templateName != null){
			template = templateName.indexOf(".html") > 0 ? 
					templateName.replace(".html", "") : templateName;


					Email email = new Email();
					email.setEmailTemplate(template);
					email.setFromEmail("");
					email.setRecipientEmail("info@luvbrite.com");
					email.setRecipientName("Luvbrite Collection");
					email.setSubject("");
					email.setEmailTitle(template + " Email");
					email.setEmailInfo("Test Info");
					
					
					if(templateName.indexOf("password") > -1){
						User user = new User();
						user.setPassword("876472364876234");

						email.setEmail(user);
					}
					
					else if(templateName.indexOf("registration-admin") > -1){
						User user = new User();
						
						user.set_id(1l);
						user.setFname("Main");
						user.setLname("Admin");
						user.setEmail("some@email.com");
						
						UserIdentity ids = new UserIdentity();
						ids.setIdCard("/user/IMG_8471-150x150.jpg");
						ids.setRecomendation("/user/IMG_8471-150x150.jpg");
						
						user.setIdentifications(ids);

						email.setEmail(user);
					}
					
					else if(templateName.indexOf("order-confirmation") > -1){
						Order order = orderDao.findOne("orderNumber", 8773);

						email.setEmail(order);
					}

					model.addAttribute("emailObj", email);		

					return "layout";

		}

		return "404";		
	}
	

	@RequestMapping(value = "/confemail/{orderNumber}")
	public String confirmationEmailTemplate(@PathVariable long orderNumber, ModelMap model){
	
		Email email = new Email();
		email.setEmailTemplate("order-confirmation");
		email.setFromEmail("");
		email.setRecipientEmail("info@luvbrite.com");
		email.setRecipientName("Luvbrite Collection");
		email.setSubject("");
		email.setEmailTitle("Order Confirmation Email");
		email.setEmailInfo("Test Info");
		Order order = orderDao.findOne("orderNumber", orderNumber);

		email.setEmail(order);

		model.addAttribute("emailObj", email);		

		return "layout";
	}
	


	@Autowired
	private CouponDAO couponDao;

	@RequestMapping(value = "/surveymonkey/coupon/1")
	public String generateCouponForSurvey(HttpSession sess, ModelMap model){
	
		try {
			
			//Check if a coupon was already generated for this session.
			if(sess != null && sess.getAttribute("surveycoupon") != null){
				String existingCoupon = (String) sess.getAttribute("surveycoupon");
				
				if(couponDao.get(existingCoupon) != null){
					
					return "redirect:/surveymonkey/showcoupon/" + existingCoupon;
				}
			}
			

			boolean createCoupon = true,
					proceed = false;
			
			int safetyCounter = 0;
			
			String newCouponCode = "";
			
			while(createCoupon){

				newCouponCode = ("freejoint-" + CouponGen.getNewCoupon(7)).toLowerCase();
				if(couponDao.get(newCouponCode) == null){
					createCoupon = false;
					proceed = true;
				}
				else{
					safetyCounter++;
				}
				
				if(safetyCounter > 20) createCoupon = false; 
					
			}
			
			
			if(proceed){
				
				Coupon coupon = new Coupon();
				coupon.set_id(newCouponCode);
				coupon.setActive(true);
				coupon.setCouponValue(10d);
				coupon.setDescription("jointpromo");
				
				Calendar now = Calendar.getInstance();
				now.add(Calendar.MONTH, 1);
				coupon.setExpiry(now.getTime());
				
				coupon.setMaxUsageCount(1);
				
				List<Long> pids = new ArrayList<Long>();
				pids.add(11872l);
				coupon.setPids(pids);
				
				coupon.setType("PO");
				coupon.setUsageCount(0);
				
				sess.setAttribute("surveycoupon", newCouponCode);
				
				couponDao.save(coupon);
				
				return "redirect:/surveymonkey/showcoupon/" + newCouponCode;
			}
			
			else {

				model.addAttribute("error", "There was some erorr creating the coupon. Please contact customer care.");
			}
			
			
		} catch (Exception e){
			e.printStackTrace();
		}

		return "404";
	}

	@RequestMapping(value = "/surveymonkey/showcoupon/{couponCode}")
	public String showCouponForSurvey(@PathVariable String couponCode, 
			@AuthenticationPrincipal UserDetailsExt user, ModelMap model){
		
		if(user!=null && user.isEnabled())
			model.addAttribute("userId", user.getId());
		
		Coupon cp = couponDao.get(couponCode);
		if(cp != null){
			
			model.addAttribute("cp", cp);
		}
		else{

			model.addAttribute("error", couponCode + " is not a valid coupon code.");
		}
		
		return "show-coupon";
	}
	
	
	
	@Autowired
	private InOfficeOrderLogic iool;

	@RequestMapping(value = "/extapi/json/inofficepurchases/create", method = RequestMethod.POST)
	public @ResponseBody GenericResponse 
		inOfficePurchaseCreate(@RequestBody OrderMain orderMain){
		
		GenericResponse gr = new GenericResponse();
		gr.setSuccess(false);
		String resp = iool.create(orderMain);
		if(resp.equals("")){
			gr.setSuccess(true);
			gr.setMessage("" + iool.getOrderNumber());
		}
		
		else{
			gr.setMessage(resp);
		}
		
		return gr;
	}
	
}
