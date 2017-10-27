package com.asp.proxy.tcp;


public class AscTcpResponse {

	private String stn;
	private byte[] rawMessage;
	private String charsetName;
		
	public AscTcpResponse(String stn, byte[] rawMessage, String charsetName) {
		super();
		this.stn = stn;
		this.rawMessage = rawMessage;
		this.charsetName = charsetName;
	}
	
	public String getStn() {
		return stn;
	}
	public byte[] getRawMessage() {
		return rawMessage;
	}
	public String getCharsetName() {
		return charsetName;
	}
	
}
