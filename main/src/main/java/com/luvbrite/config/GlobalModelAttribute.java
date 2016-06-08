package com.luvbrite.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@PropertySource("classpath:/env.properties")
public class GlobalModelAttribute {
	
	@Autowired
	private Environment env;
	
    
    @ModelAttribute
    public void populateGlobalCDN(Model model){
        model.addAttribute("_lbGlobalCDNPath", env.getProperty("cdnPath"));
    }
}
