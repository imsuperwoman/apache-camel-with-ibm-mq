package com.asp.spring.camel.converter;

import org.apache.camel.Converter;
import org.apache.camel.Exchange;

import com.asp.proxy.tcp.AscTcpResponse;

@Converter
public class AscTcpResponseToStringConverter {

	@Converter
	public static byte[] toAspTcpResponse(AscTcpResponse response, Exchange exchange) throws Exception {
		if(response == null || response.getRawMessage() == null){
			throw new IllegalArgumentException("empty message");
		}
		
		return response.getRawMessage();
	}

}
