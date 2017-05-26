package com.luvbrite.dao;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.luvbrite.web.models.Survey;

@Service
public class SurveyDAO extends BasicDAO<Survey, Long> {

	@Autowired
	protected SurveyDAO(Datastore ds) {
		super(ds);
	}

}
