package com.luvbrite.config.db;

import java.util.Arrays;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;


@Configuration
public class MongoConfig {
	
/*	@Bean
	public MongoDatabase mongodb(){
		
		MongoDatabase database = null;
		
		MongoCredential credential = MongoCredential.createCredential("smoker", "flyinguru", "What@High290".toCharArray());
		
		MongoClient client = new MongoClient(
							Arrays.asList(
								new ServerAddress("krishna", 27017),
								new ServerAddress("krishna", 27027),
								new ServerAddress("krishna", 27037)
							),
							Arrays.asList(credential)
							);
		
		database = client.getDatabase("flyinguru");
				
		
		return database;
	}*/
	
	@Bean
	public Datastore morphia(){
		
		MongoCredential credential = MongoCredential.createCredential("smoker", "flyinguru", "What@High290".toCharArray());
		
		MongoClient client = new MongoClient(
							Arrays.asList(
								new ServerAddress("krishna", 27017),
								new ServerAddress("krishna", 27027),
								new ServerAddress("krishna", 27037)
							),
							Arrays.asList(credential)
							);
		
		
		final Morphia morphia = new Morphia();
		// tell Morphia where to find your classes
		// can be called multiple times with different packages or classes
		morphia.mapPackage("com.luvbrite.web.models");
		
		final Datastore datastore = morphia.createDatastore(client, "flyinguru");
		
		return datastore;
	}

}
