package com.asp.proxy.tcp;


public class AspTcpRequest {

	private String rawMessage;
	private String charsetName;
		
	public AspTcpRequest(String rawMessage, String charsetName) {
		super();
		this.rawMessage = rawMessage;
		this.charsetName = charsetName;
	}
	
	public String getRawMessage() {
		return rawMessage;
	}
	public String getCharsetName() {
		return charsetName;
	}
	
}
