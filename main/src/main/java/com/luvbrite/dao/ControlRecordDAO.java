package com.luvbrite.dao;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.luvbrite.web.models.ControlRecord;

@Service
public class ControlRecordDAO extends BasicDAO<ControlRecord, String> {

	@Autowired
	protected ControlRecordDAO(Datastore ds) {
		super(ds);
	}
}
