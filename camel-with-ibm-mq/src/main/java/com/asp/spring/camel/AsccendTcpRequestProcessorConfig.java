package com.asp.spring.camel;

public class AsccendTcpRequestProcessorConfig {

	public enum PreprocessMode {
		IGNORE, RETURN_ERROR, DROP
	}
	
	private PreprocessMode mode;
	private long asccendTimeoutValue;
	private String errorCode;
	
	public AsccendTcpRequestProcessorConfig(PreprocessMode mode,
			long asccendTimeoutValue, String errorCode) {
		super();
		this.mode = mode;
		this.asccendTimeoutValue = asccendTimeoutValue;
		this.errorCode = errorCode;
	}

	public PreprocessMode getMode() {
		return mode;
	}

	public long getAsccendTimeoutValue() {
		return asccendTimeoutValue;
	}

	public String getErrorCode() {
		return errorCode;
	}
	
}
