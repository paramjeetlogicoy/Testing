package com.luvbrite.web.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luvbrite.dao.ProductDAO;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.web.controller.admin.ProductsController;
import com.luvbrite.web.models.Log;
import com.luvbrite.web.models.Product;

@Controller
@RequestMapping(value = "/updateproductStockStatus")
public class UpdateProductStockStatus {

	private static Logger logger = Logger.getLogger(UpdateProductStockStatus.class);

	@Autowired
	ProductDAO prdDao;
	

	@RequestMapping(value = "/updateProds", method = RequestMethod.POST)
	public ResponseEntity<String> updateProductStockStatus(@RequestBody String statusAndMongoID) {
		logger.debug("raw String recieved-->" + statusAndMongoID);
		try {
			statusAndMongoID = URLDecoder.decode(statusAndMongoID, StandardCharsets.UTF_8.toString());
			logger.debug("After Decoding String recieved-->" + statusAndMongoID);

		} catch (UnsupportedEncodingException e) {
			logger.error("Exception while Decoding data recieved from Inventory");
			e.printStackTrace();
		}

		String[] statusAndid = statusAndMongoID.split("~");
        String stockStatus = statusAndid[0];
		Long mongoId = Long.parseLong(statusAndid[1]);
        
		Product productDb = prdDao.get(mongoId);

		if (productDb != null) {
			productDb.setStockStat(stockStatus);
			prdDao.save(productDb);
			return new ResponseEntity<String>("SUCCESS", HttpStatus.OK);
		} else {
			logger.debug("Can not find mongo productid " + mongoId);
			return new ResponseEntity<String>("Can not find mongo productid " + mongoId, HttpStatus.OK);
		}

		
	}

	

}
