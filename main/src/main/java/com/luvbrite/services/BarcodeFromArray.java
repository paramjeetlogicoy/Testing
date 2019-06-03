package com.luvbrite.services;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.Barcode;
import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.luvbrite.utils.BarCodeConstants;
import  com.luvbrite.web.models.BarCodePageSetup;;

public class BarcodeFromArray {
	
	private Barcode128 code128;
	private PdfWriter writer;
	private ByteArrayOutputStream baos;
	private BarCodePageSetup bcps;
	
	public BarcodeFromArray(String averyTemplate, ArrayList<String> items, int skip) 
			throws FileNotFoundException, DocumentException {
		
		bcps = BarCodeConstants.constantsMap.get(averyTemplate);
		
		if(bcps!=null){		
			Rectangle pageSize = new Rectangle(bcps.getPageWidth()*72f, bcps.getPageHeight()*72f);
			Document document = new Document(pageSize, 
					bcps.getLeftMargin(), bcps.getRightMargin(), 
					bcps.getTopMargin(), bcps.getBotMargin());
			
			baos = new ByteArrayOutputStream();
			writer = PdfWriter.getInstance(document, baos);

			document.open(); 

			code128 = new Barcode128();
			code128.setSize(bcps.getSize());
			code128.setCodeType(Barcode.CODE128);

			PdfPTable table = new PdfPTable(bcps.getColumns());
			table.setTotalWidth(bcps.getColumnSizes());
			table.setLockedWidth(true);
			
			int columnIteration = (bcps.getColumns()+1)/2;
			
			/**
			 * SKIP tells the system to skip few columns before starting
			 * to print the data. We create empty items in the starting
			 * of the LIST 
			 **/
			if(skip>0){
				for(int j=0; j<skip; j++){
					items.add(0, "");
				}
			}
			
			/**
			 * The arraysize has to be multiple of four. Else the last line
			 * won't be printed. Hence add dummy elements to the end of array 
			 * and make it a multiple of four
			 **/
			int factor = items.size() % 4;
			
			if(factor!=0){
				for(int j=0; j<(4-factor); j++){
					items.add("0~DO NOT USE");
				}
			}

			for(int i=0; i<items.size(); i++){			

				table.addCell(getMainCell(items.get(i)));
				
				if((i==0) ||
						((i+1) % columnIteration) != 0)
					table.addCell(getCell());
			}
			
			document.add(table);
			document.close();
		}
	}
	
	private static PdfPCell getCell(){

		PdfPCell cell = new PdfPCell();
		cell.setBorder(Rectangle.NO_BORDER);
		
		return cell;		
	}
	
	private PdfPCell getMainCell(String string){
		PdfPCell cell = new PdfPCell();
		
		cell.setFixedHeight(bcps.getRowHeight());
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setBorder(Rectangle.NO_BORDER);
		cell.setPadding(bcps.getPadding());

		if(!string.equals("") && !string.equals("0~DO NOT USE")){

			code128.setCode(string.split("~")[0]);
			code128.setAltText(string);
			Image img = code128.createImageWithBarcode(writer.getDirectContent(), null, null);
			cell.setImage(img);
		}
		
		return cell;
	}

	public ByteArrayOutputStream getBaos() {
		return baos;
	}
}

