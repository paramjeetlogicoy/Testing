package com.luvbrite.dao;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.luvbrite.web.models.Review;

@Service
public class ReviewDAO extends BasicDAO<Review, ObjectId> {

	@Autowired
	protected ReviewDAO(Datastore ds) {
		super(ds);
	}

}
