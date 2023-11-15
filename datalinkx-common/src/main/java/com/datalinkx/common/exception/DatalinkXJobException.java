package com.datalinkx.common.exception;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;


public class DatalinkXJobException extends RuntimeException {
	private static final long serialVersionUID = -940285811464169752L;

	private String errorMsg;

	@JsonProperty("err_parameter")
	private Map<String, Object> errorParam;

	public DatalinkXJobException(String msg) {
		super(msg);
	}

	public DatalinkXJobException(String msg, Throwable throwable) {
		super(msg, throwable);
	}

	public DatalinkXJobException(String msg, Map<String, Object> errorParam) {
		super(msg);
		this.errorParam = errorParam;
	}

	public Map<String, Object> getErrorParam() {
		return errorParam;
	}
}
