package com.luvbrite.web.models;

import java.util.List;

public class ProdCatResponse extends GenericResponse {

	public ProdCatResponse() {
		super();
	}

	private List<Product> products;
	private List<Category> categories;

	public List<Category> getCategories() {
		return categories;
	}

	public void setCategories(List<Category> categories) {
		this.categories = categories;
	}

	public List<Product> getProducts() {
		return products;
	}

	public void setProducts(List<Product> products) {
		this.products = products;
	}
}
