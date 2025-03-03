package com.datalinkx.common.exception;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;


public class DatalinkXJobException extends RuntimeException {
	private static final long serialVersionUID = -940285811464169752L;


	public DatalinkXJobException(String msg) {
		super(msg);
	}

}
