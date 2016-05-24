package com.luvbrite.components;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Component;

import com.luvbrite.dao.PersistentLoginDAO;
import com.luvbrite.web.models.PersistentLogin;

@Component
public class MongoDBTokenRepository implements PersistentTokenRepository {

	@Autowired
	private PersistentLoginDAO dao;
	
	@Override
	public void createNewToken(PersistentRememberMeToken token) {
		
		PersistentLogin pl = new PersistentLogin();
		pl.set_id(token.getSeries());
		pl.setTimeStamp(token.getDate());
		pl.setToken(token.getTokenValue());
		pl.setUsername(token.getUsername());

		dao.save(pl);
	}

	@Override
	public PersistentRememberMeToken getTokenForSeries(String series) {
		PersistentLogin pl = dao.findOne("_id", series);
		
		if(pl==null) {
			
			System.out.println("PersistentRememberMeToken is NULL ");
			
			return null;
		}
		
/*		else{
			System.out.println(pl.getUsername());
			System.out.println(pl.get_id());
			System.out.println(pl.getToken());
			System.out.println(pl.getTimeStamp());
		}*/
		
		return new PersistentRememberMeToken(pl.getUsername(), pl.get_id(), pl.getToken(), pl.getTimeStamp());
	}

	@Override
	public void removeUserTokens(String username) {
		dao.deleteByQuery(dao.getDs().createQuery(dao.getEntityClass()).filter("username", username));
	}

	@Override
	public void updateToken(String series, String tokenValue, Date lastUsed) {

		PersistentLogin pl = dao.findOne("_id", series);
		pl.setTimeStamp(lastUsed);
		pl.setToken(tokenValue);
		
		dao.save(pl);
	}

}
