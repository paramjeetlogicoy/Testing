package com.luvbrite.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.luvbrite.email.Email;
import com.luvbrite.email.EmailFunction;
import com.models.web.tookan.TrackingLinkEmailInfo;


public class TrackingLinkToCustomer {
	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(TrackingLinkToCustomer.class);

    public static boolean send(TrackingLinkEmailInfo trackingLinkEmailInfo, String recipentemail) {
        boolean isTrackingLinkSent = false;
        
       

        if (trackingLinkEmailInfo==null) {
            logger.error("Cannot send tracking link mail  becasue trackingLinkEmailInfo Object is null");
            return false;
        } else if (StringUtils.isEmpty(recipentemail)) {
            logger.error("Cannot send tracking link mail  because recipent mail is empty ");
            return false;
        }       


        try {

            EmailFunction emailFunction = new EmailFunction();
            Email email = new Email();
            email.setEmailTemplate("order-confirmation");
            email.setFromName("Luvbrite Orders");
            email.setFromEmail("no-reply@luvbrite.com");
            email.setRecipientName(recipentemail);

            boolean isDev = false;
            if (isDev) {
                email.setRecipientEmail("sumiit.prashant.june@gmail.com");
            } else {
    	
            	email.setRecipientEmail(recipentemail);
                email.setBccs(Arrays.asList(new String[]{"orders-notify@luvbrite.com"}));
    
            }

            email.setEmailTitle("Tookan Order Tracking Email");
            email.setSubject("Tookan Driver TrackingLink");
            email.setEmailInfo("Your order with Luvbrite.");

            email.setEmail("tracking link");
            ArrayList<String> emailRecipentList = new ArrayList<String>();

            
            emailRecipentList.add(trackingLinkEmailInfo.getRecipentEmail());
            String mailBody_html = replacePlaceHolderInEmailContentWithOrderInfo(trackingLinkEmailInfo);

            String emailSubject = "LuvbriteOrder#" + trackingLinkEmailInfo.getOrderNumber() + " Tracking Link";

        isTrackingLinkSent =  emailFunction.email(emailRecipentList, "", emailSubject, mailBody_html, "");
    
        } catch (Exception e) {
             logger.error("$$Exception occured while sending mail to customer about tracking link : ",e);
             isTrackingLinkSent = false;
        }

        return isTrackingLinkSent;
    }

    private static HashMap<String, String> getMapOfTrackingLinkInfo(TrackingLinkEmailInfo trackingLinkEmail) {
        HashMap<String, String> hm = new HashMap<String, String>();

        hm.put("${ORDERDATE}", trackingLinkEmail.getOrderDate());
        hm.put("${ORDERTOTAL}", trackingLinkEmail.getOrderTotal());
        hm.put("${ORDERNUMBER}", trackingLinkEmail.getOrderNumber());
        hm.put("${TRACKINGLINK}", trackingLinkEmail.getTrackingLink());
        hm.put("${RECIPENTNAME}", trackingLinkEmail.getRecipentName()); 
        hm.put("${RECIPENTEMAIL}", trackingLinkEmail.getRecipentEmail());

        return hm;

    }

    private static String readInputStreamThroughBufferedReader(InputStream is) {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String line = null;
        StringBuffer sb = new StringBuffer();

        try {

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            logger.error("Exception occured while reading patientRequestXML", e);
            return e.getMessage();
        }

        return sb.toString();

        // return null;
    }

    public static String replacePlaceHolderInEmailContentWithOrderInfo(TrackingLinkEmailInfo trackinglinkemail) {

        HashMap<String, String> hm = getMapOfTrackingLinkInfo(trackinglinkemail);
        String emailContent = getTrackingLinkEmailContentFromResources();

        for (Map.Entry<String, String> entry : hm.entrySet()) {
            emailContent = emailContent.replace(entry.getKey(),
                    entry.getValue() != null ? entry.getValue() : "");
        }

        return emailContent;
    }

    private static String getTrackingLinkEmailContentFromResources() {
        InputStream is = TrackingLinkToCustomer.class.getResourceAsStream("trackinglink-email.html");
        String emailContent = readInputStreamThroughBufferedReader(is);
        return emailContent;
    }

    private static void writeUsingFiles(String data) {
        try {
            Files.write(Paths.get("C:\\Users\\dell\\Desktop\\emailTemplate\\emailTemplate.txt"), data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
