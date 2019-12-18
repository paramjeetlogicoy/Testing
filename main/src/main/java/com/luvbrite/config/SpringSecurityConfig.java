package com.luvbrite.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

import com.luvbrite.components.MongoDBTokenRepository;
import com.luvbrite.security.CsrfHeaderFilter;
import com.luvbrite.security.DBAuthProvider;
import com.luvbrite.security.LBAuthFailureHandler;
import com.luvbrite.security.LBAuthSuccessHandler;
import com.luvbrite.services.UserDetailsServiceImp;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private DBAuthProvider authenticationProvider;
	
	@Autowired
	private LBAuthSuccessHandler successHandler;
	
	@Autowired
	private LBAuthFailureHandler failureHandler;
	
	@Autowired
	private UserDetailsServiceImp userDetailsService;
	
	@Autowired
	private MongoDBTokenRepository mongoDBTokenRepository;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.authorizeRequests()
				.antMatchers("/admin/**").hasRole("ADMIN")
				.antMatchers("**/Test/**", "/Test").hasRole("ADMIN")
				//.antMatchers("/updateproductStockStatus/**").hasRole("ADMIN")
				.antMatchers("/customer/**", "/checkout")
				.authenticated()
			.and()
			
			.exceptionHandling().accessDeniedPage("/403")
			.and()
			
			.formLogin()
				.loginPage("/login")
				.failureUrl("/login?error")
				.successHandler(successHandler)
				.failureHandler(failureHandler)
			    .usernameParameter("username")
			    .passwordParameter("password")		
			.and()
			
			.rememberMe()
				.rememberMeParameter("rememberme")
				.tokenRepository(mongoDBTokenRepository)
				.tokenValiditySeconds(30 * 24 * 3600)  //30 days
			.and()
				
			
			
			.csrf().disable()
				.csrf().csrfTokenRepository(csrfTokenRepository())
			.and()
			.addFilterAfter(new CsrfHeaderFilter(), CsrfFilter.class)
			
			.logout()
			.logoutSuccessUrl("/login?logout");
		
	}
	
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth
		.authenticationProvider(authenticationProvider)
		.userDetailsService(userDetailsService);
	}

	private CsrfTokenRepository csrfTokenRepository() {
		HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
		repository.setHeaderName("X-XSRF-TOKEN");
		return repository;
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
	    web.ignoring().antMatchers("/updateproductStockStatus/**");
	    
        }
//       @Override
//	public void configure(WebSecurity web) throws Exception {
//	    web.ignoring().antMatchers("/testTookanWebhook/**");
//	}

}
