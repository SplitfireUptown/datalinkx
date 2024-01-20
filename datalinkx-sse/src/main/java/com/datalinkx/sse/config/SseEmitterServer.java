package com.datalinkx.sse.config;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import com.datalinkx.common.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
public class SseEmitterServer {

    /**
     * 当前连接数
     */
    private static final AtomicInteger count = new AtomicInteger(0);

    /**
     * 使用map对象，便于根据userId来获取对应的SseEmitter，或者放redis里面
     */
    private static final Map<String, SseEmitter> sseEmitterMap = new ConcurrentHashMap<>();

    /**
     * 创建用户连接并返回 SseEmitter
     * @param employeeCode 用户ID
     * @return SseEmitter
     */
    public static SseEmitter connect(String employeeCode) {
        if (!ObjectUtils.isEmpty(sseEmitterMap.get(employeeCode))) {
            return sseEmitterMap.get(employeeCode);
        }
        SseEmitter sseEmitter = new SseEmitter(0L);
        // 注册回调
        sseEmitter.onCompletion(completionCallBack(employeeCode));
        sseEmitter.onError(errorCallBack(employeeCode));
        sseEmitter.onTimeout(timeoutCallBack(employeeCode));
        sseEmitterMap.put(employeeCode, sseEmitter);
        // 数量+1
        count.getAndIncrement();
        return sseEmitter;
    }

    /**
     * 给指定用户发送信息
     * @param employeeCode
     * @param jsonMsg
     */
    public static void sendMessage(String employeeCode, String jsonMsg) {
        try {
            SseEmitter emitter = sseEmitterMap.get(employeeCode);
            if (emitter == null) {
                emitter = connect(employeeCode);
            }
            emitter.send(jsonMsg, MediaType.APPLICATION_JSON);
        } catch (IOException e) {
            log.error("sse用户[{}]推送异常:", employeeCode, e);
            removeUser(employeeCode);
        }
    }


    /**
     * 移除用户连接
     */
    public static void removeUser(String employeeCode) {
        SseEmitter emitter = sseEmitterMap.get(employeeCode);
        if(emitter != null){
            emitter.complete();
        }
        sseEmitterMap.remove(employeeCode);
        // 数量-1
        count.getAndDecrement();
    }



    private static Runnable completionCallBack(String employeeCode) {
        return () -> {
            log.info("end sse connect：{}", employeeCode);
            removeUser(employeeCode);
        };
    }

    private static Runnable timeoutCallBack(String employeeCode) {
        return () -> {
            log.info("connect sse connect timeout：{}", employeeCode);
            removeUser(employeeCode);
        };
    }

    private static Consumer<Throwable> errorCallBack(String employeeCode) {
        return throwable -> {
            log.info("sse connect error：{}", employeeCode);
            removeUser(employeeCode);
        };
    }
}
