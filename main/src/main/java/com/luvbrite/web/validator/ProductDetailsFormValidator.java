package com.luvbrite.web.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.luvbrite.web.models.Product;

@Component
public class ProductDetailsFormValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {

		return Product.class.equals(clazz);
	}

	@Override
	public void validate(Object obj, Errors e) {

		Product p = (Product) obj;
		
		ValidationUtils.rejectIfEmptyOrWhitespace(e, "name", "", "Product Name is required");
		ValidationUtils.rejectIfEmptyOrWhitespace(e, "url", "", "Product URL cannot be empty");
		ValidationUtils.rejectIfEmptyOrWhitespace(e, "status", "", "Status is required");
		ValidationUtils.rejectIfEmptyOrWhitespace(e, "description", "", "Product description ire required");
		ValidationUtils.rejectIfEmptyOrWhitespace(e, "stockStat", "","Stock status is required");
		
		if(p.get_id() == 0){
			e.rejectValue("_id", "", "Product ID is invalid");
		}
	}


}
