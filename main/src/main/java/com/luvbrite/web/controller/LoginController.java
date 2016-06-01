package com.luvbrite.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class LoginController {

	@RequestMapping(value = "/login")
	public ModelAndView login(
			
			@RequestParam(value = "error", required = false) String error,
			@RequestParam(value = "ret", required = false) String returnURL,
			@RequestParam(value = "logout", required = false) String logout) {

			ModelAndView model = new ModelAndView();
			
			if (error != null) {
				model.addObject("error", "Invalid username and password!");
			}

			if (logout != null) {
				model.addObject("msg", "You've been logged out successfully.");
			}


			if (returnURL != null) {
				model.addObject("ret", returnURL);
			}
			
			model.setViewName("login");

			return model;	
	}
	

	@RequestMapping(value = "/register")
	public String register(){
		return "register";
	}
}
