package com.datalinkx.dataserver.client;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.lang.Tuple;
import com.datalinkx.common.exception.DatalinkXServerException;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.dataserver.controller.form.JobForm;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

/**
 * @author: uptown
 * @date: 2024/12/16 21:46
 */
@Slf4j
public class HttpConstructor {


    private static OkHttpClient client = new OkHttpClient();

    public static Object go(JobForm.HttpTestForm httpTestForm) {

        // 1、解析param参数
        HttpUrl.Builder urlBuilder = HttpUrl.parse(httpTestForm.getApiUrl()).newBuilder();
        for (Map.Entry<String, String> entry : httpTestForm.getParams().entrySet()) {
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }
        // 2、解析body
        RequestBody requestBody = RequestBody.create(MediaType.parse(httpTestForm.getContentType()), JsonUtils.toJson(httpTestForm.getBody()));

        Request.Builder requestBuilder = new Request.Builder()
                .url(urlBuilder.build())
                .method(httpTestForm.getMethod(), requestBody);

        // 3、解析header
        httpTestForm.getHeaders().forEach(requestBuilder::addHeader);

        Request request = requestBuilder.build();
        try {
            Response response = client.newCall(request).execute();
            ResponseBody responseBody = response.body();

            if (responseBody != null) {
                System.out.println(responseBody.string());
            }

            return responseBody;
        } catch (IOException e) {

            log.error(e.getMessage(), e);
        }
        throw new DatalinkXServerException("HTTP构造失败");
    }

    public static Pair<String, Integer> checkUrlFormat(String url) {
        try {
            URL urlObj = new URL(url);
            String ip = urlObj.getHost();
            int port = urlObj.getPort() != -1 ? urlObj.getPort() : urlObj.getDefaultPort();
            return Pair.of(ip, port);
        } catch (Exception e) {
            throw new DatalinkXServerException(url + "-> 解析host异常");
        }
    }
}
