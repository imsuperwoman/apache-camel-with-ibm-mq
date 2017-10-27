package com.asp.proxy.tcp;

import java.util.Date;

public abstract class AbstractTcpMessageEvent {

	private boolean garbage;
	private String socketWorkerId;
	private String rawMessage;
	private Date timestamp;
	private Exception exception;
	
	public AbstractTcpMessageEvent(String socketWorkerId, String rawMessage, Date timestamp) {
		super();
		this.socketWorkerId = socketWorkerId;
		this.rawMessage = rawMessage;
		this.timestamp = timestamp;
		this.garbage = false;
	}	
	public AbstractTcpMessageEvent(String socketWorkerId, String rawMessage, Date timestamp,
			Exception exception) {
		super();
		this.socketWorkerId = socketWorkerId;
		this.rawMessage = rawMessage;
		this.timestamp = timestamp;
		this.exception = exception;
		this.garbage = false;
	}
	public AbstractTcpMessageEvent(boolean garbage, String socketWorkerId, String rawMessage, Date timestamp,
			Exception exception) {
		super();
		this.socketWorkerId = socketWorkerId;
		this.rawMessage = rawMessage;
		this.timestamp = timestamp;
		this.exception = exception;
		this.garbage = garbage;
	}
	
	public String getSocketWorkerId() {
		return socketWorkerId;
	}
	public String getRawMessage() {
		return rawMessage;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public Exception getException() {
		return exception;
	}
	public boolean isGarbage() {
		return garbage;
	}
	
}
