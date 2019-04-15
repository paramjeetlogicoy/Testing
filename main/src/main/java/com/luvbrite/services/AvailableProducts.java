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

	private final String postProdListURL = "http://localhost:8989/inventory/apps/acceptproductlist?json";

	public List<Product> getAvailProdsFromInv(List<Product> activeProdList) {

		String commaSeparatedIds = ListOfProdIds.getCommaSeparatedIds(ListOfProdIds.retrieveIds(activeProdList));

		List<Product> prodAvailInInventory = null;
		String resp = null;
		try {

			/* POST TO INVENTORY SERVER */
			GenericConnection conn = new GenericConnection();

			resp = conn.contactService(commaSeparatedIds, new URL(postProdListURL), false);
            //logger.info("Resp-> "+resp+"CommaSepareatedIds-->"+commaSeparatedIds);
			if (resp != null) {

				Gson g = new Gson();
				ProductsInfoJSON prodInfo = g.fromJson(resp, ProductsInfoJSON.class);

				if (prodInfo.isSuccess()) {

					List<ProductAvailable> prodAvailableList = prodInfo.getResult();

					if (prodAvailableList.size() > 0) {

						prodAvailInInventory = new ArrayList();

						Collections.sort(activeProdList);

						if (activeProdList.size() == prodAvailableList.size()) {

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
									prod.setTotal_remain_qty();
									prod.setTotal_purchase_weight(prodAvailable.getTotal_purchase_weight());
									prod.setTotal_packed_weight(prodAvailable.getTotal_packed_weight());
									prod.setTotal_sold_weight(prodAvailable.getTotal_sold_weight());
									prod.setTotal_remain_weight();
									prod.setMongo_productid(prodAvailable.getMongo_productid());

									prodAvailInInventory.add(prod);
								}

							}
						} else {

							for (int i = 0; i < activeProdList.size(); i++) {

								Product prod = activeProdList.get(i);

								for (int j = 0; j < prodAvailableList.size(); j++) {

									ProductAvailable prodAvailable = prodAvailableList.get(j);

									if (prod.get_id() == prodAvailable.getMongo_productid()
											&& prodAvailable.getTotal_remain_qty() > 0) {

										prod.setProduct_id(prodAvailable.getProduct_id());
										prod.setCategory_id(prodAvailable.getCategory_id());
										prod.setStrainid(prodAvailable.getStrainid());
										prod.setStrain_name(prodAvailable.getStrain_name());
										prod.setTotal_purchase_qty(prodAvailable.getTotal_purchase_qty());
										prod.setTotal_packet_qty(prodAvailable.getTotal_purchase_qty());
										prod.setTotal_sold_qty(prodAvailable.getTotal_sold_qty());
										prod.setTotal_remain_qty();
										prod.setTotal_purchase_weight(prodAvailable.getTotal_purchase_weight());
										prod.setTotal_packed_weight(prodAvailable.getTotal_packed_weight());
										prod.setTotal_sold_weight(prodAvailable.getTotal_sold_weight());
										prod.setTotal_remain_weight();
										prod.setMongo_productid(prodAvailable.getMongo_productid());

										prodAvailInInventory.add(prod);
										prodAvailableList.remove(j);
										break;

									}else {
										prodAvailInInventory.add(prod);
									}

								}

							}

						}

					} else {
						return activeProdList;
					}
				} else {
					return activeProdList;
				}

			}

		} catch (Exception e) {
			logger.error(Exceptions.giveStackTrace(e));
			return activeProdList;
		}

		return prodAvailInInventory;
	}

}
