package com.luvbrite.services.paymentgateways;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luvbrite.dao.SquareUpResponseDAO;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.web.models.GenericResponse;
import com.luvbrite.web.models.squareup.Charge;
import com.luvbrite.web.models.squareup.Response;
import com.luvbrite.web.models.squareup.ResponseError;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;

@Service
@PropertySource("classpath:/env.properties")
public class SquareUp {
	
	private static Logger logger = LoggerFactory.getLogger(SquareUp.class);
			
	@Autowired
	private Environment env;
	
	@Autowired
	private SquareUpResponseDAO dao;
	
	final private String paymentURL = "https://connect.squareup.com/v2/locations/{locationId}/transactions";
	
	public GenericResponse processPayment(Charge body){
		
		String response = "";
		String locationId = env.getProperty("squareUpLocationId");
		String accessToken = env.getProperty("squareUpAccessToken");
		
		GenericResponse gr = new GenericResponse();
		gr.setSuccess(false);
		gr.setMessage("");
		
		try {
			
			ObjectMapper mapper = new ObjectMapper();

			HttpResponse<JsonNode> postResponse = Unirest.post(paymentURL)
					.routeParam("locationId", locationId)

					.header("accept", "application/json")
					.header("Content-Type", "application/json")
					.header("Authorization", "Bearer " + accessToken)

					.body(mapper.writeValueAsString(body))

					.asJson();

			Response r = mapper.readValue(postResponse.getBody().toString(), Response.class);
			//Save response into DB
			dao.save(r);

			if(postResponse.getStatus() == 200){	
				gr.setSuccess(true);
				response = r.getTransaction().getId();	
			}
			else{

				List<ResponseError> errors = r.getErrors();
				if(errors!=null){
					for(ResponseError error: errors){
						if(error.getCode().equals("CARD_TOKEN_EXPIRED") 
								|| error.getCode().equals("CARD_TOKEN_USED")){
							
							gr.setMessage("retry");
							return gr;
						}
						response+= (error.getDetail() + " ");
					}
				}
			}
			
		}catch(Exception e){
			response = "Error - " + e.getMessage();
			logger.error(Exceptions.giveStackTrace(e));
		}
		
		gr.setMessage(response);
		return gr;
	}
}
