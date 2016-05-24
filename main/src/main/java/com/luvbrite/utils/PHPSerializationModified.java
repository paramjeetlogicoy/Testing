package com.luvbrite.utils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.HashMap;


public abstract class PHPSerializationModified{
	
	private static Deque<Integer> stack = new ArrayDeque<Integer>();
	
	public static Object unserialize(String serializedString)
			throws IllegalStateException {
		
		String ss = serializedString;
		
		StringBuffer st = new StringBuffer().append(ss);
		if(st.charAt(0)!='a'){
			return null;
		}
		
		int arrayLength = Utility.getInteger(ss.split(":")[1]);
		
		ss = ss.substring(ss.indexOf("{")+1);
		ss = ss.substring(0, ss.length()-1); //removes the last }
		
		//System.out.println("**SS - " + ss);
		
		//now ss is the data inside the array
		Map<Object, Object> outArray = new HashMap<Object, Object>();
		int counter = 0;
		Object key = null,
				value = null;
		
		if(ss.length()<=0){
			return null;
		}
		
		while (true){
			//System.out.println("** SS - " + ss);
			char first = ss.charAt(0);
			String stringData = "";
			
			if(first == 's'){
				int sLength = Utility.getInteger(ss.split(":")[1]);
				
				int start = ("s:"+sLength+":").length()+1;				
				stringData = ss.substring(start,start+sLength);
				//System.out.println("stringData - " + stringData);
				ss = ss.substring(("s:"+sLength+":'" + stringData +"';").length());
				
				if(counter % 2 == 0)
					key = stringData;				
				else
					value = stringData;
			}
			
			else if(first == 'i'){
				int intData = Utility.getInteger(ss.substring(ss.indexOf(":")+1,ss.indexOf(";")));
				
				ss = ss.substring(("i:" + intData + ";").length());

				//System.out.println("**intData - " + ss);

				if(counter % 2 == 0)
					key = intData;				
				else
					value = intData;
			}
			
			else if(first == 'a'){
				//System.out.println("**counter - " + counter);
				if(counter % 2 == 0){
					throw new IllegalStateException();
				}

				//System.out.println("***SS to array - " + ss);
				Object vars = getArray(ss);
				if(vars!=null){
					if(vars.getClass().equals(HashMap.class)){
						int arLen = stack.pollFirst();								
						ss = ss.substring(arLen); 
					}
					
					value = vars;
				}
			}
			
			if(counter % 2 == 1){
				outArray.put(key, value);
			}
			
			counter++;
			
			if(counter/2 >= arrayLength){
				return outArray;
			}
		
		}
	}

	private static Object getArray(String ss) throws IllegalStateException {
		
		int arrayLength = Utility.getInteger(ss.split(":")[1]);		
		String ssNew = ss.substring(ss.indexOf("{")+1);
		
		//find array length
		int newArrayPos = ssNew.indexOf("{");
		if(newArrayPos == -1)
			ssNew = ssNew.substring(0, ssNew.lastIndexOf("}"));
		
		else {
			int arrayEnd = ssNew.indexOf("}");
			if(arrayEnd < newArrayPos)
				ssNew = ssNew.substring(0, arrayEnd);
			
			else{
				
				ssNew = ssNew.substring(0, ssNew.lastIndexOf("}"));
				
			}
			
		}
		
		//now ssNew is the data inside the array
		Map<Object, Object> outArray = new HashMap<Object, Object>();
		
		String theArray = "a:" + arrayLength + ":{" + ssNew + "}"; 
		stack.addFirst(theArray.length());
		
		int counter = 0;
		Object key = null,
				value = null;
		
		while (true){
			char first = ssNew.charAt(0);
			String stringData = "";
			
			if(first == 's'){
				int sLength = Utility.getInteger(ssNew.split(":")[1]);
				
				int start = ("s:"+sLength+":").length()+1;				
				stringData = ssNew.substring(start,start+sLength);

				//System.out.println("**stringData - " + stringData);
				ssNew = ssNew.substring(("s:"+sLength+":'" + stringData +"';").length());
				
				if(counter % 2 == 0)
					key = stringData;		
				else
					value = stringData;
			}
			
			else if(first == 'i'){
				int intData = Utility.getInteger(ssNew.substring(ssNew.indexOf(":")+1,ssNew.indexOf(";")));
				
				ssNew = ssNew.substring(("i:" + intData + ";").length());

				if(counter % 2 == 0)
					key = intData;				
				else
					value = intData;
			}
			
			else if(first == 'a'){
				if(counter % 2 == 0){
					throw new IllegalStateException();
				}
				
				//System.out.println("SSNEW to array - " + ssNew);

				Object vars = getArray(ssNew);
				if(vars!=null){
					if(vars.getClass().equals(HashMap.class)){
						int arLen = stack.pollFirst();						
						ssNew = ssNew.substring(arLen); 
					}
					
					value = vars;
				}
			}

			
			if(counter % 2 == 1){
				outArray.put(key, value);
			}
			
			counter++;
			
			if(counter/2 >= arrayLength){
				return outArray;
			}
		}
	}
}
