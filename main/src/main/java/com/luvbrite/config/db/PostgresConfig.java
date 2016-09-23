package com.luvbrite.config.db;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;


@Configuration
@PropertySource("classpath:/env.properties")
public class PostgresConfig {

	@Autowired
	private Environment env;
	
	@Bean
	public HikariDataSource postgres(){

		String pgdb = env.getProperty("pgdb");
		String pghost = env.getProperty("pghost");
		String pguser = env.getProperty("pguser");
		String pgpassword = env.getProperty("pgpassword");
		
		Properties props = new Properties();
		props.setProperty("dataSourceClassName", "org.postgresql.ds.PGSimpleDataSource");
		props.setProperty("dataSource.user", pguser);
		props.setProperty("dataSource.password", pgpassword);
		props.setProperty("dataSource.databaseName", pgdb);
		props.setProperty("dataSource.serverName", pghost);

		HikariConfig config = new HikariConfig(props);
		
		HikariDataSource datasource = new HikariDataSource(config);
		
		return datasource;
	}

}
