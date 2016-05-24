package com.luvbrite.web.models;

import java.util.List;

public class ResponseWithPg {
	
	private boolean success = false;
	private String message = "";
	private Pagination pg;
	private List<?> respData;
	
	public ResponseWithPg() {}
	
	public Pagination getPg() {
		return pg;
	}
	public void setPg(Pagination pg) {
		this.pg = pg;
	}
	
	public List<?> getRespData() {
		return respData;
	}
	public void setRespData(List<?> respData) {
		this.respData = respData;
	}

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
}
