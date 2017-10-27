package com.asp.proxy.tcp;

import java.io.IOException;

public interface AscTcpProxyServer {

	AscTcpResponse requestAndResponseToAsc(AscTcpRequest request) throws IOException;
		
	AspTcpResponse requestAndResponseToAsp(AspTcpRequest request) throws IOException;
	
	void start() throws IOException;

	void pushOnlyToAsp(AspTcpRequest request) throws IOException;
	
}
