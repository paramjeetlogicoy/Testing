package com.luvbrite.web.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.luvbrite.web.models.Category;

@Component
public class CategoryValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {

		return Category.class.equals(clazz);
	}

	@Override
	public void validate(Object obj, Errors e) {

		Category c = (Category) obj;
		
		ValidationUtils.rejectIfEmptyOrWhitespace(e, "name", "", "Category Name is required");
		
		if(c.get_id() == 0){
			e.rejectValue("_id", "", "Category ID is invalid");
		}
	}


}
