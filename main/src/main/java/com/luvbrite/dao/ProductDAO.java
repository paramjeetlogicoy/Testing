package com.luvbrite.dao;

import java.util.List;
import java.util.regex.Pattern;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.luvbrite.utils.SequenceGen;
import com.luvbrite.web.models.Product;

@Service
public class ProductDAO extends BasicDAO<Product, Long> {

	@Autowired
	protected ProductDAO(Datastore ds) {
		super(ds);
	}
	
	
	public long count(String search){
		
		if(!search.equals("")) {
			Pattern regExp = Pattern.compile(search + ".*", Pattern.CASE_INSENSITIVE);	
			return count(getDs().createQuery(getEntityClass()).filter("name", regExp));	
		}
		
		else
			return super.count();
	}
	
	
	public List<Product> find(String orderBy, int limit, int offset, String search){
		
		final Query<Product> query = getDs().createQuery(getEntityClass()).order(orderBy);

		//Apply search criteria
		if(!search.equals("")){
			Pattern regExp = Pattern.compile(search + ".*", Pattern.CASE_INSENSITIVE);
			query.filter("name", regExp);
		}
		
		//Apply limit if applicable
		if(limit != 0) query.limit(limit);

		//Apply offset
		query.offset(offset);
		
		
		return query.asList();			
	}
	
	
	public List<Product> findAll(String search){
		
		final Query<Product> query = getDs().createQuery(getEntityClass()).order("name");

		//Apply search criteria
		if(!search.equals("")){
			Pattern regExp = Pattern.compile(search + ".*", Pattern.CASE_INSENSITIVE);
			query.filter("name", regExp);
		}
		
		query.retrievedFields(true, "name");
		
		return query.asList();			
	}
	
	
	public long getNextSeq() {
		return SequenceGen.getNextSequence(getDs(), "products__id");
	}

}
