package com.luvbrite.security;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Service;

import com.luvbrite.services.RequestUtil;

@Service
public class LBAuthSuccessHandler implements AuthenticationSuccessHandler  {

	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest req,
			HttpServletResponse res, Authentication auth) throws IOException,
			ServletException {	
		
		HttpSession sess = req.getSession();
		sess.setAttribute("currentUser", auth.getName());		

    	SavedRequest savedRequest = 
    		    new HttpSessionRequestCache().getRequest(req, res);
    	String originalURL = savedRequest==null? "" : savedRequest.getRedirectUrl();
		
		String targetUrl = determineTargetUrl(auth, originalURL); 
		if(RequestUtil.isAjaxRequest(req)) {
			RequestUtil.sendJsonResponse(res, "success", targetUrl);
		}
		else{
			redirectStrategy.sendRedirect(req, res, targetUrl);
		}
		
	}

	protected String determineTargetUrl(Authentication authentication, String originalURL) {
        
		Set<String> authorities = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        if (authorities.contains("ROLE_ADMIN")) {
        	
        	if(!originalURL.equals(""))
        		return originalURL;
        	
        	else
        		return "/admin/orders";
        	
        
        } else if (authorities.contains("ROLE_CUSTOMER")){
        	
        	if(!originalURL.equals(""))
        		return originalURL;
        	
        	else
        		return "/customer";
        	
        
        } else {
        		
        	return "/pending-registration";
        }
    }

	public RedirectStrategy getRedirectStrategy() {
		return redirectStrategy;
	}

	public void setRedirectStrategy(RedirectStrategy redirectStrategy) {
		this.redirectStrategy = redirectStrategy;
	}
}
