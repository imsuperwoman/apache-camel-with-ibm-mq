package com.asp.proxy.tcp;

import java.util.Date;

public class OutgoingMessageEvent extends AbstractTcpMessageEvent {

	public OutgoingMessageEvent(boolean garbage, String socketWorkerId,
			String rawMessage, Date timestamp, Exception exception) {
		super(garbage, socketWorkerId, rawMessage, timestamp, exception);
	}

	public OutgoingMessageEvent(String socketWorkerId, String rawMessage,
			Date timestamp, Exception exception) {
		super(socketWorkerId, rawMessage, timestamp, exception);
	}

	public OutgoingMessageEvent(String socketWorkerId, String rawMessage,
			Date timestamp) {
		super(socketWorkerId, rawMessage, timestamp);
	}	

}
