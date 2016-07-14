package com.luvbrite.web.models.squareup;

import org.mongodb.morphia.annotations.Embedded;

@Embedded
public class ResponseError {

	private String category;
	private String code;
	private String detail;
	private String field;
	
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
}
