package com.luvbrite.web.controller;

import org.apache.log4j.Logger;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.luvbrite.dao.CategorySortSequencesDAO;
import com.luvbrite.web.models.CategorySortOptionSeq;
import com.luvbrite.web.models.GenericResponse;
import com.luvbrite.web.models.Order;
import com.luvbrite.web.models.Sequence;

@Controller
public class SortController {

	private static Logger logger = Logger.getLogger(SortController.class);
	
	@Autowired
	CategorySortSequencesDAO categorySortSeqdao;
	
	/****
	*This service finds categorysortoption doc from collection category_sortsequence on the basis of category id 
	*and update existing doc with categorysortoption doc from front  end if  categorysortoption doc does not exist already 
	*insert a new one
	****/
	
	
	@RequestMapping(value = "/CategorySortSeq" ,  method = RequestMethod.GET)
	public @ResponseBody String CategorySortSeq(@RequestBody CategorySortOptionSeq categorySortOption ) {	
		
		long i=0l;
		CategorySortOptionSeq cat_sortOption_seq=null;
		
		cat_sortOption_seq = categorySortSeqdao.findCategorySortOptionSeqDoc(categorySortOption.get_id());
		
		if(cat_sortOption_seq!=null) {
			 
	     Query query = categorySortSeqdao.getDs().createQuery(CategorySortOptionSeq.class)
	    		            .field("_id").equal(categorySortOption.get_id());
		
	     UpdateOperations <CategorySortOptionSeq> ops  = categorySortSeqdao.getDs()
	    		                                        .createUpdateOperations(CategorySortOptionSeq.class)
	    		                                        .set("sortOptionSequences", categorySortOption.getSortOptionSequences());

			categorySortSeqdao.getDs().update(query, ops);
		}
		     else{
		         categorySortOption.set_id(categorySortSeqdao.getNextSeq());
	             i = categorySortSeqdao.persistCategorySortSeq(categorySortOption);
	             }
        
	    
	     return "Ouput After saving==>"+i;
     }

/**
 * Find categorysort option doc from category_sortsequence on the category id 
 * 
 * 
 * **/

	@RequestMapping(value="/getcategorysortoptseq", method= RequestMethod.GET)
	public @ResponseBody CategorySortOptionSeq getCategorySortSeq(@RequestBody CategorySortOptionSeq categorySortOption ) {
		
		       return categorySortSeqdao.findCategorySortOptionSeqByCatId(categorySortOption.getCat_id());
	}
	
	
	
}
