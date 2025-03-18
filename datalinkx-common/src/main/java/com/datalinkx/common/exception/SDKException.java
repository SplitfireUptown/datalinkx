package com.datalinkx.common.exception;

import lombok.Getter;

@Getter
public class SDKException extends RuntimeException {

	private Integer status;

	public SDKException(String msg) {
		super(msg);
		status = 2;
	}

	public SDKException(Throwable throwable) {
		super(throwable);
		status = 2;
	}

	public SDKException(Integer status, String msg) {
		super(msg);
		this.status = status;
	}
}
