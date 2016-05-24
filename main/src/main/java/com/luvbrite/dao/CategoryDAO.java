package com.luvbrite.dao;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.luvbrite.utils.SequenceGen;
import com.luvbrite.web.models.Category;

@Service
public class CategoryDAO extends BasicDAO<Category, Long> {

	@Autowired
	protected CategoryDAO(Datastore ds) {
		super(ds);
	}
	
	
	public long getNextSeq() {
		return SequenceGen.getNextSequence(getDs(), "categories__id");
	}

}
