package com.luvbrite.config;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

@EnableWebMvc
@Configuration
@ComponentScan("com.luvbrite")
public class SpringWebConfig extends WebMvcConfigurerAdapter {
    	
		@Autowired
    	private ApplicationContext applicationContext;
    
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
	    	
	    	SpringResourceTemplateResolver  templateResolver = new SpringResourceTemplateResolver ();
	        templateResolver.setApplicationContext(this.applicationContext);
	        templateResolver.setPrefix("/WEB-INF/templates/");
	        templateResolver.setSuffix(".html");
	        templateResolver.setTemplateMode("HTML5");
	        templateResolver.setCacheable(false);

	        return templateResolver;
	    }    
	    
	    /**
	     * THYMELEAF: Template Resolver for email templates.
	     */
	    @Bean
	    public TemplateResolver emailTemplateResolver() {
	        
	    	SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
	        templateResolver.setApplicationContext(this.applicationContext);
	        templateResolver.setPrefix("/resources/email-templates/");
	        templateResolver.setSuffix(".html");
	        templateResolver.setTemplateMode("HTML5");
	        templateResolver.setCacheable(false);
	        
	        return templateResolver;
	    }


	    @Bean
	    public SpringTemplateEngine templateEngine(){
	        
	    	SpringTemplateEngine templateEngine = new SpringTemplateEngine();
	        final Set<TemplateResolver> templateResolvers = new HashSet<TemplateResolver>();
	        templateResolvers.add(templateResolver());
	        templateResolvers.add(emailTemplateResolver());
	        templateEngine.setTemplateResolvers(templateResolvers);
	        
	        return templateEngine;
	    }

	    @Bean
	    public ViewResolver viewResolver(){
	        ThymeleafViewResolver viewResolver = new ThymeleafViewResolver() ;
	        viewResolver.setTemplateEngine(templateEngine());
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
