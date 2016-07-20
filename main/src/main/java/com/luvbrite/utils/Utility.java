package com.luvbrite.utils;

import java.util.ArrayList;

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
