package com.luvbrite.web.controller;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.luvbrite.dao.ArticleDAO;
import com.luvbrite.web.models.Article;
import com.luvbrite.web.models.UserDetailsExt;


@Controller
@RequestMapping(value = "/article")
public class ArticleController {

	@Autowired
	private ArticleDAO dao;
	
	@RequestMapping(value = "/{permalink}")
	public String articles(
			@AuthenticationPrincipal UserDetailsExt user, 
			@PathVariable String permalink, 
			ModelMap model) {
		
		if(user!=null){
			
			Set<String> authorities = AuthorityUtils.authorityListToSet(user.getAuthorities());
			if (authorities.contains("ROLE_EDIT")){
				model.addAttribute("adminEditable", true);
			}
				
			
			if(user.isEnabled()){
				model.addAttribute("userId", user.getId());
			}
		}
		
		if(permalink != null){
			
			Article article = dao.createQuery()
					.field("permalink").equal(permalink)
					.field("status").equal("publish")
					.get();
			
			if(article != null){
				model.addAttribute("a", article);
				return "article";
			}

			return "404";
		}
		else {
			return "404";
		}
	}	
	
}
