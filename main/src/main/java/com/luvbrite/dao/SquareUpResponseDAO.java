package com.luvbrite.dao;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.luvbrite.web.models.squareup.Response;

@Service
public class SquareUpResponseDAO extends BasicDAO<Response, ObjectId> {

	@Autowired
	protected SquareUpResponseDAO(Datastore ds) {
		super(ds);
	}
}
