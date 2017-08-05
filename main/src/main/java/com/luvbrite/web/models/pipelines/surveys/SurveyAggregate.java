package com.luvbrite.web.models.pipelines.surveys;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity
public class SurveyAggregate {
	
	@Id
	private SurveyId _id;
	private int sum;
	
	public SurveyId get_id() {
		return _id;
	}
	public void set_id(SurveyId _id) {
		this._id = _id;
	}
	public int getSum() {
		return sum;
	}
	public void setSum(int sum) {
		this.sum = sum;
	}
}
