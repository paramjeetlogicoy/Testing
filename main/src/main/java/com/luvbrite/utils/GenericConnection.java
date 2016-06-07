package com.luvbrite.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class GenericConnection {

	public GenericConnection() {}

	public String contactService(String inputString, URL url, boolean SysOut) throws Exception{		
		String outputStr = null;
		OutputStream outputStream = null;

		try {

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			System.out.println("Client established connection with " + url.toString());
			if(SysOut)
				System.out.println(inputString);
			
			// Setup HTTP POST parameters
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setUseCaches(false);
			connection.setRequestMethod("POST");
			
			if(url.toString().indexOf("/xml") > -1)
				connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
			
			else if(url.toString().indexOf("json") > -1)
				connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");


			outputStream = connection.getOutputStream();		
			outputStream.write(inputString.getBytes());
			outputStream.flush();
			outputStream.close();

			outputStr = readURLConnection(connection);	
			if(SysOut)
				System.out.println(outputStr);
			
		} catch (Exception e) {
			System.out.println("Error sending data to server");
			e.printStackTrace();
		} finally {						
			if(outputStream != null){
				outputStream.close();
				outputStream = null;
			}
		}		
		return outputStr;
	}

	/**
	 * This method read all of the data from a URL connection to a String
	 */

	private String readURLConnection(HttpURLConnection uc) throws Exception {
		StringBuffer buffer = new StringBuffer();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			String response = "";			
			while ((response = reader.readLine()) != null){
				buffer.append(response);
			}
			reader.close();
		} catch (Exception e) {
			System.out.println("Could not read from URL: " + e.toString());
			e.printStackTrace();
			return "Error getting response";
		} finally {
			if(reader != null){
				reader.close();
				reader = null;
			}
		}
		return buffer.toString();
	}
}