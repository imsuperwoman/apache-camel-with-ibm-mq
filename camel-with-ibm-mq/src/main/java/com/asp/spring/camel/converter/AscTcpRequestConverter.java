package com.asp.spring.camel.converter;

import java.io.UnsupportedEncodingException;

import org.apache.camel.Converter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asp.proxy.tcp.AscTcpRequest;
import com.tcp.common.AscMessageUtils;

@Converter
public class AscTcpRequestConverter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AscTcpRequestConverter.class);
	
    @Converter
    public static AscTcpRequest toAscTcpRequest(byte[] message) throws UnsupportedEncodingException {

    	String afterEncode = new String(message, "Cp037");
    	LOGGER.info("Receive:{} ", afterEncode.toString().trim());

        if(StringUtils.isEmpty(afterEncode)){
        throw new IllegalArgumentException("receive empty message");
        }
    	
        return AscMessageUtils.formRequest(afterEncode, message);
    }

}
