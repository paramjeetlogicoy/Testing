package com.luvbrite.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import com.luvbrite.dao.ControlRecordDAO;
import com.luvbrite.services.ControlConfigService;
import com.luvbrite.web.models.ModifiedDate;


@Configuration
@PropertySource("classpath:/env.properties")
public class ControlConfig {
	
	@Autowired
	private Environment env;
	
	@Autowired
	private ControlRecordDAO dao;
	
	@Bean(name="ControlConfigService")
	public ControlConfigService configureControl(){
		
		ControlConfigService ccs = new ControlConfigService(env, dao);
		
		System.out.println("$$$ ControlConfigService called");
		
		return ccs;
	}	
	
	@Bean(name="modifiedDate")
	public ModifiedDate md(){
		return configureControl().modifiedDates();
	}
}
