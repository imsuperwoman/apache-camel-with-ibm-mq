package com.tcp.common;

import java.nio.charset.Charset;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import com.asp.proxy.tcp.AscTcpRequest;
import com.asp.proxy.tcp.AscTcpResponse;
import com.google.common.base.Throwables;

public class AscMessageUtils {

    private static final FastDateFormat ASCCEND_DATE_FORMAT;

    static {
        ASCCEND_DATE_FORMAT = FastDateFormat.getInstance("HHmmssddMMyy");
    }

    private AscMessageUtils(){}

    public static String getService(String message){
        return StringUtils.substring(message, 23, 29);
    }

    public static String getClientId(String message){
        return StringUtils.substring(message, 16, 36);
    }

    public static String getDatetime(String message){
        return StringUtils.substring(message, 35, 47);
    }

    public static String getStn(String message){
        return StringUtils.defaultString(StringUtils.substring(message, 23, 59), "");

    }

    public static String getReturnCode(String message){
        return StringUtils.substring(message, 0, 4);
    }

    public static Date getDatetimeAsDate(String message){
        try{
            return AscMessageUtils.ASCCEND_DATE_FORMAT.parse(getDatetime(message) + "0");
        } catch (Exception e){
            throw Throwables.propagate(e);
        }
    }

    public static boolean isResponse(String message){
        return !isRequest(message);
    }

    public static boolean isRequest(String message){
    	if (getReturnCode(message).contains("0600"))
    		return true;
        return false;
    }

    public static String getMessageBody(String message){
        return message.substring(220, message.length() - 4);
    }

    public static AscTcpResponse formResponse(String afterEncode , byte[] message){
        return formResponse(afterEncode, message, Charset.defaultCharset().name());
    }

    public static AscTcpRequest formRequest(String afterEncode, byte[] rawData){
    	  
        return new AscTcpRequest(AscMessageUtils.getStn(afterEncode), rawData) ;
    }

    public static AscTcpResponse formResponse(String afterEncode , byte[] message, String charsetName){
    	
        return new AscTcpResponse(getStn(afterEncode), message, charsetName);
    }

}
