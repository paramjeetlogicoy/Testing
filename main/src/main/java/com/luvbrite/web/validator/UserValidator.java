package com.luvbrite.web.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.luvbrite.web.models.User;

@Component
public class UserValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {

		return User.class.equals(clazz);
	}

	@Override
	public void validate(Object obj, Errors e) {
		
		ValidationUtils.rejectIfEmptyOrWhitespace(e, "phone", "", "Phone is required");
		ValidationUtils.rejectIfEmptyOrWhitespace(e, "password", "", "Password cannot be empty");
		ValidationUtils.rejectIfEmptyOrWhitespace(e, "fname", "", "First name is required");
		ValidationUtils.rejectIfEmptyOrWhitespace(e, "lname", "", "Last name required");
		ValidationUtils.rejectIfEmptyOrWhitespace(e, "email", "","Email is required");
		ValidationUtils.rejectIfEmptyOrWhitespace(e, "username", "","Username is required");
		
	}


}
