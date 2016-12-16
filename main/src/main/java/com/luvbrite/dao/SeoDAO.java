package com.luvbrite.dao;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.luvbrite.web.models.Seo;

@Service
public class SeoDAO extends BasicDAO<Seo, ObjectId> {

	@Autowired
	protected SeoDAO(Datastore ds) {
		super(ds);
	}

}
