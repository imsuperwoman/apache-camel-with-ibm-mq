package com.asp.proxy.tcp;

import org.springframework.beans.factory.annotation.Value;

public class AscTcpRequest {

    private String stn;
    private byte[] rawByte;

    public AscTcpRequest(String stn, byte[] rawByte ) {
        super();
        this.stn = stn;
        this.rawByte = rawByte;
    }
    
    public String getStn() {
        return stn;
    }

	public byte[] getRawByte() {
		return rawByte;
	}

	public void setRawByte(byte[] rawByte) {
		this.rawByte = rawByte;
	}

}
