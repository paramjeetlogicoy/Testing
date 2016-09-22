package com.luvbrite.config.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import com.zaxxer.hikari.HikariDataSource;


@Configuration
@PropertySource("classpath:/env.properties")
public class PostgresConfig {

	@Autowired
	private Environment env;
	
	@Bean
	public HikariDataSource postgres(){

		String pgurl = env.getProperty("pghost");
		String pgdbuser = env.getProperty("pgdbuser");
		String pgpassword = env.getProperty("pgpassword");
		
		HikariDataSource datasource = new HikariDataSource();
		datasource.setJdbcUrl(pgurl);
		datasource.setUsername(pgdbuser);
		datasource.setPassword(pgpassword);
		
		return datasource;
	}

}
