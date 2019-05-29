package com.luvbrite.web.models;

import java.util.List;

public class ProductsInfoJSON {

	private String message;
	
	private List<ProductAvailable> result;

	private Boolean success;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<ProductAvailable> getResult() {
		return result;
	}

	public void setResult(List<ProductAvailable> result) {
		this.result = result;
	}

	public Boolean isSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}
	
}
