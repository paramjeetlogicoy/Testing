package com.luvbrite.services;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordEncoderImp extends BCryptPasswordEncoder implements
		PasswordEncoder {

}
