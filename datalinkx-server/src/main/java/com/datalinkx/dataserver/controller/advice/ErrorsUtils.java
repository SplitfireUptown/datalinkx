package com.datalinkx.dataserver.controller.advice;

import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 异常处理工具
 */
public final class ErrorsUtils {
	private ErrorsUtils() {
	}

	public static Throwable getRootCause(Throwable ex) {
		return ex.getCause() != null ? getRootCause(ex.getCause()) : ex;
	}

	public static Map<String, Object> compositeValiditionError(Errors err) {
		if (!err.hasErrors()) {
			return Collections.emptyMap();
		} else {
			return err.getFieldErrors().stream().distinct().collect(
					Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (v1, v2) -> v1 + "," + v2));
		}
	}

	public static String getDetailErrorMessage(Throwable ex) {
		return Arrays.stream(ex.getStackTrace())
			.map(trace -> trace.toString()).collect(Collectors.joining("\n"));
	}

}
