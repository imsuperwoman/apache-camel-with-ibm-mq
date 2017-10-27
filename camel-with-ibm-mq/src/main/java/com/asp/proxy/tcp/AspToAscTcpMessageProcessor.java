package com.asp.proxy.tcp;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

public class AspToAscTcpMessageProcessor implements Processor{
	private final Logger log = LoggerFactory.getLogger(this.getClass());
    private AscTcpProxyServer ascTcpProxyServer;

    @Override
    public void process(Exchange exchange) throws Exception {
    	
        AscTcpRequest request = (AscTcpRequest) exchange.getIn().getBody();

        try
        {
        	log.debug("incoming message:{}", exchange.getIn().getBody());
            AscTcpResponse response = sendAndReceive(request);
            log.debug("outgoing message:{}", response);
            exchange.getOut().setBody(response.getRawMessage());
        }
        catch(Exception e)
        {
            exchange.setException(e);
        }
    }

    private AscTcpResponse sendAndReceive(AscTcpRequest request){
        try
        {
            if(ascTcpProxyServer == null){
                throw new IllegalStateException("tcp proxy server is not set");
            }

            return ascTcpProxyServer.requestAndResponseToAsc(request);
        }
        catch(Exception e)
        {
          throw Throwables.propagate(e);
        }
    }

    public AscTcpProxyServer getAscTcpProxyServer() {
        return ascTcpProxyServer;
    }

    public void setAscTcpProxyServer(AscTcpProxyServer ascTcpProxyServer) {
        this.ascTcpProxyServer = ascTcpProxyServer;
    }

}
