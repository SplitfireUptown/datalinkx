package com.datalinkx.messagehub.service;


import com.datalinkx.messagehub.bean.form.BaseMessageForm;
import com.datalinkx.messagehub.bean.form.ConsumerAdapterForm;
import com.datalinkx.messagehub.bean.form.ProducerAdapterForm;


/**
 * 生产消息通过producer api
 * 消费消息通过注解回调逻辑
 */
public interface MessageHubService {

    /**
     * 生产消息
     */
    void produce(ProducerAdapterForm producerAdapterForm);

    /**
     * 消费消息
     */
    void consume(ConsumerAdapterForm adapterForm);
}
