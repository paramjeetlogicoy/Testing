package com.luvbrite.dao;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.luvbrite.utils.SequenceGen;
import com.luvbrite.web.models.CategorySortOptionSeq;

@Service
public class CategorySortSequencesDAO extends BasicDAO<CategorySortOptionSeq,Integer> {

	@Autowired
	protected CategorySortSequencesDAO(Datastore ds) {
	 super(ds);	
	}

  public  Long persistCategorySortSeq(CategorySortOptionSeq catSortOptionDoc) {  /*Save CategorySortOption to category_sortsequence collection */
              
	     getDs().save(catSortOptionDoc);
  
         return catSortOptionDoc.get_id();
  }
  
  
  
  
  public CategorySortOptionSeq findCategorySortOptionSeqDoc(final Long _id){  /**Find CategorySortOption doc of provided _id from category_sortsequence collection**/
	    return getDs().find(CategorySortOptionSeq.class).field("_id").equal(_id).get();
	   }

  
  public CategorySortOptionSeq findCategorySortOptionSeqByCatId(final int cat_id) { /**Find CategorySortOption doc of provided 
                                                                                     cat_id from category_sortsequence collection**/
	
	  return  getDs().find(CategorySortOptionSeq.class).field("cat_id").equal(cat_id).get();
	  
  }
  
  public long getNextSeq() {
		return SequenceGen.getNextSequence(getDs(), "categorysortoptionseq__id");
	}
  
}
