package com.asp.proxy.tcp;


public class AspTcpResponse {

	private String stn;
	byte[]  rawMessage;
	private String charset;
		
	public AspTcpResponse(String stn, byte[] rawMessage, String charset) {
		super();
		this.stn = stn;
		this.rawMessage = rawMessage;
		this.charset = charset;
	}
	
	public String getStn() {
		return stn;
	}
	public byte[] getRawMessage() {
		return rawMessage;
	}
	public String getCharset() {
		return charset;
	}
	
}
