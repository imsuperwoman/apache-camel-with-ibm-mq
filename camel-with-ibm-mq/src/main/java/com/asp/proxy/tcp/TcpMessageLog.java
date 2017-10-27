package com.asp.proxy.tcp;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.awf.spring.model.AspPersistentObject;

public class TcpMessageLog extends AspPersistentObject{

	public static final String TABLE_NAME = "ASP_TCP_PROXY_LOG";
	
	public enum Result {
		SUCCESS, FAIL
	}
	
	public enum Type {
		REQUEST, RESPONSE, GARBAGE
	}
	
	public enum Direction {
		INCOMING, OUTGOING
	}
	
	private String instanceId;
	private String socketWorkerId;
	private Date timestamp;
	private String stn;
	private String serviceId;
	private String clientId;
	private String returnCode;
	private String asccendDatetime;
	private String rawMessage;
	private Direction direction;
	private Type type;
	private Result result;
	private String exception;
	
	public String getInstanceId() {
		return instanceId;
	}
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	
	public String getSocketWorkerId() {
		return socketWorkerId;
	}
	public void setSocketWorkerId(String socketWorkerId) {
		this.socketWorkerId = socketWorkerId;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	public String getStn() {
		return stn;
	}
	public void setStn(String stn) {
		this.stn = stn;
	}
	
	public String getServiceId() {
		return serviceId;
	}
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}
	
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
	public String getReturnCode() {
		return returnCode;
	}
	public void setReturnCode(String returnCode) {
		this.returnCode = returnCode;
	}
	
	public String getAsccendDatetime() {
		return asccendDatetime;
	}
	public void setAsccendDatetime(String asccendDatetime) {
		this.asccendDatetime = asccendDatetime;
	}
	
	public String getRawMessage() {
		return rawMessage;
	}
	public void setRawMessage(String rawMessage) {
		this.rawMessage = rawMessage;
	}
	
	public Direction getDirection() {
		return direction;
	}
	public void setDirection(Direction direction) {
		this.direction = direction;
	}
	
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	
	public Result getResult() {
		return result;
	}
	public void setResult(Result result) {
		this.result = result;
	}
	
	public String getException() {
		return exception;
	}
	public void setException(String exception) {
		this.exception = exception;
	}
	@Override
	public String toString() {
	
		SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ssss");
		
		
		return " [ "+direction + "| "  + type + "| " 
				+ result + "| " + socketWorkerId + "| "
				+ sdf.format(timestamp)  + "| " + stn + "| " 
				+ returnCode	+ "| " + asccendDatetime + "| "
				+ rawMessage.trim() +  "| " + exception+"]";
	}

	
}
