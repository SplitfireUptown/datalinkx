package com.datalinkx.dataclient.config;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

import com.datalinkx.common.exception.DatalinkXSDKException;
import lombok.SneakyThrows;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Response;
import retrofit2.Retrofit;


/**
 * 消除默认的返回值Call<T> 使得 可以使用正常的接口返回值 将兼容异步调用的的返回值方式 直接转换成同步调用的结果返回
 */
public final class SynchronousCallAdapterFactory extends CallAdapter.Factory {

	/**
	 * 调用返回状态不符合 就抛出异常
	 */
	private boolean errorThrow = true;

	private SynchronousCallAdapterFactory(Boolean errorThrow) {
		this.errorThrow = errorThrow;
	}

	private SynchronousCallAdapterFactory() {
	}

	public static CallAdapter.Factory create() {
		return new SynchronousCallAdapterFactory();
	}

	public static CallAdapter.Factory create(Boolean errorThrow) {
		return new SynchronousCallAdapterFactory(errorThrow);
	}

	@Override
	public CallAdapter<Object, Object> get(final Type returnType, Annotation[] annotations, Retrofit retrofit) {
		if (returnType.toString().contains("retrofit2.Call")) {
			return null;
		}
		return new CallAdapter<Object, Object>() {
			@Override
			public Type responseType() {
				return returnType;
			}
			@SneakyThrows
			@Override
			public Object adapt(Call<Object> call) {
				try {
					Response<Object> resp = call.execute();
					if (resp.isSuccessful()) {
						return handlerSuccessResp(resp);
					} else {
						throw new DatalinkXSDKException(call.request() + "\r\n" + resp);
					}
				} catch (Exception e) {
					throw new DatalinkXSDKException(e);
				}
			}
		};
	}

	private Object handlerSuccessResp(Response<Object> resp) throws IllegalAccessException {
		Object body = resp.body();
		if (!errorThrow) {
			return body;
		}
		Field statusField;
		try {
			statusField = body.getClass().getDeclaredField("status");
		} catch (NoSuchFieldException e) {
			return body;
		} catch (SecurityException e) {
			throw new DatalinkXSDKException(e);
		}
		statusField.setAccessible(true);
		Integer status = Integer.parseInt(statusField.get(body).toString());
		if (status != 0) {
			throw new DatalinkXSDKException(body.toString());
		} else {
			return body;
		}
	}

}
