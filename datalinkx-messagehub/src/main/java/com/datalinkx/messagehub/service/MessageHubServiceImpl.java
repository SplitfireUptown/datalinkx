package com.datalinkx.messagehub.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.datalinkx.common.constants.MessageHubConstants;
import com.datalinkx.messagehub.bean.form.BaseMessageForm;
import com.datalinkx.messagehub.bean.form.ConsumerAdapterForm;
import com.datalinkx.messagehub.bean.form.ProducerAdapterForm;
import com.datalinkx.messagehub.service.redis.RedisPubSubProcessor;
import com.datalinkx.messagehub.service.redis.RedisQueueProcessor;
import com.datalinkx.messagehub.service.redis.RedisStreamProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class MessageHubServiceImpl implements MessageHubService, ApplicationContextAware {

    @Resource
    protected StringRedisTemplate stringRedisTemplate;

    public Map<String, MessageHubService> messageHubServiceMap = new ConcurrentHashMap<>();

    private ApplicationContext applicationContext;


    @PostConstruct
    public void init() {
        this.messageHubServiceMap.put(MessageHubConstants.REDIS_PUBSUB_TYPE, applicationContext.getBean(RedisPubSubProcessor.class));
        this.messageHubServiceMap.put(MessageHubConstants.REDIS_STREAM_TYPE, applicationContext.getBean(RedisStreamProcessor.class));
        this.messageHubServiceMap.put(MessageHubConstants.REDIS_QUEUE_TYPE, applicationContext.getBean(RedisQueueProcessor.class));
    }

    public void checkTopic(BaseMessageForm baseMessageForm) {
        String topic = baseMessageForm.getTopic();
        String type = baseMessageForm.getType();

        if (!this.messageHubServiceMap.containsKey(type)) {

            throw new RuntimeException("消息类型不支持");
        }

        // 由外部携带前缀topic转换成内部白名单不携带前缀topic
        String[] topicSplit = topic.split(":");
        String innerTopic = String.format("%s:%s", topicSplit[1], topicSplit[2]);
        if (!Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(MessageHubConstants.WHITE_TOPIC, innerTopic))) {
            log.error("检测到topic未配置");
        }
    }

    @Override
    public void produce(ProducerAdapterForm producerAdapterForm) {
        this.checkTopic(producerAdapterForm);
        this.messageHubServiceMap.get(producerAdapterForm.getType()).produce(producerAdapterForm);
    }


    @Override
    public void consume(ConsumerAdapterForm messageForm) {
        this.checkTopic(messageForm);
        this.messageHubServiceMap.get(messageForm.getType()).consume(messageForm);
        log.info("messagehub consumer init successful: type {}, topic {} , class {}#{}", messageForm.getType(), messageForm.getTopic(), messageForm.getBean().getClass().getName(), messageForm.getInvokeMethod().getName());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
