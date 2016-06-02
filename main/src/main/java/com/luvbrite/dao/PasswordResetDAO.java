package com.luvbrite.dao;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.luvbrite.web.models.PasswordReset;

@Service
public class PasswordResetDAO extends BasicDAO<PasswordReset, ObjectId> {

	@Autowired
	protected PasswordResetDAO(Datastore ds) {
		super(ds);
	}

}
