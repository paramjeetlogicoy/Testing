package com.luvbrite.web.models;

public class BarCodePageSetup {

	private float pageWidth = 0f;
	private float pageHeight = 0f;
	private float topMargin = 0f;
	private float botMargin = 0f;
	private float leftMargin = 0f;
	private float rightMargin = 0f;
	private float padding = 0f;
	private float size = 0f;
	private float[] columnSizes = {};
	private float rowHeight = 0f;
	
	private int columns = 0;
	private int rowCount = 0;
	public float getPageWidth() {
		return pageWidth;
	}
	public void setPageWidth(float pageWidth) {
		this.pageWidth = pageWidth;
	}
	public float getPageHeight() {
		return pageHeight;
	}
	public void setPageHeight(float pageHeight) {
		this.pageHeight = pageHeight;
	}
	public float getTopMargin() {
		return topMargin;
	}
	public void setTopMargin(float topMargin) {
		this.topMargin = topMargin;
	}
	public float getBotMargin() {
		return botMargin;
	}
	public void setBotMargin(float botMargin) {
		this.botMargin = botMargin;
	}
	public float getLeftMargin() {
		return leftMargin;
	}
	public void setLeftMargin(float leftMargin) {
		this.leftMargin = leftMargin;
	}
	public float getRightMargin() {
		return rightMargin;
	}
	public void setRightMargin(float rightMargin) {
		this.rightMargin = rightMargin;
	}
	public float getPadding() {
		return padding;
	}
	public void setPadding(float padding) {
		this.padding = padding;
	}
	public float getSize() {
		return size;
	}
	public void setSize(float size) {
		this.size = size;
	}
	public float[] getColumnSizes() {
		return columnSizes;
	}
	public void setColumnSizes(float[] columnSizes) {
		this.columnSizes = columnSizes;
	}
	public float getRowHeight() {
		return rowHeight;
	}
	public void setRowHeight(float rowHeight) {
		this.rowHeight = rowHeight;
	}
	public int getColumns() {
		return columns;
	}
	public void setColumns(int columns) {
		this.columns = columns;
	}
	public int getRowCount() {
		return rowCount;
	}
	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}
}