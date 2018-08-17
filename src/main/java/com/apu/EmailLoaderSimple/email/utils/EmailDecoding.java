package com.apu.EmailLoaderSimple.email.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.apu.EmailLoaderSimple.utils.string.StringUtils;

public class EmailDecoding {
    
    private static Logger LOGGER = LogManager.getLogger(EmailDecoding.class.getName());

    private static String START_ENCODED_SYMBOLS = "=?";
    private static String END_ENCODED_SYMBOLS = "?=";
    private static String KOI8R_ENCODING = "KOI8-R";
    private static String KOI8R_START_ROW = "KOI8-R?B?";
    private static String UTF8_ENCODING = "UTF-8";
    private static String UTF8B_START_ROW = "UTF-8?B?";
    private static String UTF8Q_START_ROW = "UTF-8?Q?";
    private static String ISO8859_5_ENCODING = "ISO-8859-5";
    private static String ISO8859_5_START_ROW = "ISO-8859-5?B?";
    private static String US_ASCIIQ_START_ROW = "US-ASCII?Q?";
    
    public static String chechFileNameCoding(String fileName) throws IOException {
        if(fileName == null)
            return null;
        String str = getDecodedStr(fileName);
        return StringUtils.removePunct(str);     
    }
    
    public static String getDecodedStr(String str) throws UnsupportedEncodingException {
        if(str == null)
            return null;
        RowPart findedPart;
        do {
            findedPart = getNextEncodedPart(str, START_ENCODED_SYMBOLS, END_ENCODED_SYMBOLS);
            if((findedPart != null)&&(findedPart.encoded == true)) {
                findedPart.strDecoded = decodePart(findedPart.str);
                str = replaceEncodedPart(str, findedPart);
                int startOfReplacedFragment = str.indexOf(findedPart.strDecoded);
                if((startOfReplacedFragment != -1) && (startOfReplacedFragment != 0)) {
                    if(str.charAt(startOfReplacedFragment - 1) == ' ') {
                        str = str.substring(0, startOfReplacedFragment - 1) +
                                str.substring(startOfReplacedFragment, str.length());
                    }
                }
            }
        } while(findedPart != null);
//        str = checkForErrorDecoding(str);
        return str;
    }
    
    private static RowPart getNextEncodedPart(String str, String startStr, String endStr) {
        int AMOUNT_OF_QUESTION_SYMBOLS = 2;
        int startIndex = str.indexOf(startStr);
        if (startIndex == -1)
            return null;
        int tempIndex = startIndex + startStr.length();
        for(int i=0; i<AMOUNT_OF_QUESTION_SYMBOLS; i++) {
            tempIndex = str.indexOf("?", tempIndex + 1);
            if(tempIndex == -1)
                return null;
        }
        int endIndex = str.indexOf(endStr, tempIndex + 1);
        if (endIndex == -1)
            return null;
        String subStr = str.substring(startIndex + startStr.length(), endIndex);        
        RowPart retPart = new RowPart(subStr, true, startIndex, endIndex + endStr.length());        
        return retPart;
    }
    
    private static String decodePart(String str) {
        if (str.length() < 5)
                return str;
        String retValue = null;
        if (str.toUpperCase().contains(KOI8R_START_ROW)) {        
            String substring = str.substring(KOI8R_START_ROW.length());
            byte[] decoded = Base64.getDecoder().decode(substring);
            try {
                String koiStr = new String(decoded, KOI8R_ENCODING);
                decoded = koiStr.getBytes(UTF8_ENCODING);
                retValue = new String(decoded, UTF8_ENCODING);
            } catch (UnsupportedEncodingException e) {
                LOGGER.error(ExceptionUtils.getStackTrace(e));
            }                                  
        } else if (str.toUpperCase().contains(UTF8B_START_ROW)) { 
            String substring = str.substring(UTF8B_START_ROW.length());
            byte[] decoded = Base64.getDecoder().decode(substring);
            try {
                retValue = new String(decoded, UTF8_ENCODING);
            } catch (UnsupportedEncodingException e) {
                LOGGER.error(ExceptionUtils.getStackTrace(e));
            }
        } else if (str.toUpperCase().contains(ISO8859_5_START_ROW)) { 
            String substring = str.substring(ISO8859_5_START_ROW.length());
            byte[] decoded = Base64.getDecoder().decode(substring);
            try {
                retValue = new String(decoded, ISO8859_5_ENCODING);
            } catch (UnsupportedEncodingException e) {
                LOGGER.error(ExceptionUtils.getStackTrace(e));
            }
        } else if (str.toUpperCase().contains(UTF8Q_START_ROW)) { 
            String substring = str.substring(UTF8Q_START_ROW.length());
            retValue = substring;
        } else if (str.toUpperCase().contains(US_ASCIIQ_START_ROW)) { 
            String substring = str.substring(US_ASCIIQ_START_ROW.length());
            retValue = usAsciiToString(substring);
        } else { 
            return str;
        }        
        return retValue;
    }
    
    private static String usAsciiToString(String inputStr) {
        String retValue = null;
        char findSymbol = '=';
        int startPosition = 0;
        int tempPosition = 0;
        byte[] inputStrBytes = inputStr.getBytes();
        StringBuilder sb = new StringBuilder();
        while(startPosition < inputStr.length()) {
            tempPosition = startPosition;
            startPosition = inputStr.indexOf(findSymbol, startPosition);
            if(startPosition == -1) {
                sb.append(inputStr.substring(tempPosition));
                break;
            }
            if(startPosition >= (inputStr.length() - 2)) {
                sb.append(inputStr.substring(tempPosition));
                break;
            }            
            byte[] codeBytes = new byte[2];
            codeBytes[0] = inputStrBytes[startPosition + 1];
            codeBytes[1] = inputStrBytes[startPosition + 2];
            
            if(tempPosition != startPosition)
                sb.append(inputStr.substring(tempPosition, startPosition));
            
            String asciiCode = new String(codeBytes);
            try {
                int asciiCodeInt = Integer.parseInt(asciiCode, 16);
                String str = String.valueOf(Character.toChars(asciiCodeInt));
                sb.append(str);
            } catch(NumberFormatException e) {
                sb.append(inputStr.substring(startPosition, startPosition + 3));
            }
            
            startPosition += 3;
        }       
        retValue = sb.toString();        
        return retValue;
    }
    
    private static String replaceEncodedPart(String strSrc, RowPart rowPart) {
        StringBuilder sb = new StringBuilder();
        sb.append(strSrc.substring(0, rowPart.startPosition));
        sb.append(rowPart.strDecoded);
        sb.append(strSrc.substring(rowPart.endPosition, strSrc.length()));
        return sb.toString();
    }
    
    private static class RowPart {
        String str;
        boolean encoded;
        String strDecoded;
        int startPosition;
        int endPosition;
        
        public RowPart(String str, boolean encoded, int startPosition, int endPosition) {
            this.str = str;
            this.encoded = encoded;
            this.startPosition = startPosition;
            this.endPosition = endPosition;
        }        
    }
    
    public static void main(String[] args) throws UnsupportedEncodingException {
        String inputStr = "karlova=5Ft=5Fm=5Fthe=5Fworld=5Fof=5Finformation=5Fs";        
        String ret = usAsciiToString(inputStr);
        inputStr = "=?us-ascii?q?FigureRemake2UMLDiagrams=5F20170210=5F1905?= =?us-ascii?q?=2Ezip?=";
        ret = getDecodedStr(inputStr);
        inputStr = "=?us-ascii?q?26=2E12=2E2017cb65e54c-1609-4d6e-ac54-7a2b?= =?us-ascii?q?9e7b2c66=2Epdf?=";
        ret = getDecodedStr(inputStr);
        System.out.println(ret);
    }
    
}
