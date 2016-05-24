package com.luvbrite.web.models;

import java.util.List;

import org.mongodb.morphia.annotations.Embedded;

@Embedded
public class ProductAttr {
	
	private String attr;
	private int position;
	private boolean visible;
	private List<String> values;
	
	public String getAttr() {
		return attr;
	}
	public void setAttr(String attr) {
		this.attr = attr;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public boolean isVisible() {
		return visible;
	}
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	public List<String> getValues() {
		return values;
	}
	public void setValues(List<String> values) {
		this.values = values;
	}
}
