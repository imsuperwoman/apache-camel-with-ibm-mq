package com.asp.proxy.tcp;

import java.util.Date;

public class IncomingMessageEvent extends AbstractTcpMessageEvent {

	public IncomingMessageEvent(boolean garbage, String socketWorkerId,
			String rawMessage, Date timestamp, Exception exception) {
		super(garbage, socketWorkerId, rawMessage, timestamp, exception);
	}

	public IncomingMessageEvent(String socketWorkerId, String rawMessage,
			Date timestamp, Exception exception) {
		super(socketWorkerId, rawMessage, timestamp, exception);
	}

	public IncomingMessageEvent(String socketWorkerId, String rawMessage,
			Date timestamp) {
		super(socketWorkerId, rawMessage, timestamp);
	}

}
