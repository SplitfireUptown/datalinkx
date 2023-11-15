package com.datalinkx.common.exception;

import java.util.Map;

import com.datalinkx.common.result.StatusCode;
import com.fasterxml.jackson.annotation.JsonProperty;


public class DatalinkXServerException extends RuntimeException {
	private static final long serialVersionUID = -940285811464169752L;

	private StatusCode status;

	@JsonProperty("err_parameter")
	private Map<String, Object> errorParam;

	public DatalinkXServerException(String msg) {
		super(msg);
		status = StatusCode.API_INTERNAL_ERROR;
	}

	public DatalinkXServerException(StatusCode status, String msg) {
		super(msg);
		this.status = status;
	}

	public DatalinkXServerException(StatusCode status) {
		super(status.getMsg());
		this.status = status;
	}

	public DatalinkXServerException(Throwable throwable) {
		super(throwable);
		status = StatusCode.API_INTERNAL_ERROR;
	}

	public DatalinkXServerException(String msg, Throwable throwable) {
		super(msg, throwable);
		status = StatusCode.API_INTERNAL_ERROR;
	}

	public DatalinkXServerException(String msg, Throwable throwable, StatusCode status) {
		super(msg, throwable);
		this.status = status;
	}

	public DatalinkXServerException(Throwable throwable, StatusCode status) {
		super(throwable);
		this.status = status;
	}

	public DatalinkXServerException(StatusCode status, String msg, Map<String, Object> errorParam) {
		super(msg);
		this.status = status;
		this.errorParam = errorParam;
	}

	public StatusCode getStatus() {
		return status;
	}

	public Map<String, Object> getErrorParam() {
		return errorParam;
	}
}
