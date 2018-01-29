package com.luvbrite.services;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.mail.MessagingException;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.luvbrite.utils.Exceptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.resource.Email;

@Service
public class EmailService {

	@Autowired 
	private TemplateEngine templateEngine;

	private final String MAILJET_API_KEY = "2a61d967f5d0d2fc06f90b875ab01910";
	private final String MAILJET_API_SECRET = "97b64359e8556e4413a1d41f33a6dbbc";
	private static Logger logger = Logger.getLogger(EmailService.class);

	public void sendEmail(com.luvbrite.web.models.Email email) throws MessagingException, UnsupportedEncodingException{

		final Context ctx = new Context(LocaleContextHolder.getLocale());
		ctx.setVariable("emailObj", email);

		// Create the HTML body using Thymeleaf
		final String emailContent = this.templateEngine.process("layout", ctx);

		String CCs = "";
		String BCCs = "";
		
		//Create the Comma separated string for CC emails
		List<String> ccs = email.getCcs();
		if(ccs !=null && ccs.size()>0){
			CCs = StringUtils.join(ccs, ",");
		}

		//Create the JSONArray for BCC emails
		List<String> bccs = email.getBccs();
		if(bccs !=null && bccs.size()>0){
			BCCs = StringUtils.join(bccs, ",");
			BCCs+= ",luvbrite-emails@codla.com";
		}
		else {
			BCCs = "luvbrite-emails@codla.com";
		}

		MailjetRequest mailJet = new MailjetRequest(Email.resource);
		mailJet
		.property(Email.HEADERS, new JSONObject().put("Reply-To", "support@luvbrite.com"))
		.property(Email.TO, email.getRecipientEmail())
		.property(Email.BCC, BCCs)
		.property(Email.SUBJECT, email.getSubject())
		.property(Email.HTMLPART, emailContent);	

		if(CCs.length()!=0) mailJet.property(Email.CC, CCs);

		if(email.getFromEmail() != null && !email.getFromEmail().equals("")){
			mailJet
			.property(Email.FROMEMAIL, email.getFromEmail())
			.property(Email.FROMNAME, email.getFromName());
		}
		else {
			mailJet
			.property(Email.FROMEMAIL, "no-reply@luvbrite.com")
			.property(Email.FROMNAME, "Luvbrite Collective");       	
		}


		try {

			MailjetClient client = new MailjetClient(MAILJET_API_KEY, MAILJET_API_SECRET);

			//System.out.println("mailjet - " + mailJet);
			// trigger the API call
			MailjetResponse response = client.post(mailJet);
			//System.out.println("responseStatus - " + response.getStatus());
			//System.out.println("responseData - " + response.getData());
			if(response.getStatus() == 200){
				System.out.println("EmailService - Email Sent to " + email.getRecipientEmail());
			}
			else{
				logger.error("Error sending email to " 
						+ email.getRecipientEmail() + ". Error Code " 
						+ response.getStatus() + ". " 
						+ response.getData());
			}

		} catch (Exception e) {
			Exceptions.giveStackTrace(e);
		}

		//System.out.println(emailContent);		
	}
}
