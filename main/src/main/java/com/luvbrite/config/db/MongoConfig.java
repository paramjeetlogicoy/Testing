package com.luvbrite.config.db;

import java.util.Arrays;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import com.luvbrite.utils.Utility;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

@Configuration
@PropertySource("classpath:/env.properties")
public class MongoConfig {

    @Autowired
    private Environment env;

    @Bean
    public Datastore morphia() {
        
        String dbname = env.getProperty("dbname");
        String dbuser = env.getProperty("dbuser");
        String password = env.getProperty("password");
        //System.out.println(dbname + " - " + dbuser + " - " + password);

        String server1 = env.getProperty("server1");
        int port1 = Utility.getInteger(env.getProperty("port1"));

      /*  String server2 = env.getProperty("server2");
        int port2 = Utility.getInteger(env.getProperty("port2"));

        String server3 = env.getProperty("server3");
        int port3 = Utility.getInteger(env.getProperty("port3"));
*/
        MongoCredential credential = MongoCredential
                .createCredential(dbuser, dbname, password.toCharArray());

        MongoClient client = new MongoClient(
                Arrays.asList(
                        new ServerAddress(server1, port1)                        
//                       ,new ServerAddress(server2, port2),
//                        new ServerAddress(server3, port3)
                ),
                Arrays.asList(credential)
        );

        final Morphia morphia = new Morphia();
		// tell Morphia where to find your classes
        // can be called multiple times with different packages or classes
        morphia.mapPackage("com.luvbrite.web.models");

        final Datastore datastore = morphia.createDatastore(client, dbname);

        return datastore;
    }

}
