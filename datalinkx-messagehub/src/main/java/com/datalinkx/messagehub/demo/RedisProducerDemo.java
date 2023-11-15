package com.datalinkx.messagehub.demo;

import javax.annotation.Resource;

import com.datalinkx.messagehub.bean.form.ProducerAdapterForm;
import com.datalinkx.messagehub.service.MessageHubService;


//@Component
public class RedisProducerDemo {

    @Resource(name = "messageHubServiceImpl")
    MessageHubService messageHubService;

    public void test(ProducerAdapterForm producerAdapterForm) {
        messageHubService.produce(producerAdapterForm);
    }
}
