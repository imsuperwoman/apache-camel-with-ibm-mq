package com.asp.proxy.tcp;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.asp.proxy.tcp.TcpMessageLog.Direction;
import com.asp.proxy.tcp.TcpMessageLog.Result;
import com.asp.proxy.tcp.TcpMessageLog.Type;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.tcp.common.AscMessageUtils;

@Service
public class AspTcpLogger {

	private final Logger log = LoggerFactory.getLogger("tcpLogger");

    @Value("${AspInstanceId}")
    private String aspInstanceId;

   // @Autowired
   // private PlatformTransactionManager aspTransactionManager;

   // @Autowired
   // private TcpProxyMessageLogDao tcpProxyMessageLogDao;

    private Executor executor;
    private EventBus incomingMessageEventBus;
    private EventBus outgoingMessageEventBus;

    public AspTcpLogger() {
        super();
        this.executor = Executors.newFixedThreadPool(16);
        this.incomingMessageEventBus = new AsyncEventBus("incomingMessageEventBus", executor);
        this.outgoingMessageEventBus = new AsyncEventBus("outgoingMessageEventBus", executor);
        this.incomingMessageEventBus.register(this);
        this.outgoingMessageEventBus.register(this);
    }

    public void postIncomingMessage(IncomingMessageEvent event){
        incomingMessageEventBus.post(event);
    }

    public void postOutgoingMessage(OutgoingMessageEvent event){
        outgoingMessageEventBus.post(event);
    }

    @AllowConcurrentEvents
    @Subscribe
    public void logIncomingMessage(IncomingMessageEvent event) {
        TcpMessageLog log = toLog(event);
        doInsertLog(log);
    }

    @AllowConcurrentEvents
    @Subscribe
    public void logOutgoingMessage(OutgoingMessageEvent event) {
        TcpMessageLog log = toLog(event);
        doInsertLog(log);
    }

    private void doInsertLog(TcpMessageLog msgLog){
        try{
           // new TransactionTemplate(aspTransactionManager).execute(ts -> {
           //     return tcpProxyMessageLogDao.create(msgLog);
           // });
        	log.info(msgLog.toString());
        } catch (Exception e){
        	log.error("cannot insert log", e);
        }
    }

    private TcpMessageLog toLog(OutgoingMessageEvent event){
        TcpMessageLog msgLog = _toLog(event);
        msgLog.setDirection(Direction.OUTGOING);
        return msgLog;
    }

    private TcpMessageLog toLog(IncomingMessageEvent event){
        TcpMessageLog msgLog = _toLog(event);
        msgLog.setDirection(Direction.INCOMING);
        return msgLog;
    }

    private TcpMessageLog _toLog(AbstractTcpMessageEvent event){
        TcpMessageLog log = new TcpMessageLog();
        String rawMessage = event.getRawMessage();

        log.setSocketWorkerId(event.getSocketWorkerId());
        log.setTimestamp(event.getTimestamp());
        log.setInstanceId(aspInstanceId);
        log.setRawMessage(event.getRawMessage());

        if(event.isGarbage()){
            log.setType(Type.GARBAGE);
        } else {
            log.setServiceId(AscMessageUtils.getService(rawMessage));
            log.setAsccendDatetime(AscMessageUtils.getDatetime(rawMessage));
            log.setStn(AscMessageUtils.getStn(rawMessage));
            log.setReturnCode(AscMessageUtils.getReturnCode(rawMessage));
            log.setClientId(AscMessageUtils.getClientId(rawMessage));

            if(AscMessageUtils.isRequest(rawMessage)){
                log.setType(Type.REQUEST);
            } else {
                log.setType(Type.RESPONSE);
            }
        }

        if(event.getException() != null){
            log.setException(event.getException().getMessage());
            log.setResult(Result.FAIL);
        } else {
            log.setResult(Result.SUCCESS);
        }

        return log;
    }

    @AllowConcurrentEvents
    @Subscribe
    public void handleDeadEvents(DeadEvent event){
    	log.info("no subcriber is defined, discard the event", event);
    }

}
