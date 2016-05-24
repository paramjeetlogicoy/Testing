package com.luvbrite.web.core;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.zaxxer.hikari.HikariDataSource;

public class Users {
	
	public String process(HikariDataSource ds, MongoDatabase mDB){
		
		String response = "";
		Connection tcon = null;
		int writeCounter = 0,
				readCounter = 0;
		
		try {
			

		
			MongoCollection<Document> users = mDB.getCollection("users");
		
			//Delete any existing collection.
			users.drop();			
			
			//Read the POST from MySQL
			tcon = ds.getConnection();
			ResultSet rs = tcon.createStatement().executeQuery("SELECT * FROM fg_users ORDER BY ID");
			while(rs.next()){
				readCounter++;


				//Convert applicable document elements	
				Date regDate = rs.getDate("user_registered");
				long time = regDate.getTime();
				
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(time);
				
				
				
				//Insert into Mongo
				Document user = new Document()
					.append("_id", rs.getInt("id"))
					.append("username", rs.getString("user_login"))
					.append("password", rs.getString("user_pass"))
					.append("dateRegistered", cal.getTime())
					.append("email", rs.getString("user_email"));
				
				Document billing = new Document();
				Document recommendation = new Document();
				
				ArrayList<Document> identifications = new ArrayList<Document>(); 
				ArrayList<Document> marketing = new ArrayList<Document>(); 
				
				
				SimpleDateFormat sd = new SimpleDateFormat("MM/dd/YYYY");
				
				
				ResultSet rs0 = tcon.createStatement().executeQuery("SELECT * FROM `fg_usermeta` WHERE user_id = " + rs.getInt("id"));
				while(rs0.next()){
					String metaKey = rs0.getString("meta_key"),
							metaValue = rs0.getString("meta_value");
					
					if(metaKey.equals("additional_notes") 
							&& metaValue.trim().length()>0){						
						user.append("notes", metaValue);	
					}
					
					
					else if(metaKey.equals("billing_first_name")){						
						billing.append("firstName", metaValue);
					}					
					else if(metaKey.equals("billing_last_name")){						
						billing.append("lastName", metaValue);
					}
					else if(metaKey.equals("billing_company") 
							&& metaValue.trim().length()>0){						
						billing.append("company", metaValue);
					}
					else if(metaKey.equals("billing_phone")){						
						billing.append("phone", metaValue);
					}
					else if(metaKey.equals("billing_address_1")){						
						billing.append("address_1", metaValue);
					}
					else if(metaKey.equals("billing_address_2") 
							&& metaValue.trim().length()>0){						
						billing.append("address_2", metaValue);
					}
					else if(metaKey.equals("billing_city")){						
						billing.append("city", metaValue);
					}
					else if(metaKey.equals("billing_state")){						
						billing.append("state", metaValue);
					}
					else if(metaKey.equals("billing_postcode")){						
						billing.append("zipcode", metaValue);
					}
					
					
					else if(metaKey.equals("dob")  
							&& metaValue.trim().length()>0){
						user.append("dob", sd.parse(metaValue));
					}
					
					else if(metaKey.equals("first_name")){
						user.append("firstName", metaValue);						
					}
					
					else if(metaKey.equals("last_name")){
						user.append("lastName", metaValue);						
					}
					
					else if(metaKey.equals("gender") 
							&& metaValue.trim().length()>0){
						user.append("gender", metaValue);						
					}
					
					else if(metaKey.equals("phone_number") 
							&& metaValue.trim().length()>0){
						user.append("phoneNumber", metaValue);						
					}
					
					else if(metaKey.equals("fg_user_level")){
						if(metaValue.equals("10"))
							user.append("role", "admin");
						else
							user.append("role","customer");
					}
					
					else if(metaKey.equals("user_meta_user_status")){
						if(metaValue.equals("active"))
							user.append("active", true);
						else
							user.append("active", false);
					}
					
					else if(metaKey.equals("recom_expire_date")
							&& metaValue.trim().length()== 10){
						recommendation.append("recoExpiry",sd.parse(metaValue));						
					}					
					else if(metaKey.equals("your_doctors_medical_recommendation_letter")
							&& metaValue.trim().length() > 0){
						recommendation.append("recomendation", metaValue);						
					}
					
					
					else if(metaKey.equals("your_identification_card_drivers_license")
							&& metaValue.trim().length() > 0){
						
						identifications.add(new Document().append("driversLicense", metaValue));						
					}
					
					
					else if(metaKey.equals("_order_count")
							&& metaValue.trim().length() > 0){
						user.append("orderCount", Integer.valueOf(metaValue).intValue());						
					}
					
					else if(metaKey.equals("_money_spent")
							&& metaValue.trim().length() > 0){
						user.append("moneySpent", Double.valueOf(metaValue).doubleValue());
					}
					
					else if(metaKey.equals("how_did_you_hear_about_us")
							&& metaValue.trim().length() > 0){
						
						marketing.add(new Document().append("hearAboutUs", metaValue));
					}
					
					
					
					
				}
				rs0.close();
				
				identifications.add(recommendation);
				
				user.append("billing", billing);
				
				if(identifications.size()>0)
					user.append("identifications", identifications);
				
				if(marketing.size()>0)
					user.append("marketing", marketing);
				
				
				users.insertOne(user);
				writeCounter++;
			}	
			
			rs.close();
			tcon.close();
			

			System.out.println("Out and done");
			
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("in Exception");
			
			response = "Some exception during the operation. " + e.getLocalizedMessage();
		}
		
		finally{
			try{if(tcon!=null){tcon.close();tcon=null;}}catch(Exception e){}			
		}
		
		return response + ". Records read:" + readCounter + ". Records Written: " + writeCounter + ".";
	}	

}
