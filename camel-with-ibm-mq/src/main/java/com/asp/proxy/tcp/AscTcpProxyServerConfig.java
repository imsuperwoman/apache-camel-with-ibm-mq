package com.asp.proxy.tcp;

import java.util.concurrent.TimeUnit;

public class AscTcpProxyServerConfig {

	private int port;
	private int backlog;
	private String charsetName;
	private long timeout;
	private int coreThreadCount;
	private int maxThreadCount;
	private long threadKeepAliveTime;
	private TimeUnit threadKeepAliveTimeUnit = TimeUnit.SECONDS;
	private int queueCapacity;

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getBacklog() {
		return backlog;
	}

	public void setBacklog(int backLog) {
		this.backlog = backLog;
	}

	public String getCharsetName() {
		return charsetName;
	}

	public void setCharsetName(String charsetName) {
		this.charsetName = charsetName;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public int getCoreThreadCount() {
		return coreThreadCount;
	}

	public void setCoreThreadCount(int coreThreadCount) {
		this.coreThreadCount = coreThreadCount;
	}

	public int getMaxThreadCount() {
		return maxThreadCount;
	}

	public void setMaxThreadCount(int maxThreadCount) {
		this.maxThreadCount = maxThreadCount;
	}

	public long getThreadKeepAliveTime() {
		return threadKeepAliveTime;
	}

	public void setThreadKeepAliveTime(long threadKeepAliveTime) {
		this.threadKeepAliveTime = threadKeepAliveTime;
	}

	public TimeUnit getThreadKeepAliveTimeUnit() {
		return threadKeepAliveTimeUnit;
	}

	public void setThreadKeepAliveTimeUnit(TimeUnit threadKeepAliveTimeUnit) {
		this.threadKeepAliveTimeUnit = threadKeepAliveTimeUnit;
	}

	public int getQueueCapacity() {
		return queueCapacity;
	}

	public void setQueueCapacity(int queueCapacity) {
		this.queueCapacity = queueCapacity;
	}
}
