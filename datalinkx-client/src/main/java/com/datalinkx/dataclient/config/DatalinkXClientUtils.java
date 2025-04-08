package com.datalinkx.dataclient.config;

import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.lang3.StringUtils;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Slf4j
public final class DatalinkXClientUtils {
    private static final long DEFAULT_CONNECT_TIMEOUT = 60000;
    private static final long DEFAULT_CALL_TIMEOUT = 60000;
    private static final long DEFAULT_READ_TIMEOUT = 60000;

    private DatalinkXClientUtils() {
    }


    public static <T> T createClient(String serviceName, ClientConfig.ServicePropertieBean properties, Class<T> clazz,
                                     Interceptor interceptor) {
        Retrofit retrofit = checkAndBuildRetrofit(properties, interceptor);
        return retrofit.create(clazz);
    }

    private static Retrofit checkAndBuildRetrofit(ClientConfig.ServicePropertieBean prop, Interceptor interceptor) {
        OkHttpClient.Builder okHttpBuider = buildOkHttpClient(prop);
        if (null != interceptor) {
            okHttpBuider.addInterceptor(interceptor);
        }

        return new Retrofit.Builder().baseUrl(prop.getUrl()).client(okHttpBuider.build())
                .addConverterFactory(JacksonConverterFactory.create())
                .addCallAdapterFactory(SynchronousCallAdapterFactory.create(prop.getErrorThrow())).build();
    }

    private static OkHttpClient.Builder buildOkHttpClient(ClientConfig.ServicePropertieBean prop) {
        OkHttpClient.Builder okHttpBuider = new OkHttpClient.Builder()
                .connectTimeout(
                        prop.getConnectTimeoutMs() != null ?
                                prop.getConnectTimeoutMs() : DEFAULT_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .callTimeout(
                        prop.getCallTimeoutMs() != null ?
                                prop.getCallTimeoutMs() : DEFAULT_CALL_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(
                        prop.getReadTimeoutMs() != null ?
                                prop.getReadTimeoutMs() : DEFAULT_READ_TIMEOUT, TimeUnit.MILLISECONDS
                );
        if (Boolean.TRUE.equals(prop.getLogging())) {
            HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor();
            logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            okHttpBuider.addInterceptor(logInterceptor);
        }
        return okHttpBuider;
    }
}
