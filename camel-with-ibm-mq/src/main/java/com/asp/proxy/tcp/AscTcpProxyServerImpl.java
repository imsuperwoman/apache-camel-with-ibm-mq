package com.asp.proxy.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asp.proxy.AscToAspProxyDecoder;
import com.google.common.collect.Maps;

public class AscTcpProxyServerImpl implements AscTcpProxyServer, Runnable {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

    private ServerSocket ascServerSocket;
    private ProducerTemplate ascToAspMessageProducer;
    private AscTcpProxyServerConfig config;
    private ExecutorService executors;
    private AscTcpIncomingCoordinator incomingCoordinator;
    private AscTcpOutgoingCoordinator outgoingCoordinator;
    private ConcurrentMap<String, LinkedBlockingQueue<AscTcpResponse>> requestKeeper;
    private ConcurrentMap<String, AscTcpProxySocketWorker> socketWorkers;
    private Thread workerThread;
    public AscTcpProxyServerImpl(AscTcpProxyServerConfig config, AspTcpLogger messageLogger, ProducerTemplate ascToAspMessageProducer) {
        super();
        this.config = config;
        this.requestKeeper = Maps.newConcurrentMap();
        this.socketWorkers = Maps.newConcurrentMap();
        this.outgoingCoordinator = new AscTcpOutgoingCoordinator(socketWorkers, messageLogger, config.getCharsetName());
        this.incomingCoordinator = new AscTcpIncomingCoordinator(this, requestKeeper, messageLogger, config.getCharsetName());
        this.ascToAspMessageProducer = ascToAspMessageProducer;
        this.executors =  new ThreadPoolExecutor(config.getCoreThreadCount(), config.getMaxThreadCount(),
                config.getThreadKeepAliveTime(), config.getThreadKeepAliveTimeUnit(),
                new LinkedBlockingQueue<Runnable>(config.getQueueCapacity()),
                new CallerRunsPolicy());
    }

    public synchronized void close() throws InterruptedException, IOException {
        close(0);
    }

    public synchronized void close(long timeout) throws InterruptedException, IOException {
        Thread tmp = workerThread;
        if (tmp == null) {
            return;
        }
        workerThread = null;
        ascServerSocket.close();
        tmp.interrupt();
        tmp.join(timeout);
        log.info(this.getClass().getName() + " Close");
    }

    @Override
    public void pushOnlyToAsp(AspTcpRequest request) throws IOException {

        ascToAspMessageProducer.requestBody(request);
    }

    @Override
    public AscTcpResponse requestAndResponseToAsc(AscTcpRequest request) throws IOException  {
        String stn = request.getStn();

        LinkedBlockingQueue<AscTcpResponse> responseQueue = new LinkedBlockingQueue<>(1);
        requestKeeper.put(stn, responseQueue);

        try{
            outgoingCoordinator.sendOutgoingMessage(request.getRawByte());
            AscTcpResponse response = responseQueue.poll(config.getTimeout(), TimeUnit.SECONDS);
            // handle the timeout
            if (response == null) {
                throw new IOException("polling response timeout for stn:" + stn);
            }
            log.info("AscTcpResponse {}" + new String (response.getRawMessage(), "cp1047"));
            return response;
        } catch (InterruptedException ie){
            Thread.currentThread().interrupt();
            throw new IOException("interrupted while polling response for stn:" + stn, ie);
        }
    }
    
    @Override
    public AspTcpResponse requestAndResponseToAsp(AspTcpRequest request)
            throws IOException {

        try{
            //sync method
            Object responseMessage = ascToAspMessageProducer.requestBody(request);
            if(!AspTcpResponse.class.isInstance(responseMessage)){
                throw new RuntimeException("unknown response " + ToStringBuilder.reflectionToString(responseMessage));
            }

            return (AspTcpResponse) responseMessage;
        } catch (CamelExecutionException e){
            throw new IOException("error in getting response", e);
        }
    }

    @Override
    public void run() {
    	log.info("Starting {}, listen at: {}:{}", this.getClass().getName(), config.getPort());
        socketWorkers.clear();

        try {
            while (Thread.currentThread() == workerThread) {
                Socket socket = ascServerSocket.accept();
                InetSocketAddress localAddr = (InetSocketAddress) socket.getLocalSocketAddress();
                InetSocketAddress remoteAddr = (InetSocketAddress) socket.getRemoteSocketAddress();
                String workerId = String.format("%s:%s-%s:%s", localAddr.getHostName(), localAddr.getPort(), remoteAddr.getHostName(), remoteAddr.getPort());
                AscToAspProxyDecoder decoder = new AscToAspProxyDecoder();
                AscTcpProxySocketWorker worker = new AscTcpProxySocketWorker(workerId, socket, decoder, incomingCoordinator, executors);
                worker.start();
                socketWorkers.put(workerId, worker);

                log.info("New connection with client# " + workerThread + " at " + socket);
            }
            log.info("{} thread ran to end.", this.getClass().getName());
        } catch (Exception ex) {
        	log.error("Error when running AspProxyServer.", ex);
        } finally {
            if (Thread.currentThread() != workerThread) {
                // stopping, cleaning up.
                for (AscTcpProxySocketWorker worker : socketWorkers.values()) {
                    if(!worker.isClosed()){
                        worker.close();
                    }
                }
                log.info("{} thread shutdown.", this.getClass().getName());
            }
        }
    }

    @Override
    public synchronized void start() throws IOException {
        if (workerThread != null) {
            return;
        }

        ascServerSocket = new ServerSocket(config.getPort(), config.getBacklog());
        workerThread = new Thread(this, "AscTcpProxyServerImpl");
        workerThread.setDaemon(true);
        workerThread.start();
        log.info(this.getClass().getName() +" Thread Start" );
    }

}
