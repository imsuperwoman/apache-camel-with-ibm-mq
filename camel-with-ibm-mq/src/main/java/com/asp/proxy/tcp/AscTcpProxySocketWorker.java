package com.asp.proxy.tcp;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asp.proxy.RequestFrameDecoder;

public class AscTcpProxySocketWorker extends Thread {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public static final String NO_SOCKET_WORKER_ID = "NO_SOCKET_WORKER";
	
	public enum Status {
		RUNNING, CLOSED
	}
	
	private String workerId;	
	private Status status;
	private Socket socket; //socket to ASCCEND
	
	private RequestFrameDecoder incomingFrameDecoder;
	private AscTcpIncomingCoordinator incomingCoordinator;
	
	private ExecutorService executors;
    private AtomicInteger currentOutgoing;
    private OutputStream os;

	public AscTcpProxySocketWorker(String workerId, Socket socket,
			RequestFrameDecoder incomingFrameDecoder,
			AscTcpIncomingCoordinator incomingCoordinator,
			ExecutorService executors) {
		super();
		this.workerId = workerId;
		this.socket = socket;
		this.incomingFrameDecoder = incomingFrameDecoder;
		this.incomingCoordinator = incomingCoordinator;
		this.currentOutgoing = new AtomicInteger(0);
		this.executors = executors;
		this.status = Status.RUNNING;
	}

	@Override
	public void run() {
		if(status != Status.RUNNING){
			log.info("worker:{} is not running", workerId);
			return;
		}
		
		try {
			os = socket.getOutputStream();
			InputStream input = socket.getInputStream(); // this input stream is from ASC Socket
			while (true) {

				byte[] incomingMessage = incomingFrameDecoder.onDecodeRequestFrame(input);
				if (incomingMessage == null) {
					break;
				}
				
				// Forward the decoded incomingMessage to incomingCoordinator.
				executors.execute(() -> {
					try{
						incomingCoordinator.addIncomingMessage(workerId, incomingMessage);
						log.debug("incoming message processed successfully by worker:{}", workerId);
					} catch (Exception e) {
						log.error("error in processing incoming message by worker:{}", workerId, e);
					}
				});
			}
		} catch (EOFException e) {
			InetSocketAddress localAddr = (InetSocketAddress) socket.getLocalSocketAddress();
			InetSocketAddress remoteAddr = (InetSocketAddress) socket.getRemoteSocketAddress();
			log.warn("EOFException occurred in {}, ending socket with: Local:{}:{}-Remote{}:{}",
					getClass().getSimpleName(), localAddr.getHostName(), localAddr.getPort(), 
					remoteAddr.getHostName(), remoteAddr.getPort(), e);
		} catch (Exception ex) {
			log.error("Error in AspProxySocketWorker", ex);
		} finally {
			close();
		}
	}
	
	public void sendOutgoingMessage(byte[] outgoingMessage) throws IOException {
		if(socket.isOutputShutdown()){
			throw new IOException("output already shut down");
		}
		
		currentOutgoing.incrementAndGet();
		try{
			os.write(outgoingMessage);
			os.flush();
		} finally{
			currentOutgoing.decrementAndGet();
		}
	}

	public void close() {
		if(status != Status.CLOSED){
			// close incoming related resources
			status = Status.CLOSED;
			incomingFrameDecoder.dispose();
			while(currentOutgoing.get() != 0){
				try{
					Thread.sleep(1000); //wait until all message write
				} catch (InterruptedException e){
					log.error("interrupted while waiting for all outgoing process completed", e);
					Thread.currentThread().interrupt();
				}
			}
			IOUtils.closeQuietly(socket);
			log.info("Socket Worker:{} closed", workerId);
		} else {
			log.info("Socket Worker:{} already closed", workerId);
		}
	}

	public String getWorkerId() {
		return workerId;
	}
	
	public boolean isClosed(){
		return status == Status.CLOSED;
	}

}
