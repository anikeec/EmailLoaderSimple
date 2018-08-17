package com.apu.EmailLoaderSimple.utils.string;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StringUtils {

	//private static final String PUNCT = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";

	private static final String PUNCT_SUBJECT = "!\"#$%&'()*+,/:;<>?@[\\]^`{|}~";
	
    public static String removePunct(String str) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (PUNCT_SUBJECT.indexOf(c) < 0) {
                result.append(c);
            }
        }
        return result.toString();
    }
    
    public static String removePunct(String str, String pattern) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (pattern.indexOf(c) < 0) {
                result.append(c);
            }
        }
        return result.toString();
    }
    
    public static String lengthRestriction(String str, int maxLength) {
        if(str.length() >= maxLength) {
            str = str.substring(0, maxLength);
        }
        return str;
    }
    
    public static String dateFormat(Date date) {        
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss");
        return dateFormat.format(date);
    }
    
    public static Date dateFormat(String date) {        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss");
        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            return new Date(0);
        }
    }
	
}
