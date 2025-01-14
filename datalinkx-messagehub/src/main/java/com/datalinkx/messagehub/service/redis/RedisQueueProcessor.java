package com.datalinkx.messagehub.service.redis;

import java.lang.reflect.Method;

import com.datalinkx.messagehub.bean.form.ConsumerAdapterForm;
import com.datalinkx.messagehub.bean.form.ProducerAdapterForm;
import com.datalinkx.messagehub.service.MessageHubServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;


@Slf4j
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
@Service("redisQueueProcessor")
public class RedisQueueProcessor extends MessageHubServiceImpl {

    @Override
    public void produce(ProducerAdapterForm producerAdapterForm) {
        stringRedisTemplate.opsForList().leftPush(producerAdapterForm.getTopic(), producerAdapterForm.getMessage());
    }

    @Override
    public void consume(ConsumerAdapterForm messageForm) {
        String topic = messageForm.getTopic();
        Object consumerBean = messageForm.getBean();
        Method invokeMethod = messageForm.getInvokeMethod();

        new Thread(() -> {
            while (true) {
                try {
                    boolean isEmpty = stringRedisTemplate.opsForList().size(topic) == 0;
                    if (isEmpty) {

                        Thread.sleep(1000);
                        continue;
                    }

                    String message = stringRedisTemplate.opsForList().rightPop(messageForm.getTopic());
                    if (ObjectUtils.isEmpty(message)) {

                        Thread.sleep(1000);
                        continue;
                    }

                    invokeMethod.invoke(consumerBean, message);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);

                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
