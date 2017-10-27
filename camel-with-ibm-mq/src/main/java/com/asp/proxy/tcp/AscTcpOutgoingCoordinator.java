package com.asp.proxy.tcp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asp.spring.camel.converter.AscTcpRequestConverter;
import com.google.common.base.Throwables;
import com.google.common.primitives.Bytes;

public class AscTcpOutgoingCoordinator {

	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
    private ConcurrentMap<String, AscTcpProxySocketWorker> socketWorkers;
    private AspTcpLogger messageLogger;
    private String charsetName;

    public AscTcpOutgoingCoordinator(
            ConcurrentMap<String, AscTcpProxySocketWorker> socketWorkers,
            AspTcpLogger messageLogger,
            String charsetName) {
        super();
        this.socketWorkers = socketWorkers;
        this.messageLogger = messageLogger;
        this.charsetName = charsetName;
    }

    /**
     * send randomly if no socket worker is specified
     *
     */
    
    public void sendOutgoingMessage(byte [] message) throws IOException  {
        AscTcpProxySocketWorker worker = getNextSocketWorker();
        if(worker != null){
            sendOutgoingMessage(worker.getWorkerId(), message);
        } else {
            IOException e = new IOException("No Available Connection");
            messageLogger.logOutgoingMessage(new OutgoingMessageEvent(AscTcpProxySocketWorker.NO_SOCKET_WORKER_ID, mqEncode(message), new Date(), e));
            throw e;
        }
    }

    private String mqEncode(byte[] message) {
		try {
			return new String(message, "CP037");
		} catch (Exception e) {
			log.error("Encoding error : {}", e);
		}
		return null;
	}

	/**
     * send to the specified socket worker
     *
     */
    
    public void sendOutgoingMessage(String workerId, byte [] message) throws IOException{
        Date timestamp = new Date();
        try{
            AscTcpProxySocketWorker worker = getSocketWorker(workerId);
            worker.sendOutgoingMessage(encodeMessage(message));
            messageLogger.postOutgoingMessage(new OutgoingMessageEvent(workerId, mqEncode(message), timestamp));
        } catch(Exception e){
            messageLogger.postOutgoingMessage(new OutgoingMessageEvent(workerId, mqEncode(message), timestamp, e));
            throw e;
        }
    }

    private AscTcpProxySocketWorker getSocketWorker(String workerId){
        AscTcpProxySocketWorker worker = socketWorkers.get(workerId);
        if(worker == null){
            throw new IllegalStateException("unknown socket worker:" + workerId);
        }

        if(worker.isClosed()){
            throw new IllegalStateException("worker already closed");
        }
        return worker;
    }

    private List<AscTcpProxySocketWorker> getAvailableSocketWorkers(){
        return socketWorkers.values().stream().filter(sw -> !sw.isClosed()).collect(Collectors.toList());
    }

    //caller to handle if no available socket worker
    private AscTcpProxySocketWorker getNextSocketWorker(){
        List<AscTcpProxySocketWorker> workers = getAvailableSocketWorkers();
        if(workers.isEmpty()){
            return null;
        }
        return workers.get(RandomUtils.nextInt(0, workers.size()));
    }

    private byte[] encodeMessage( byte[]  message) {
            int msgLenth = message.length;
            byte[] lenBytes = new byte[] { (byte) (msgLenth >> 8), (byte) msgLenth };
            return Bytes.concat(lenBytes, message);

    }

}

