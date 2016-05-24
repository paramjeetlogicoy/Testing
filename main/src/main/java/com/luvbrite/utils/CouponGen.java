package com.luvbrite.utils;

import java.util.Random;

public class CouponGen {

	protected static Random r = new Random();
	
	public static String getNewCoupon(int min) {
		String newCoupon = "";
		
		try {
			newCoupon = getWord(min);
		}
		catch (Exception e) {
			newCoupon = "";
			e.printStackTrace();
		}
		
		return newCoupon;
	}

	  /*
	   * Set of characters that is valid. Must be printable, memorable, and "won't
	   * break HTML" (i.e., not ' <', '>', '&', '=', ...). or break shell commands
	   * (i.e., not ' <', '>', '$', '!', ...). I, L and O are good to leave out,
	   * as are numeric zero and one.
	   */
	  protected static char[] goodChar = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K',
	      'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
	      '2', '3', '4', '5', '6', '7', '8', '9'};

	  /* Generate a random word. */
	  private static String getWord(int minLength) {
		  
	    StringBuffer sb = new StringBuffer();
	    
	    for (int i = 0; i < minLength; i++) {
	      sb.append(goodChar[r.nextInt(goodChar.length)]);
	    }
	    
	    return sb.toString();
	  }	
}