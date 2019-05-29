package com.luvbrite.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.luvbrite.web.models.Product;

public class ListOfProdIds {


  public static List retrieveIds(List<Product> list) {
	  List<Long> idList = list.stream().map(Product::get_id).collect(Collectors.toList());
      return idList;
  }

  
 public static String getCommaSeparatedIds(List<Long> list ) {
   StringBuffer sb = new StringBuffer();
	 for(int i=0;i<list.size();i++) {
		 
		 sb.append(list.get(i));
         if(i<list.size()-1) {
        	 sb.append(",");
         }
	 }
       return sb.toString();
 
 }
  
 public static void main(String[] args) {
	 List list = new ArrayList();
	 for(int i =0 ;i<10;i++) {
		 list.add(i);
	 }
 

	 System.out.println(getCommaSeparatedIds(list));
 } 
 
     
}
