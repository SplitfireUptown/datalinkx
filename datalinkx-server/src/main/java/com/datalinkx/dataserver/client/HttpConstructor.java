package com.datalinkx.dataserver.client;

import cn.hutool.core.lang.Pair;
import com.datalinkx.common.exception.DatalinkXServerException;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.dataserver.controller.form.JobForm;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: uptown
 * @date: 2024/12/16 21:46
 */
@Slf4j
public class HttpConstructor {

    public static Map<String, String> contentTypeMapping = new HashMap<>();
    static {
        contentTypeMapping.put("raw", "application/json");
        contentTypeMapping.put("x-www-form-urlencoded", "application/x-www-form-urlencoded");
        contentTypeMapping.put("form-data", "multipart/form-data");
    }

    private static final OkHttpClient client = new OkHttpClient();

    public static Object go(JobForm.HttpTestForm httpTestForm) {

        String apiUrl = httpTestForm.getApiUrl();
        if (!apiUrl.contains("http") && !apiUrl.contains("https")) {
            apiUrl = "http://" + apiUrl;
        }

        // 1、解析param参数
        HttpUrl.Builder urlBuilder = HttpUrl.parse(apiUrl).newBuilder();
        for (JobForm.ItemConfig itemConfig : httpTestForm.getParam()) {
            urlBuilder.addQueryParameter(itemConfig.getKey(), itemConfig.getValue());
        }


        Request.Builder requestBuilder = new Request.Builder()
                .url(urlBuilder.build());

        if ("POST".equalsIgnoreCase(httpTestForm.getMethod().toLowerCase())) {
            // 2、解析body
            if (httpTestForm.getContentType().equalsIgnoreCase("raw")) {
                RequestBody requestBody = RequestBody.create(MediaType.parse(contentTypeMapping.get(httpTestForm.getContentType())), JsonUtils.toJson(httpTestForm.getBody()));
                requestBuilder.method(httpTestForm.getMethod().toLowerCase(), requestBody);
            }
            else {
                FormBody.Builder formBody = new FormBody.Builder();
                for (JobForm.ItemConfig itemConfig : httpTestForm.getBody()) {
                    formBody.add(itemConfig.getKey(), itemConfig.getValue());
                }
                requestBuilder.post(formBody.build());
            }
        }

        // 3、解析header
        for (JobForm.ItemConfig itemConfig : httpTestForm.getHeader()) {
            requestBuilder.addHeader(itemConfig.getKey(), itemConfig.getValue());
        }


        Request request = requestBuilder.build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                ResponseBody responseBody = response.body();
                // 4、解析配置json_path
                return JsonUtils.touchJsonPath(responseBody.string(), httpTestForm.getJsonPath());
            } else {
                throw new DatalinkXServerException(response.message());
            }
        } catch (Exception e) {

            log.error(e.getMessage(), e);
            throw new DatalinkXServerException("接口请求失败: " + e.getMessage());
        }
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
