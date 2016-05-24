package com.luvbrite.utils;

import com.luvbrite.web.models.Pagination;

public class PaginationLogic {
	
	private Pagination pg = null;

	public Pagination getPg() {
		return pg;
	}

	public void setPg(Pagination pg) {
		this.pg = pg;
	} 
	
	public PaginationLogic(int totalItems, int itemsPerPage, int currentPage){
		
		pg = new Pagination();
		int pages = 0,
				offset = 0;
		
		if (totalItems != 0 && (totalItems % itemsPerPage) == 0) {
			pages = totalItems / itemsPerPage;
		} else {
			pages = (totalItems / itemsPerPage) + 1;
		}

		if (currentPage > pages) {
			currentPage = pages;
		}
		
		offset = (currentPage - 1) * itemsPerPage;

		
		int startC = offset + 1;
		int endC = offset + itemsPerPage;
		
		if(startC > totalItems) startC = totalItems;
		if(endC > totalItems) endC = totalItems;
		
		
		pg.setCurrentPage(currentPage);
		pg.setItemsPerPage(itemsPerPage);
		pg.setTotalItems(totalItems);
		pg.setStartCount(startC);
		pg.setEndCount(endC);
		pg.setOffset(offset);
	}
}
