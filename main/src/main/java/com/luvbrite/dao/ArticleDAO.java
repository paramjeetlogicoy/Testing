package com.luvbrite.dao;

import java.util.List;
import java.util.regex.Pattern;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.luvbrite.utils.SequenceGen;
import com.luvbrite.web.models.Article;

@Service
public class ArticleDAO extends BasicDAO<Article, Long> {

	@Autowired
	protected ArticleDAO(Datastore ds) {
		super(ds);
	}
	
	private void setFilters(Query<Article> query, Pattern regExp){
		query.or(
				query.criteria("title").equal(regExp),
				query.criteria("subTitle").equal(regExp)
				);
	}
	
	
	public long count(String search){
		
		if(!search.equals("")) {
			Pattern regExp = Pattern.compile(search + ".*", Pattern.CASE_INSENSITIVE);	
			Query<Article> query = getDs().createQuery(getEntityClass());
			setFilters(query, regExp);
			
			return count(query);	
		}
		
		else
			return super.count();
	}
	
	
	public List<Article> find(String orderBy, int limit, int offset, String search){
		
		final Query<Article> query = getDs().createQuery(getEntityClass()).order(orderBy);

		//Apply search criteria
		if(!search.equals("")){
			Pattern regExp = Pattern.compile(search + ".*", Pattern.CASE_INSENSITIVE);
			setFilters(query, regExp);
		}
		
		//Apply limit if applicable
		if(limit != 0) query.limit(limit);

		//Apply offset
		query.offset(offset);
		
		
		return query.asList();			
	}
	
	
	public long getNextSeq() {
		return SequenceGen.getNextSequence(getDs(), "articles__id");
	}

}
