package com.datalinkx.dataio.client.datalinkxserver;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class WebResult<T> {
	private String status;
	private String errstr;
	private T result;
	@JsonProperty("err_parameter")
	private T errorParam;
	private String trcid;


	public WebResult() {
	}

	public WebResult(T result, String errstr) {
		this.result = result;
		this.errstr = errstr;
	}

	public WebResult(T result, String errstr, T errorParam) {
		this.result = result;
		this.errstr = errstr;
		this.errorParam = errorParam;
	}

	public T getResult() {
		return this.result;
	}

	public WebResult<T> setResult(T result) {
		this.result = result;
		return this;
	}

	/**
	 * 兼容之前python服务返回的状态码（都是string类型的，而非int)
	 * @return
	 */
	public String getStatus() {
		return this.status;
	}

	public WebResult<T> setStatus(String status) {
		this.status = status;
		return this;
	}

	public String getErrstr() {
		return this.errstr;
	}

	public WebResult<T> setErrstr(String errstr) {
		this.errstr = errstr;
		return this;
	}

	public T getErrorParam() {
		return this.errorParam;
	}

	public WebResult<T> setErrorParam(T errorParam) {
		this.errorParam = errorParam;
		return this;
	}

	public static <T> WebResult<T> newInstance() {
		return new WebResult<T>();
	}

}
