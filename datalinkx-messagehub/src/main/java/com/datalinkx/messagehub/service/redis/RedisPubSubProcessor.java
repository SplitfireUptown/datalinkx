package com.datalinkx.messagehub.service.redis;

import javax.annotation.Resource;

import com.datalinkx.messagehub.bean.form.ConsumerAdapterForm;
import com.datalinkx.messagehub.bean.form.ProducerAdapterForm;
import com.datalinkx.messagehub.service.MessageHubServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Service;

@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
@Service("redisPubSubProcessor")
public class RedisPubSubProcessor extends MessageHubServiceImpl {

    @Resource
    RedisMessageListenerContainer redisPubSubContainer;

    @Override
    public void produce(ProducerAdapterForm producerAdapterForm) {
        stringRedisTemplate.convertAndSend(producerAdapterForm.getTopic(), producerAdapterForm.getMessage());
    }

    @Override
    public void consume(ConsumerAdapterForm messageForm) {
        MessageListenerAdapter adapter = new MessageListenerAdapter(messageForm.getBean(), messageForm.getInvokeMethod().getName());
        adapter.afterPropertiesSet();
        redisPubSubContainer.addMessageListener(adapter, new PatternTopic(messageForm.getTopic()));
    }


    @Bean
    public RedisMessageListenerContainer redisPubSubContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }
}
