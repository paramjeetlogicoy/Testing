package com.luvbrite.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Utility {
	
	public static String [] phpToJavaArray(String phpArray){
		
		String[] javaArray = null,
				emptyArray = new String[]{};
		
		if(phpArray.trim().length()==0 || phpArray.indexOf("a:0") == 0)
			javaArray = emptyArray;
		
		else if(phpArray.indexOf("a:") != 0){
			javaArray = emptyArray;
		}
		
		else{
			
			ArrayList<String> values = new ArrayList<String>();
			
			String[] splits = phpArray.replace("\\}", "").split("\\{");
			for(int i=1; i<splits.length; i++){
				
				String elem = splits[i].split(";")[1];
				String value = elem.split(":")[2];
				
				//remove any starting quotes
				if(value.indexOf("\"")==0)
					value = value.substring(1);
				
				//remove any ending quotes
				if(value.lastIndexOf("\"")==(value.length()-1))
					value = value.substring(0,value.length()-1);
				
				values.add(value);
				
				System.out.println("Array values  - " + value);
			}
			
			javaArray =  values.toArray(new String[]{});
			
		}
		
		
		
		return javaArray;
	}
	
	public static void main(String[] args){
		
		//phpToJavaArray("a:1:{i:0;s:25:\"georgeyporgy123@yahoo.com\";}");
		//Object vars = PHPSerialization.unserialize("a:1:{s:6:'weight';a:6:{s:4:'name';s:6:'Weight';s:5:'value';s:8:'.5g | 1g';s:8:'position';s:1:'0';s:10:'is_visible';i:0;s:12:'is_variation';i:1;s:11:'is_taxonomy';i:0;}}");
		//Object vars = PHPSerialization.unserialize("a:1:{s:6:'weight';s:12:'1/8oz (3.5g)';}");
		
		Object vars = PHPSerializationModified.unserialize("a:1:{s:6:'weight';a:6:{s:4:'name';s:6:'Weight';s:5:'value';s:8:'.5g | 1g';s:8:'position';s:1:'0';s:10:'is_visible';i:0;s:12:'is_variation';i:1;s:11:'is_taxonomy';i:0;}}");
		if(vars!=null){
			if(vars.getClass().equals(HashMap.class)){
				Iterator<?> it = ((Map)vars).entrySet().iterator();
				printArray(it);				
			}
			
		}
		else
		{
			System.out.println("Vars null");
		}
		
	}	
	
	public static void printArray(Iterator<?> it){
		while(it.hasNext()){
			Map.Entry pair = (Map.Entry)it.next();
			
			if( pair.getValue().getClass().equals(HashMap.class)){
				Iterator<?> newIt = ((Map)pair.getValue()).entrySet().iterator();
				printArray(newIt);
			}
			else
				System.out.println(pair.getKey() + " = " + pair.getValue());					
		}
	}
	
	public static double Round(double Rval, int Rpl) {
		double p = (double)Math.pow(10,Rpl);
		double tmp = Math.round(Rval * p);
		return tmp/p;
	}

	public static float Round(float Rval, int Rpl) {
		float p = (float)Math.pow(10,Rpl);
		float tmp = Math.round(Rval * p);
		return tmp/p;
	}

	public static double getDouble(String Rval) {
		
		try {
			return Double.valueOf(Rval).doubleValue();
		} catch (Exception e) {
			return  0d;
		}
	}

	public static int getInteger(String Rval) {
		
		try {
			return Integer.valueOf(Rval).intValue();
		} catch (Exception e) {
			return  0;
		}
	}

	public static float getFloat(String Rval) {
		
		try {
			return Float.valueOf(Rval).floatValue();
		} catch (Exception e) {
			return  0f;
		}
	}

	public static long getLong(String Rval) {
		
		try {
			return Long.valueOf(Rval).longValue();
		} catch (Exception e) {
			return  0l;
		}
	}
	
	public static String getTwoDigit(int i) {
		return i < 10 ? ("0" + i) : (i + "");
	}

}
