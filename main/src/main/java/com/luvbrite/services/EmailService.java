package com.luvbrite.services;

import javax.mail.MessagingException;
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
    
	public void sendEmail(Email email) throws MessagingException{
		
		final Context ctx = new Context(LocaleContextHolder.getLocale());
        ctx.setVariable("name", email.getRecipientName());
        
        // Prepare message using a Spring helper
        final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
        final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
        message.setSubject(email.getSubject());
        message.setFrom(email.getFromEmail());
        message.setTo(email.getRecipientEmail());

        // Create the HTML body using Thymeleaf
        final String emailContent = this.templateEngine.process(email.getEmailTemplate(), ctx);
        message.setText(emailContent, true);
        
        // Send email
        this.mailSender.send(mimeMessage);
		
	}

}
