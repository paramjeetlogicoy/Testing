package com.luvbrite.dao;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.luvbrite.web.models.Log;

@Service
public class LogDAO extends BasicDAO<Log, ObjectId> {

	@Autowired
	protected LogDAO(Datastore ds) {
		super(ds);
	}

}
