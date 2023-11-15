package com.datalinkx.common.exception;

public class SDKConnectException extends SDKException {
	public SDKConnectException(String msg) {
		super(msg);
	}

	public SDKConnectException(Exception e) {
		super(e.getMessage());
	}
}
