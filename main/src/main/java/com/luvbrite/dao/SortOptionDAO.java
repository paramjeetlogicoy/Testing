package com.luvbrite.dao;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;

import com.luvbrite.web.models.SortOptions;

public class SortOptionDAO extends BasicDAO<SortOptions, Integer> {

	
	protected SortOptionDAO(Datastore ds) {
		super(ds);
	}





}
