package com.luvbrite.dao;

import java.util.List;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.luvbrite.utils.SequenceGen;
import com.luvbrite.web.models.Price;

@Service
public class PriceDAO extends BasicDAO<Price, Long> {

	@Autowired
	protected PriceDAO(Datastore ds) {
		super(ds);
	}

	
	public List<Price> findPriceByProduct(long productId) {		
		return getDs().find(getEntityClass()).filter("pid", productId).asList();
	}
	
	
	public long getNextSeq() {
		return SequenceGen.getNextSequence(getDs(), "prices__id");
	}

}
