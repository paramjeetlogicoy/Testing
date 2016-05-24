package com.luvbrite.security;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Service;

import com.luvbrite.services.RequestUtil;

@Service
public class LBAuthFailureHandler implements AuthenticationFailureHandler  {

	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

	public RedirectStrategy getRedirectStrategy() {
		return redirectStrategy;
	}

	public void setRedirectStrategy(RedirectStrategy redirectStrategy) {
		this.redirectStrategy = redirectStrategy;
	}
	

	@Override
	public void onAuthenticationFailure(HttpServletRequest req,
			HttpServletResponse res, AuthenticationException exception)
			throws IOException, ServletException {
		
		if(RequestUtil.isAjaxRequest(req)) {	
			RequestUtil.sendJsonResponse(res, "authfailure", "true");
		}
		else{
			redirectStrategy.sendRedirect(req, res, "/login?error");
		}
	}
}
