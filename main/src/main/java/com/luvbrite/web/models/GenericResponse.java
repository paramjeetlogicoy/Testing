package com.luvbrite.web.models;

import java.util.List;

public class GenericResponse {

	private boolean success;
	private String message;
	private List<?> results;
	
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public List<?> getResults() {
		return results;
	}
	public void setResults(List<?> results) {
		this.results = results;
	}
}
