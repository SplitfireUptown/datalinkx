package com.datalinkx.sse.config;

import com.datalinkx.sse.config.oksse.RealEventSource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SseTransformUtil {

    public static SseEmitter transformRequest(Request request, String connectId) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.DAYS)
                .readTimeout(1, TimeUnit.DAYS) //这边需要将超时显示设置长一点，不然刚连上就断开，之前以为调用方式错误被坑了半天
                .build();

        SseEmitter sseEmitter = SseEmitterServer.connect(connectId);

        // 实例化EventSource，注册EventSource监听器
        RealEventSource realEventSource = new RealEventSource(request, new EventSourceListener() {
            @Override
            public void onOpen(EventSource eventSource, Response response) {
                log.info("SSE open");
            }

            @Override
            public void onEvent(EventSource eventSource, String id, String type, String data) {
                try {
                    SseEmitter.SseEventBuilder event = SseEmitter.event()
                            .data(data)
                            .id(id)
                            .name(type);
                    sseEmitter.send(event);
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                    sseEmitter.completeWithError(e);
                }
            }

            @Override
            public void onClosed(EventSource eventSource) {
                log.info("SSE close");
//                try {
//                    HashMap<String, HashMap> completeData = new HashMap<String, HashMap>() {{
//                        put("message",
//                                new HashMap<String, String>() {{
//                                    put("content", "complete");
//                                }}
//                        );
//                    }};
//                    SseEmitter.SseEventBuilder event = SseEmitter.event()
//                            .data(JsonUtils.toJson(completeData))
//                            .id("complete")
//                            .name("complete");
//                    sseEmitter.send(event);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
                sseEmitter.complete();
            }

            @Override
            public void onFailure(EventSource eventSource, Throwable t, Response response) {
                if (response != null) {
                    String msg;
                    try {
                        msg = response.body().string();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    if (!StringUtils.hasLength(msg)) {
                        msg = response.message();
                    }
                    log.error(msg);
                }
                sseEmitter.completeWithError(t);
            }
        });

        realEventSource.connect(okHttpClient); // 真正开始请求的一步
        // 在SSE连接关闭时执行清理操作
        sseEmitter.onCompletion(realEventSource::cancel);

        // 处理连接错误事件
        sseEmitter.onError(error -> {
            // 在发生错误时执行处理
            log.error("SSE web err: " + error.toString());
            realEventSource.cancel();
        });
        return sseEmitter;
    }
}
