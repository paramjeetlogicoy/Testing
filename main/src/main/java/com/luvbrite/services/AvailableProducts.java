package com.luvbrite.services;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.utils.GenericConnection;
import com.luvbrite.utils.ListOfProdIds;
import com.luvbrite.web.models.Product;
import com.luvbrite.web.models.ProductAvailable;
import com.luvbrite.web.models.ProductsInfoJSON;

public class AvailableProducts {

	private Logger logger = Logger.getLogger(AvailableProducts.class);

	// private final String postProdListURL = "http://localhost:8086/inventory/apps/acceptproductlist?json";
        
        private final String postProdListURL = "https://www.luvbrite.com/inventory/apps/acceptproductlist?json";
        
	public List<Product> getAvailProdsFromInv(List<Product> activeProdList) {
                    
                if(activeProdList == null || activeProdList.size() == 0){
                 return activeProdList; 
                }
                
		String commaSeparatedIds = ListOfProdIds.getCommaSeparatedIds(ListOfProdIds.retrieveIds(activeProdList));
                
		List<Product> prodAvailInInventory = null;
		String resp = null;
		try {

			/* POST TO INVENTORY SERVER */
			GenericConnection conn = new GenericConnection();

			resp = conn.contactService(commaSeparatedIds, new URL(postProdListURL), false);
			
			if (resp == null) {
				logger.debug("Recieved NULL after trying to hit Inventory Appln");
				return activeProdList;
			}

			Gson g = new Gson();
			ProductsInfoJSON prodInfo = g.fromJson(resp, ProductsInfoJSON.class);

			if (!prodInfo.isSuccess()) {
				logger.debug("Successfully recieved response from Inv Application but success = False ");
				return activeProdList;
			}

			List<ProductAvailable> prodAvailableList = prodInfo.getResult();
			logger.info("Size Of available from inventory product->" + prodAvailableList.size());
			if (prodAvailableList.size() == 0) {
				logger.debug("Size of Product List recieved from Inventory is 0");
				return activeProdList;
			}
			prodAvailInInventory = new ArrayList();
			Collections.sort(activeProdList);
			if (activeProdList.size() == prodAvailableList.size()) {
				logger.debug(
						"List of products recieved from Inventory is EQUAL to List of products from Main(Luvbrite)");
				for (int i = 0; i < activeProdList.size(); i++) {
					ProductAvailable prodAvailable = prodAvailableList.get(i);
					Product prod = activeProdList.get(i);
					if (prodAvailable.getTotal_remain_qty() > 0) {

						prod.setProduct_id(prodAvailable.getProduct_id());
						prod.setCategory_id(prodAvailable.getCategory_id());
						prod.setStrainid(prodAvailable.getStrainid());
						prod.setStrain_name(prodAvailable.getStrain_name());
						prod.setTotal_purchase_qty(prodAvailable.getTotal_purchase_qty());
						prod.setTotal_packet_qty(prodAvailable.getTotal_purchase_qty());
						prod.setTotal_sold_qty(prodAvailable.getTotal_sold_qty());
						prod.setTotal_remain_qty(prodAvailable.getTotal_remain_qty());
						prod.setInv_productname(prodAvailable.getInv_productname());
						prod.setMongo_productid(prodAvailable.getMongo_productid());
                                                prod.setFromInv(prodAvailable.isFromInv());
						prodAvailInInventory.add(prod);
					}

				}
			} else {
				logger.debug(
						"List of products recieved from Inventory IS NOT EQUAL to List of products from Main(Luvbrite)");

				int activeProdsSize = activeProdList.size();
				int invAvailProdsSize = prodAvailableList.size();

				int activeProd_pointr = 0;
				int invProd_pointr = 0;
            
				
				
				while (activeProd_pointr < activeProdsSize && invProd_pointr < invAvailProdsSize) {
                    
					Product activeProd = activeProdList.get(activeProd_pointr);
					ProductAvailable prodAvailable = prodAvailableList.get(invProd_pointr);
                          
					if (activeProd.get_id() < prodAvailable.getMongo_productid()) {
						//prodAvailInInventory.add(activeProdList.get(activeProd_pointr));
						activeProd_pointr++;
					} 
					else if (activeProd.get_id() > prodAvailable.getMongo_productid()) {
						//prodAvailInInventory.add(activeProdList.get(activeProd_pointr));
						invProd_pointr++;
					} 
					else {

                         if (prodAvailable.getTotal_remain_qty() > 0) {
						
							activeProd.setProduct_id(prodAvailable.getProduct_id());
							activeProd.setCategory_id(prodAvailable.getCategory_id());
							activeProd.setStrainid(prodAvailable.getStrainid());
							activeProd.setStrain_name(prodAvailable.getStrain_name());
							activeProd.setTotal_purchase_qty(prodAvailable.getTotal_purchase_qty());
							activeProd.setTotal_packet_qty(prodAvailable.getTotal_purchase_qty());
							activeProd.setTotal_sold_qty(prodAvailable.getTotal_sold_qty());
							activeProd.setTotal_remain_qty(prodAvailable.getTotal_remain_qty());
							activeProd.setMongo_productid(prodAvailable.getMongo_productid());
							activeProd.setInv_productname(prodAvailable.getInv_productname());
							activeProd.setInv_productname(prodAvailable.getInv_productname());
                            activeProd.setFromInv(prodAvailable.isFromInv());
							prodAvailInInventory.add(activeProd);
							}
                         
                         activeProd_pointr++;
						 invProd_pointr++;
					}
				}



			}

		} catch (Exception e) {

			logger.error("Error retrieving products from inventory");
			logger.error(Exceptions.giveStackTrace(e));
			return activeProdList;
		}
		
		logger.info("size of product available list before returning ==" + prodAvailInInventory.size());
		return prodAvailInInventory;
	}

	
	
	public List<Product> getAllAvailProdsFromInv(List<Product> activeProdList) {

		String commaSeparatedIds = ListOfProdIds.getCommaSeparatedIds(ListOfProdIds.retrieveIds(activeProdList));

		List<Product> prodAvailInInventory = null;
		String resp = null;
		try {

			/* POST TO INVENTORY SERVER */
			GenericConnection conn = new GenericConnection();

			resp = conn.contactService(commaSeparatedIds, new URL(postProdListURL), false);
			
			if (resp == null) {
				logger.debug("Recieved NULL after trying to hit Inventory Appln");
				return activeProdList;
			}

			Gson g = new Gson();
			ProductsInfoJSON prodInfo = g.fromJson(resp, ProductsInfoJSON.class);

			if (!prodInfo.isSuccess()) {
				logger.debug("Successfully recieved response from Inv Application but success = False ");
				return activeProdList;
			}

			List<ProductAvailable> prodAvailableList = prodInfo.getResult();
			logger.info("Size Of available from inventory product->" + prodAvailableList.size());
			if (prodAvailableList.size() == 0) {
				logger.debug("Size of Product List recieved from Inventory is 0");
				return activeProdList;
			}
			prodAvailInInventory = new ArrayList();
			Collections.sort(activeProdList);
			if (activeProdList.size() == prodAvailableList.size()) {
				logger.debug(
						"List of products recieved from Inventory is EQUAL to List of products from Main(Luvbrite)");
				for (int i = 0; i < activeProdList.size(); i++) {
					ProductAvailable prodAvailable = prodAvailableList.get(i);
					Product prod = activeProdList.get(i);
					

						prod.setProduct_id(prodAvailable.getProduct_id());
						prod.setCategory_id(prodAvailable.getCategory_id());
						prod.setStrainid(prodAvailable.getStrainid());
						prod.setStrain_name(prodAvailable.getStrain_name());
						prod.setTotal_purchase_qty(prodAvailable.getTotal_purchase_qty());
						prod.setTotal_packet_qty(prodAvailable.getTotal_purchase_qty());
						prod.setTotal_sold_qty(prodAvailable.getTotal_sold_qty());
						prod.setTotal_remain_qty(prodAvailable.getTotal_remain_qty());
						prod.setInv_productname(prodAvailable.getInv_productname());
						prod.setMongo_productid(prodAvailable.getMongo_productid());
                        prod.setFromInv(prodAvailable.isFromInv());
						prodAvailInInventory.add(prod);
					

				}
			} else {
				logger.debug(
						"List of products recieved from Inventory IS NOT EQUAL to List of products from Main(Luvbrite)");

				int activeProdsSize = activeProdList.size();
				int invAvailProdsSize = prodAvailableList.size();

				int activeProd_pointr = 0;
				int invProd_pointr = 0;
            
				
				
				while (activeProd_pointr < activeProdsSize && invProd_pointr < invAvailProdsSize) {
                    
					Product activeProd = activeProdList.get(activeProd_pointr);
					ProductAvailable prodAvailable = prodAvailableList.get(invProd_pointr);
                          
					if (activeProd.get_id() < prodAvailable.getMongo_productid()) {
						//prodAvailInInventory.add(activeProdList.get(activeProd_pointr));
						activeProd_pointr++;
					} 
					else if (activeProd.get_id() > prodAvailable.getMongo_productid()) {
						//prodAvailInInventory.add(activeProdList.get(activeProd_pointr));
						invProd_pointr++;
					} 
					else {
                            activeProd.setProduct_id(prodAvailable.getProduct_id());
							activeProd.setCategory_id(prodAvailable.getCategory_id());
							activeProd.setStrainid(prodAvailable.getStrainid());
							activeProd.setStrain_name(prodAvailable.getStrain_name());
							activeProd.setTotal_purchase_qty(prodAvailable.getTotal_purchase_qty());
							activeProd.setTotal_packet_qty(prodAvailable.getTotal_purchase_qty());
							activeProd.setTotal_sold_qty(prodAvailable.getTotal_sold_qty());
							activeProd.setTotal_remain_qty(prodAvailable.getTotal_remain_qty());
							activeProd.setMongo_productid(prodAvailable.getMongo_productid());
							activeProd.setInv_productname(prodAvailable.getInv_productname());
							activeProd.setInv_productname(prodAvailable.getInv_productname());
                            activeProd.setFromInv(prodAvailable.isFromInv());
							prodAvailInInventory.add(activeProd);
						
                         
                         activeProd_pointr++;
						 invProd_pointr++;
					}
				}

			

			}

		} catch (Exception e) {

			logger.error("Error retrieving products from inventory");
			logger.error(Exceptions.giveStackTrace(e));
			return activeProdList;
		}
		
		logger.info("size of product available list before returning ==" + prodAvailInInventory.size());
		return prodAvailInInventory;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
