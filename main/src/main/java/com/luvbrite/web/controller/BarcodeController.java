package com.luvbrite.web.controller;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.luvbrite.services.BarcodeFromArray;
import com.luvbrite.web.models.BarcodeSKU;


@Controller
@RequestMapping("/getlabelsku")
public class BarcodeController {

	
	private static Logger logger = Logger.getLogger(BarcodeController.class);
	boolean success=false;
 
	
	@ResponseBody String getLabelSKU(@RequestBody BarcodeSKU barcodes ) {
	  success=false;
	
	
	  try {		
			
			int cellsToSkip =barcodes.getCellsToSkip();
			String[] packetCodes = barcodes.getCodes()==null? null:barcodes.getCodes().split(",");		
			ArrayList<String> nc = new ArrayList<String>();
			
			for(String s: packetCodes){
				nc.add(s);
			}

			
		 String   filename = "Barcodes_labels_" + nc.get(0) + "_" + nc.size() + ".pdf";
			
			BarcodeFromArray bca = new BarcodeFromArray("avery5167", nc, cellsToSkip);
			ByteArrayInputStream stream = new ByteArrayInputStream(bca.getBaos().toByteArray());
			
			success = true;
					
		}catch(Exception e){
			e.printStackTrace();
			
			
		}
	  
	  
	  return null;
	  
  }
}
