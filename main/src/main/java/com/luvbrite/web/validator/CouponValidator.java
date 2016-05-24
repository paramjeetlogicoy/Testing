package com.luvbrite.web.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.luvbrite.web.models.Coupon;

@Component
public class CouponValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {

		return Coupon.class.equals(clazz);
	}

	@Override
	public void validate(Object obj, Errors e) {

		Coupon c = (Coupon) obj;
		
		ValidationUtils.rejectIfEmptyOrWhitespace(e, "_id", "", "Coupon code is required");
		ValidationUtils.rejectIfEmptyOrWhitespace(e, "couponValue", "", "Coupon value cannot be empty");
		
		if(c.getExpiry()==null && c.getMaxUsageCount()==0){
			e.rejectValue("", "", "Both Max Usage and Expiry Date cannot be empty");
		}

	}


}
