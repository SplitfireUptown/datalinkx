package com.datalinkx.dataserver.config;

import java.util.Optional;

import com.datalinkx.common.result.StatusCode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class WebResult<T> {
	private String status;
	private String errstr;
	private T result;


	public WebResult() {
	}

	public WebResult(T result, String errstr) {
		this.result = result;
		this.errstr = errstr;
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

	public static <T> WebResult<T> of(T result) {
		if (result instanceof Optional) {
			return of((Optional<T>) result);
		}
		WebResult<T> r = newInstance();
		r.setResult(result);
		r.setStatus("0");
		return r;
	}

	public static <T> WebResult<T> of(Optional<T> result) {
		return result.isPresent()
				? of(result.get())
				: new WebResult<T>().setErrstr("object does not exists").setStatus("101");
	}

	public static <T> WebResult<T> fail(Throwable throwable) {
		log.error(throwable.getMessage(), throwable);
		WebResult<T> r = newInstance();
		r.setStatus("500");
		r.setErrstr(throwable.getMessage());
		return r;
	}

	public static <T> WebResult<T> fail(Throwable throwable, T o) {
		log.error(throwable.getMessage(), throwable);
		String msg = Optional.ofNullable(throwable.getCause())
				.map(s -> s.getMessage() + ",")
				.orElse("") + throwable.getMessage();
		WebResult<T> r = newInstance();
		r.setErrstr(msg);
		r.setStatus(String.valueOf(StatusCode.API_INTERNAL_ERROR.getValue()));
		r.setResult(o);
		return r;
	}

	public static <T> WebResult<T> newInstance() {
		return new WebResult<T>();
	}

}
