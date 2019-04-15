package com.luvbrite.web.models;

import java.util.List;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity(value="category_sortsequence", noClassnameStored=true)
public class CategorySortOptionSeq {

 @Id 
 private long _id;

 private int cat_id;

 private  List<SortOptionSequence>  sortOptionSequences;

 public long get_id() {
	return _id;
 }
 
 public void set_id(long _id) {
	this._id = _id;
 }

 public int getCat_id() {
	return cat_id;
 }

 public void setCat_id(int cat_id) {
	this.cat_id = cat_id;
 } 

 public List<SortOptionSequence> getSortOptionSequences() {
	return sortOptionSequences;
 }

 public void setSortOptionSequences(List<SortOptionSequence> sortOptionSequences) {
	this.sortOptionSequences = sortOptionSequences;
 }


  }
