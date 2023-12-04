package com.datalinkx.common.result;

import static com.datalinkx.common.constants.MetaConstants.CommonConstant.TRACE_ID;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@JsonIgnoreProperties(ignoreUnknown = true)
@Slf4j
@Data
public class WebResult<T> {
	private String status;
	private String errstr;
	private String traceId;
	private T result;


	public WebResult() {
	}


	public void setResult(T result) {
		this.result = result;
	}

	public WebResult<T> setStatus(String status) {
		this.status = status;
		this.traceId = MDC.get(TRACE_ID);
		return this;
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
