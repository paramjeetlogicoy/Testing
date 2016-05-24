package com.luvbrite.utils;

import java.net.URL;
import java.net.URLEncoder;


public class OldHashEncoder  {
	
	private GenericConnection conn = new GenericConnection();
	
	private static final String postURL = "https://www.luvbrite.com/auth.php";
	
	public boolean isValid(String username, String rawPwd){
		
		boolean userValid = false;
		
		try {
			
			String data = "un=" + URLEncoder.encode(username, "UTF-8");
			data = data + "&pd=" + URLEncoder.encode(rawPwd, "UTF-8");
			
			URL url = new URL(postURL);
			String response = conn.contactService(data, url, true);
			
			if(response.trim().equalsIgnoreCase("success"))
				userValid = true;
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return userValid;		
		
	}

}

