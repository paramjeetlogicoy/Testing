package com.luvbrite.web.models.squareup;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity("squareupresponse")
public class Response {

	@Id
	private ObjectId _id;
	private Transaction transaction;
	private List<ResponseError> errors;
	
	public ObjectId get_id() {
		return _id;
	}
	public void set_id(ObjectId _id) {
		this._id = _id;
	}
	public Transaction getTransaction() {
		return transaction;
	}
	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}
	public List<ResponseError> getErrors() {
		return errors;
	}
	public void setErrors(List<ResponseError> errors) {
		this.errors = errors;
	}
}
