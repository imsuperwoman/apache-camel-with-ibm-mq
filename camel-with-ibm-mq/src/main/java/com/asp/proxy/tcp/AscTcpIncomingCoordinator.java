package com.asp.proxy.tcp;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tcp.common.AscMessageUtils;

public class AscTcpIncomingCoordinator {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

    private String charsetName;
    private AspTcpLogger messageLogger;
    private ConcurrentMap<String, LinkedBlockingQueue<AscTcpResponse>> requestKeeper;
    private AscTcpProxyServer server;
    private AscTcpOutgoingCoordinator outgoingCoordinator;

    public AscTcpIncomingCoordinator(
            AscTcpProxyServer server,
            ConcurrentMap<String, LinkedBlockingQueue<AscTcpResponse>> requestKeeper,
            AspTcpLogger messageLogger,
            String charsetName) 
            {
        super();
        this.server = server;
        this.requestKeeper = requestKeeper;
        this.messageLogger = messageLogger;
        this.charsetName = charsetName;
    }

    /**
     * check if the message is valid
     * if valid, check if the message is request or response by the stn
     *
     */
    public void addIncomingMessage(String workerId, byte[] incomingFrame) throws IOException {
        Date timestamp = new Date();

        String incomingMessage = StringUtils.newString(ArrayUtils.subarray(incomingFrame, 2, incomingFrame.length), charsetName);
        messageLogger.postIncomingMessage(new IncomingMessageEvent(workerId, incomingMessage, timestamp));
        //remove 2 byte in font
        byte[] incomingArray = Arrays.copyOfRange(incomingFrame, 2, incomingFrame.length);
        String stn = AscMessageUtils.getStn(incomingMessage);
        if(isAscRequestExist(stn)){
            boolean ret = requestKeeper.get(stn).offer(new AscTcpResponse(stn, incomingArray, charsetName));
            if(!ret){
                throw new IOException("cannot add response to request keeper, stn:" + stn);
            }
        } else {
            AspTcpResponse aspResp = server.requestAndResponseToAsp(new AspTcpRequest(incomingMessage, charsetName));
            outgoingCoordinator.sendOutgoingMessage(workerId, aspResp.getRawMessage());
        }
    }

    private boolean isAscRequestExist(String stn){
        return requestKeeper.containsKey(stn);
    }

}
