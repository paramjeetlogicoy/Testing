package com.luvbrite.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.luvbrite.utils.Utility;


@Configuration
@PropertySource("classpath:/env.properties")
public class MailConfig {
	
	@Autowired
	private Environment env;
	
	@Bean
	public JavaMailSender javaMailSenderImpl(){
		
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		
		mailSender.setHost(env.getProperty("mailHost"));	
		mailSender.setPort(Utility.getInteger(env.getProperty("mailPort")));
		
		mailSender.setUsername(env.getProperty("mailUser"));
		mailSender.setPassword(env.getProperty("mailPass"));
		
		Properties prop = mailSender.getJavaMailProperties();
		prop.put("mail.smtp.auth", "true");
		prop.put("mail.smtp.starttls.enable", "true");
		prop.put("mail.smtp.quitwait", "false");
		prop.put("mail.transport.protocol", "smtp");
		//prop.put("mail.debug", "true");	
		
		return mailSender;
	}
}
