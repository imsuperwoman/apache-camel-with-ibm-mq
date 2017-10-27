package com.asp.spring;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.asp.proxy.tcp.AscTcpProxyServer;
import com.asp.proxy.tcp.AscTcpProxyServerConfig;
import com.asp.proxy.tcp.AscTcpProxyServerImpl;
import com.asp.proxy.tcp.AspTcpLogger;
import com.asp.proxy.tcp.AspToAscTcpMessageProcessor;
import com.google.common.base.Throwables;


@Component
public class AspSocketServerLauncher {

    private static final Logger LOGGER = LoggerFactory.getLogger("SocketMonitor");

    @Value("${AspTcpRequestListenPortNumber}")
    private Integer aspTcpRequestListenPortNumber;

    @Value("${AspCoreThreadCount}")
    private Integer aspCoreThreadCount;

    @Value("${AspMaxThreadCount}")
    private Integer aspMaxThreadCount;

    @Value("${AspProxyTimeout}")
    private Integer aspProxyTimeout;

    @Value("${AspQueueCapacity}")
    private Integer aspQueueCapacity;

    @Value("${AspCharsetName}")
    private String aspCharsetName;

    @Value("${AspToAsccendTcpProxyBacklog:1000}")
    private Integer aspToAsccendTcpProxyBacklog;

    @EndpointInject(ref="ascToAspIncomingMessage")
    private ProducerTemplate ascToAspIncomingMessage;

    @Autowired
    private AspToAscTcpMessageProcessor aspToAscTcpMessageProcessor;

    @Autowired
    private AspTcpLogger messageLogger;

    @PostConstruct
    public void startAspProxyServer() throws InterruptedException {

            AscTcpProxyServerConfig proxyConfig = new AscTcpProxyServerConfig();
            proxyConfig.setCoreThreadCount(aspCoreThreadCount);
            proxyConfig.setMaxThreadCount(aspMaxThreadCount);
            proxyConfig.setBacklog(aspToAsccendTcpProxyBacklog);
            proxyConfig.setQueueCapacity(aspQueueCapacity);
            proxyConfig.setCharsetName(aspCharsetName);
            proxyConfig.setTimeout(aspProxyTimeout);
            proxyConfig.setPort(aspTcpRequestListenPortNumber);
            LOGGER.info("ASP Core Thread Count: {}", proxyConfig.getCoreThreadCount());
            LOGGER.info("ASP Max Thread Count: {}", proxyConfig.getMaxThreadCount());
            LOGGER.info("ASP Queue Capacity : {}", proxyConfig.getQueueCapacity());
            LOGGER.info("ASP Thread Keep Alive Time: {}", proxyConfig.getThreadKeepAliveTime());
            LOGGER.info("ASP Thread Keep Alive Time Unit: {}", proxyConfig.getThreadKeepAliveTimeUnit());
            LOGGER.info("ASP Charset Name: {}", proxyConfig.getCharsetName());

            AscTcpProxyServer ascTcpProxyServer = new AscTcpProxyServerImpl(proxyConfig, messageLogger, ascToAspIncomingMessage);

            try {
                aspToAscTcpMessageProcessor.setAscTcpProxyServer(ascTcpProxyServer);
                ascTcpProxyServer.start();
                LOGGER.info("ASP ASCCEND TCP Proxy Server started.");
            } catch (IOException e) {
                LOGGER.error("proxy fail to wait for ready.", e);
                throw Throwables.propagate(e);
            }

        }
}

