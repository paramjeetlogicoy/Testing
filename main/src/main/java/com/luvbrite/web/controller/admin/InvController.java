package com.luvbrite.web.controller.admin;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.luvbrite.web.models.UserDetailsExt;


@Controller
@RequestMapping(value = "/admin/inv")
public class InvController {	
	
	@RequestMapping(method = RequestMethod.GET)
	public String mainPage(@AuthenticationPrincipal 
			UserDetailsExt user, ModelMap model){	
		
		model.addAttribute("userId", user.getId());
		
		return "admin/inv/main";		
	}
}
