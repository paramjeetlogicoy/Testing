package com.luvbrite.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

import com.luvbrite.config.db.MongoConfig;

@EnableWebMvc
@Configuration
@ComponentScan("com.luvbrite")
@Import ({MongoConfig.class, SpringSecurityConfig.class})
public class SpringWebConfig extends WebMvcConfigurerAdapter {
	 
		@Override
		public void addResourceHandlers(ResourceHandlerRegistry registry) {
			registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
		}

		@Bean
		public ResourceBundleMessageSource messageSource() {
			ResourceBundleMessageSource rb = new ResourceBundleMessageSource();
			rb.setBasenames(new String[] { "messages/messages", "messages/validation" });
			return rb;
		}


	    @Bean
	    public TemplateResolver templateResolver(){
	        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver();
	        templateResolver.setPrefix("/WEB-INF/templates/");
	        templateResolver.setSuffix(".html");
	        templateResolver.setTemplateMode("HTML5");
	        templateResolver.setCacheable(false);

	        return templateResolver;
	    }

	    @Bean
	    public SpringTemplateEngine templateEngine(){
	        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
	        templateEngine.setTemplateResolver(templateResolver());
	        return templateEngine;
	    }

	    @Bean
	    public ViewResolver viewResolver(){
	        ThymeleafViewResolver viewResolver = new ThymeleafViewResolver() ;
	        viewResolver.setTemplateEngine(templateEngine());
	        //viewResolver.setCache(false);
	        viewResolver.setOrder(1);

	        return viewResolver;
	    }
	    
	    @Bean(name = "multipartResolver")
	    public CommonsMultipartResolver getCommonsMultipartResolver() {
	        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
	        multipartResolver.setMaxUploadSize(20971520);   // 20MB
	        multipartResolver.setMaxInMemorySize(1048576);  // 1MB
	        return multipartResolver;
	    }
}
