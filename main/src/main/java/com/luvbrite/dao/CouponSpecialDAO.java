package com.luvbrite.dao;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.luvbrite.web.models.CouponSpecial;

@Service
public class CouponSpecialDAO extends BasicDAO<CouponSpecial, String> {

	@Autowired
	protected CouponSpecialDAO(Datastore ds) {
		super(ds);
	}

}
