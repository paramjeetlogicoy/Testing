package com.luvbrite.services;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.luvbrite.web.models.Email;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired 
    private TemplateEngine templateEngine;
    
	public void sendEmail(Email email) throws MessagingException, UnsupportedEncodingException{
		
		final Context ctx = new Context(LocaleContextHolder.getLocale());
        ctx.setVariable("emailObj", email);
        
        // Prepare message using a Spring helper
        final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
        final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
        message.setSubject(email.getSubject());
       
        if(email.getFromEmail() != null && !email.getFromEmail().equals("")){
            message.setFrom(new InternetAddress(email.getFromEmail(), email.getFromName()));
        }
        else{
            message.setFrom(new InternetAddress("no-reply@luvbrite.com", "Luvbrite Collective"));        	
        }
        
        
        message.setReplyTo("support@luvbrite.com");
        message.setTo(email.getRecipientEmail());
        
        List<String> bccs = email.getBccs();
        if(bccs == null || bccs.size() <= 0){
        	bccs = new ArrayList<String>();
        	bccs.add("updates@luvbrite.com");
        }
        else {
        	bccs.add("updates@luvbrite.com");
        }
        
    	String[] array1 = new String[bccs.size()];
    	array1 = bccs.toArray(array1);
    	message.setBcc(array1);
        
        List<String> ccs = email.getCcs();
        if(ccs !=null && ccs.size()>0){
        	String[] array2 = new String[ccs.size()];
        	array2 = bccs.toArray(array2);
        	message.setCc(array2);
        }

        // Create the HTML body using Thymeleaf
        final String emailContent = this.templateEngine.process("layout", ctx);
        message.setText(emailContent, true);
        
        // Send email
        this.mailSender.send(mimeMessage);
        
		
		System.out.println("EmailService - Email Sent");
		//System.out.println(emailContent);
		
	}

}
