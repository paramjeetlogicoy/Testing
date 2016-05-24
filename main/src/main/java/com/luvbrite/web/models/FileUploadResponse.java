package com.luvbrite.web.models;

public class FileUploadResponse extends GenericResponse {

	private String url;
	private long id;
	
	public FileUploadResponse() {
		super();
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
}
