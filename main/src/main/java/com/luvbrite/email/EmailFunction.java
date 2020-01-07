package com.luvbrite.email;

import java.util.ArrayList;
import org.apache.log4j.Logger;


import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.resource.Email;

public class EmailFunction {	
	
    static Logger logger = Logger.getLogger(EmailFunction.class);

	private final String MAILJET_API_KEY = "2a61d967f5d0d2fc06f90b875ab01910";
	private final String MAILJET_API_SECRET = "97b64359e8556e4413a1d41f33a6dbbc";
   
	

	
	public boolean email(ArrayList<String>lTo, String replyTo, String strSubject, String strMessage, String strXMailer) {
		
		try {
			
			if (lTo.size() <= 0) {
				System.out.println("No email recipients");
				return false;
			}
			
			MailjetRequest mailJet = new MailjetRequest(Email.resource);
			mailJet
			.property(Email.TO, lTo.get(0))
			.property(Email.SUBJECT, strSubject)
			.property(Email.HTMLPART, strMessage)
			.property(Email.FROMEMAIL, "no-reply@luvbrite.com");      	

			try {

				MailjetClient client = new MailjetClient(MAILJET_API_KEY, MAILJET_API_SECRET);

				// trigger the API call
				MailjetResponse response = client.post(mailJet);
				if(response.getStatus() == 200){
					System.out.println("EmailService - Email Sent to " + lTo.get(0));
				}
				else{
					logger.error("Error sending text to " 
							+ lTo.get(0) + ". Error Code " 
							+ response.getStatus() + ". " 
							+ response.getData());
				}

			} catch (Exception e) {
				
				logger.error("$$Exception occred in EmailFunction", e);
				
			}
			
			return true;
			
		} catch (Exception E) {
			logger.error("$$Exception occred in EmailFunction",E);
			return false;
		}

	}
}