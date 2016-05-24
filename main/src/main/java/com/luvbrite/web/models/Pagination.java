package com.luvbrite.web.models;

public class Pagination {


	protected int totalItems = 0;
	protected int currentPage = 0;
	protected int itemsPerPage = 0;
	protected int startCount = 0;
	protected int endCount = 0;
	protected int offset = 0;

	public Pagination(){}	
	
	public int getTotalItems() {
		return totalItems;
	}
	public void setTotalItems(int totalItems) {
		this.totalItems = totalItems;
	}
	public int getCurrentPage() {
		return currentPage;
	}
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}
	public int getItemsPerPage() {
		return itemsPerPage;
	}
	public void setItemsPerPage(int itemsPerPage) {
		this.itemsPerPage = itemsPerPage;
	}
	public int getStartCount() {
		return startCount;
	}
	public void setStartCount(int startCount) {
		this.startCount = startCount;
	}
	public int getEndCount() {
		return endCount;
	}
	public void setEndCount(int endCount) {
		this.endCount = endCount;
	}
	public int getOffset() {
		return offset;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}
	
	
	@Override
	public String toString() {
		return 
		new StringBuilder("{")
		.append("\"totalItems\":").append(totalItems).append(",")
		.append("\"currentPage\":").append(currentPage).append(",")
		.append("\"itemsPerPage\":").append(itemsPerPage).append(",")
		.append("\"startCount\":").append(startCount).append(",")
		.append("\"endCount\":").append(endCount).append(",")
		.append("\"offset\":").append(offset)
		.append("}").toString();
	}
}
