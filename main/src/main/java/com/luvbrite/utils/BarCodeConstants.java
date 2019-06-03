package com.luvbrite.utils;

import java.util.HashMap;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.luvbrite.web.models.BarCodePageSetup;

public class BarCodeConstants {
	
	public static HashMap<String, BarCodePageSetup> constantsMap;
	public static Font HELVETICA_BOLD = new Font(FontFamily.HELVETICA, 7, Font.BOLD, BaseColor.BLACK);
	
	static {
		constantsMap = new HashMap<String, BarCodePageSetup>();
		
		constantsMap.put("avery5167", avery5167());		
		constantsMap.put("dymo30346", dymo30346());	
		constantsMap.put("kelly01", kellyPaper());
		
	}
	
	private static BarCodePageSetup avery5167() {
		
		BarCodePageSetup map = new BarCodePageSetup();	
		map.setPageWidth(8.5f);
		map.setPageHeight(11f);
		map.setTopMargin(38f);
		map.setBotMargin(34f);
		map.setLeftMargin(18f);
		map.setRightMargin(18f);
		map.setPadding(4f);
		map.setSize(7f);
		map.setColumnSizes(new float[]{ 126f, 22.5f, 126f, 22.5f, 126f, 22.5f, 126f});
		map.setRowHeight(36f);
		
		map.setColumns(7);
		map.setRowCount(20);
		
		return map;		
	}
	
	private static BarCodePageSetup kellyPaper() {
		
		BarCodePageSetup map = new BarCodePageSetup();	
		map.setPageWidth(8.5f);
		map.setPageHeight(11f);
		map.setTopMargin(36f);
		map.setBotMargin(36f);
		map.setLeftMargin(12f);
		map.setRightMargin(12f);
		map.setPadding(4f);
		map.setSize(8f);
		map.setColumnSizes(new float[]{ (72f - 24f), 114f, 14f, 72f, 114f, 14f, 72f, 114f});
		map.setRowHeight(72f);
		
		map.setColumns(8);
		map.setRowCount(10);
		
		return map;		
	}
	
	private static BarCodePageSetup dymo30346() { 
		
		BarCodePageSetup map = new BarCodePageSetup();	
		map.setPageWidth(1.14f); //adjusted for Brother QL-700
		map.setPageHeight(0.5f);
		map.setTopMargin(0f);
		map.setBotMargin(0f);
		map.setLeftMargin(0f);
		map.setRightMargin(0f);
		map.setPadding(0);
		map.setSize(7f);
		
		map.setColumns(1);
		map.setRowCount(1);
		
		return map;		
	}

}
