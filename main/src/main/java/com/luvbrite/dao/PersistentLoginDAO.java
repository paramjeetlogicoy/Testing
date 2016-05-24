package com.luvbrite.dao;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.luvbrite.web.models.PersistentLogin;

@Service
public class PersistentLoginDAO extends BasicDAO<PersistentLogin, String> {

	@Autowired
	protected PersistentLoginDAO(Datastore ds) {
		super(ds);
	}

}
